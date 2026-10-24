package com.concesionaria.reporte.cuota.service;

import com.concesionaria.reporte.cuota.CuotaReporte;
import com.concesionaria.reporte.cuota.dto.ReporteCartera;
import com.concesionaria.reporte.cuota.dto.ReporteCartera.Moroso;
import com.concesionaria.reporte.cuota.repository.CuotaReporteRepository;
import com.concesionaria.reporte.evento.Eventos;
import com.concesionaria.reporte.evento.service.RegistroEventos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Copia de las cuotas (con los eventos de financiamiento-service) y reporte RF-41. */
@Service
@RequiredArgsConstructor
public class CuotaReporteService {

    private static final String VENCIDA = "VENCIDA";
    private static final String PAGADA = "PAGADA";
    private static final String ANULADA = "ANULADA";

    private final CuotaReporteRepository repository;
    private final RegistroEventos registroEventos;

    @Transactional
    public void aplicar(Eventos.Cuota e) {
        if (!registroEventos.marcarNuevo(e.eventoId(), "cuota." + e.tipo())) {
            return;
        }
        CuotaReporte c = repository.findByCuotaId(e.cuotaId())
                .orElseGet(() -> CuotaReporte.builder().cuotaId(e.cuotaId()).build());
        if (c.getUltimaActualizacion() != null && e.fecha().isBefore(c.getUltimaActualizacion())) {
            return;
        }
        c.setFinanciamientoId(e.financiamientoId());
        c.setVentaId(e.ventaId());
        c.setClienteId(e.clienteId());
        c.setClienteNombre(e.clienteNombre());
        c.setNumero(e.numero());
        c.setMonto(e.monto());
        c.setMora(e.mora());
        c.setMontoPagado(e.montoPagado());
        c.setSaldo(e.saldo());
        c.setEstado(e.estado());
        c.setFechaVencimiento(e.fechaVencimiento());
        c.setFechaPago(e.fechaPago());
        c.setUltimaActualizacion(e.fecha());
        repository.save(c);
    }

    /** RF-41 */
    @Transactional(readOnly = true)
    public ReporteCartera cartera() {
        LocalDate hoy = LocalDate.now();
        List<CuotaReporte> cuotas = repository.findByEstadoNot(ANULADA);
        List<CuotaReporte> vencidas = cuotas.stream().filter(c -> VENCIDA.equals(c.getEstado())).toList();
        List<CuotaReporte> pendientes = cuotas.stream()
                .filter(c -> !VENCIDA.equals(c.getEstado()) && !PAGADA.equals(c.getEstado())).toList();

        List<Moroso> morosos = vencidas.stream()
                .collect(Collectors.groupingBy(CuotaReporte::getClienteId))
                .values().stream()
                .map(delCliente -> {
                    LocalDate masVieja = delCliente.stream().map(CuotaReporte::getFechaVencimiento)
                            .min(Comparator.naturalOrder()).orElse(hoy);
                    return new Moroso(delCliente.getFirst().getClienteId(), delCliente.getFirst().getClienteNombre(),
                            delCliente.size(), sumar(delCliente, CuotaReporte::getSaldo), masVieja,
                            Math.max(0, ChronoUnit.DAYS.between(masVieja, hoy)));
                })
                .sorted(Comparator.comparing(Moroso::deudaVencida).reversed())
                .toList();

        return new ReporteCartera(
                hoy,
                sumar(cuotas, CuotaReporte::getMontoPagado),
                sumar(pendientes, CuotaReporte::getSaldo),
                sumar(vencidas, CuotaReporte::getSaldo),
                sumar(cuotas, CuotaReporte::getMora),
                cuotas.stream().filter(c -> PAGADA.equals(c.getEstado())).count(),
                pendientes.size(),
                vencidas.size(),
                morosos);
    }

    private static BigDecimal sumar(List<CuotaReporte> cuotas, Function<CuotaReporte, BigDecimal> campo) {
        return cuotas.stream().map(campo).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
