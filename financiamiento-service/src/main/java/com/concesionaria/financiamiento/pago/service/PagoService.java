package com.concesionaria.financiamiento.pago.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.cuota.dto.CuotaResponse;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.evento.CuotaEvento;
import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;
import com.concesionaria.financiamiento.financiamiento.repository.FinanciamientoRepository;
import com.concesionaria.financiamiento.pago.Pago;
import com.concesionaria.financiamiento.pago.dto.PagoResponse;
import com.concesionaria.financiamiento.pago.dto.RegistrarPagoRequest;
import com.concesionaria.financiamiento.pago.enums.TipoPago;
import com.concesionaria.financiamiento.pago.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

/**
 * RF-31. Las cuotas se pagan en orden; se aceptan pagos parciales.
 * Cada pago cubre primero la mora pendiente y después la cuota.
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    private final FinanciamientoRepository financiamientoRepository;
    private final PagoRepository pagoRepository;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public PagoResponse registrar(RegistrarPagoRequest request, UsuarioActual usuario) {
        Financiamiento f = financiamientoRepository.findWithPlanById(request.financiamientoId())
                .orElseThrow(() -> new NoEncontradoException("Financiamiento " + request.financiamientoId() + " no encontrado"));
        if (f.getEstado() != EstadoFinanciamiento.ACTIVO && f.getEstado() != EstadoFinanciamiento.EN_MORA) {
            throw new ConflictoException("El financiamiento está " + f.getEstado() + ": no admite pagos");
        }
        Pago pago = request.tipo() == TipoPago.ANTICIPO
                ? pagarAnticipo(f, request)
                : pagarCuota(f, request);
        pago.setMedioPago(request.medioPago());
        pago.setReferencia(request.referencia());
        pago.setRegistradoPorId(usuario.id());
        f.agregarPago(pago);
        return PagoResponse.desde(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public Page<PagoResponse> buscar(Long financiamientoId, Long clienteId, Pageable pageable) {
        return pagoRepository.buscar(financiamientoId, clienteId, pageable).map(PagoResponse::desde);
    }

    private Pago pagarAnticipo(Financiamiento f, RegistrarPagoRequest request) {
        if (request.cuotaId() != null) {
            throw new SolicitudInvalidaException("Un pago de anticipo no lleva cuotaId");
        }
        BigDecimal pendiente = f.getMontoAnticipo().subtract(f.getMontoAnticipoPagado());
        if (pendiente.signum() <= 0) {
            throw new ConflictoException("El anticipo ya está pago");
        }
        if (request.monto().compareTo(pendiente) > 0) {
            throw new SolicitudInvalidaException("El anticipo pendiente es $" + pendiente.toPlainString());
        }
        f.setMontoAnticipoPagado(f.getMontoAnticipoPagado().add(request.monto()));
        return Pago.builder().tipo(TipoPago.ANTICIPO).monto(request.monto()).build();
    }

    private Pago pagarCuota(Financiamiento f, RegistrarPagoRequest request) {
        Cuota primeraImpaga = f.getCuotas().stream()
                .filter(c -> c.getEstado().impaga())
                .min(Comparator.comparing(Cuota::getNumero))
                .orElseThrow(() -> new ConflictoException("No hay cuotas pendientes de pago"));
        Cuota cuota = primeraImpaga;
        if (request.cuotaId() != null) {
            cuota = f.getCuotas().stream()
                    .filter(c -> c.getId().equals(request.cuotaId()))
                    .findFirst()
                    .orElseThrow(() -> new NoEncontradoException(
                            "La cuota " + request.cuotaId() + " no es de este financiamiento"));
            if (!cuota.getEstado().impaga()) {
                throw new ConflictoException("La cuota " + cuota.getNumero() + " está " + cuota.getEstado());
            }
            if (cuota != primeraImpaga) {
                throw new SolicitudInvalidaException("Primero hay que pagar la cuota " + primeraImpaga.getNumero());
            }
        }

        BigDecimal saldo = CuotaResponse.saldo(cuota);
        if (request.monto().compareTo(saldo) > 0) {
            throw new SolicitudInvalidaException("El saldo de la cuota " + cuota.getNumero() + " es $" + saldo.toPlainString());
        }
        BigDecimal moraPendiente = cuota.getMora().subtract(pagoRepository.sumarMoraPagada(cuota.getId())).max(BigDecimal.ZERO);
        BigDecimal parteMora = request.monto().min(moraPendiente);

        cuota.setMontoPagado(cuota.getMontoPagado().add(request.monto()));
        if (request.monto().compareTo(saldo) == 0) {
            cuota.setEstado(EstadoCuota.PAGADA);
            cuota.setFechaPago(LocalDate.now());
        } else if (cuota.getEstado() == EstadoCuota.PENDIENTE) {
            cuota.setEstado(EstadoCuota.PAGADA_PARCIAL);
        }
        f.recalcularEstado();
        eventos.publishEvent(CuotaEvento.actualizada(cuota));

        return Pago.builder().tipo(TipoPago.CUOTA).cuota(cuota).monto(request.monto()).montoMora(parteMora).build();
    }
}
