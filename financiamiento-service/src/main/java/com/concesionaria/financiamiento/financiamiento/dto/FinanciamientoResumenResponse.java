package com.concesionaria.financiamiento.financiamiento.dto;

import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Para los listados. El detalle con cuotas y pagos es EstadoCuentaResponse. */
public record FinanciamientoResumenResponse(
        Long id,
        Long ventaId,
        String numeroComprobante,
        Long clienteId,
        String clienteNombre,
        Long planId,
        String plan,
        BigDecimal montoFinanciado,
        BigDecimal tasaAnualAplicada,
        Integer cantidadCuotas,
        BigDecimal valorCuota,
        LocalDate fechaPrimerVencimiento,
        EstadoFinanciamiento estado,
        LocalDateTime fechaCreacion
) {

    public static FinanciamientoResumenResponse desde(Financiamiento f) {
        return new FinanciamientoResumenResponse(
                f.getId(),
                f.getVentaId(),
                f.getNumeroComprobante(),
                f.getClienteId(),
                f.getClienteNombre(),
                f.getPlan().getId(),
                f.getPlan().getNombre(),
                f.getMontoFinanciado(),
                f.getTasaAnualAplicada(),
                f.getCantidadCuotas(),
                f.getValorCuota(),
                f.getFechaPrimerVencimiento(),
                f.getEstado(),
                f.getFechaCreacion());
    }
}
