package com.concesionaria.financiamiento.financiamiento.service;

import com.concesionaria.common.exception.AccesoDenegadoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.financiamiento.cliente.ClienteClient;
import com.concesionaria.financiamiento.cliente.dto.UsuarioPortalRequest;
import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.evento.CuotaEvento;
import com.concesionaria.financiamiento.evento.VentaEvento;
import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import com.concesionaria.financiamiento.financiamiento.dto.EstadoCuentaResponse;
import com.concesionaria.financiamiento.financiamiento.dto.FinanciamientoResumenResponse;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;
import com.concesionaria.financiamiento.financiamiento.repository.FinanciamientoRepository;
import com.concesionaria.financiamiento.plan.PlanFinanciamiento;
import com.concesionaria.financiamiento.plan.service.CalculadoraCuotas;
import com.concesionaria.financiamiento.plan.service.CalculadoraCuotas.CuotaCalculada;
import com.concesionaria.financiamiento.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** RF-30 (cronograma) y RF-32 (estado de cuenta). */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanciamientoService {

    private final FinanciamientoRepository financiamientoRepository;
    private final PlanService planService;
    private final ClienteClient clienteClient;
    private final ApplicationEventPublisher eventos;

    /**
     * RF-30: genera el crédito y sus cuotas (sistema francés). La primera cuota vence un mes
     * después de la venta. Si el evento llega repetido no se crea dos veces.
     */
    @Transactional
    public void crearDesdeVenta(VentaEvento venta) {
        if (financiamientoRepository.existsByVentaId(venta.ventaId())) {
            log.info("La venta {} ya tiene financiamiento; evento repetido", venta.ventaId());
            return;
        }
        PlanFinanciamiento plan = planService.buscar(venta.planFinanciamientoId());
        BigDecimal financiado = venta.montoFinanciado();
        int cantidad = venta.cantidadCuotas();
        List<CuotaCalculada> calculadas = CalculadoraCuotas.cronograma(financiado, plan.getTasaAnual(), cantidad);
        LocalDate base = venta.fechaVenta().toLocalDate();

        Financiamiento financiamiento = Financiamiento.builder()
                .ventaId(venta.ventaId())
                .numeroComprobante(venta.numeroComprobante())
                .clienteId(venta.clienteId())
                .clienteNombre(venta.clienteNombre())
                .clienteEmail(venta.clienteEmail())
                .plan(plan)
                .montoAnticipo(venta.saldoAPagar().subtract(financiado).max(BigDecimal.ZERO))
                .montoFinanciado(financiado)
                .tasaAnualAplicada(plan.getTasaAnual())
                .porcentajeMoraDiariaAplicado(plan.getPorcentajeMoraDiaria())
                .cantidadCuotas(cantidad)
                .valorCuota(calculadas.getFirst().monto())
                .fechaPrimerVencimiento(base.plusMonths(1))
                .build();
        for (CuotaCalculada c : calculadas) {
            financiamiento.agregarCuota(Cuota.builder()
                    .numero(c.numero())
                    .fechaVencimiento(base.plusMonths(c.numero()))
                    .capital(c.capital())
                    .interes(c.interes())
                    .monto(c.monto())
                    .build());
        }
        financiamientoRepository.save(financiamiento);
        financiamiento.getCuotas().forEach(c -> eventos.publishEvent(CuotaEvento.actualizada(c)));
        log.info("Financiamiento {} creado para la venta {}: {} cuotas de ${}",
                financiamiento.getId(), venta.ventaId(), cantidad, financiamiento.getValorCuota());
    }

    /** RF-26: la venta se anuló. Las cuotas impagas se anulan; los pagos registrados quedan. */
    @Transactional
    public void anularPorVenta(Long ventaId) {
        financiamientoRepository.findByVentaId(ventaId).ifPresentOrElse(f -> {
            if (f.getEstado() == EstadoFinanciamiento.ANULADO) {
                return;
            }
            f.setEstado(EstadoFinanciamiento.ANULADO);
            for (Cuota c : f.getCuotas()) {
                if (c.getEstado().impaga()) {
                    c.setEstado(EstadoCuota.ANULADA);
                    eventos.publishEvent(CuotaEvento.actualizada(c));
                }
            }
        }, () -> log.warn("Se anuló la venta {} pero no tenía financiamiento", ventaId));
    }

    @Transactional(readOnly = true)
    public Page<FinanciamientoResumenResponse> buscar(Long clienteId, EstadoFinanciamiento estado, Pageable pageable) {
        return financiamientoRepository.buscar(clienteId, estado, pageable).map(FinanciamientoResumenResponse::desde);
    }

    /** RF-32. Un cliente solo puede ver los suyos. */
    @Transactional(readOnly = true)
    public EstadoCuentaResponse estadoCuenta(Long id, UsuarioActual usuario) {
        Financiamiento f = financiamientoRepository.findWithPlanById(id)
                .orElseThrow(() -> new NoEncontradoException("Financiamiento " + id + " no encontrado"));
        if (usuario.esCliente() && !f.getClienteId().equals(clienteDelUsuario(usuario))) {
            throw new AccesoDenegadoException("Ese financiamiento no es tuyo");
        }
        return EstadoCuentaResponse.desde(f);
    }

    @Transactional(readOnly = true)
    public EstadoCuentaResponse deVenta(Long ventaId) {
        return financiamientoRepository.findByVentaId(ventaId)
                .map(EstadoCuentaResponse::desde)
                .orElseThrow(() -> new NoEncontradoException("La venta " + ventaId + " no tiene financiamiento"));
    }

    /** RF-32: los financiamientos del cliente logueado. */
    @Transactional(readOnly = true)
    public List<EstadoCuentaResponse> delCliente(UsuarioActual usuario) {
        return financiamientoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteDelUsuario(usuario))
                .stream().map(EstadoCuentaResponse::desde).toList();
    }

    private Long clienteDelUsuario(UsuarioActual usuario) {
        return clienteClient.delPortal(UsuarioPortalRequest.de(usuario)).id();
    }
}
