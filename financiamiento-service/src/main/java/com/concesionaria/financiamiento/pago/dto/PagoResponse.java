package com.concesionaria.financiamiento.pago.dto;

import com.concesionaria.financiamiento.pago.Pago;
import com.concesionaria.financiamiento.pago.enums.MedioPago;
import com.concesionaria.financiamiento.pago.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
        Long id,
        Long financiamientoId,
        Long ventaId,
        Long clienteId,
        TipoPago tipo,
        Long cuotaId,
        Integer numeroCuota,
        BigDecimal monto,
        BigDecimal montoMora,
        MedioPago medioPago,
        String referencia,
        Long registradoPorId,
        LocalDateTime fechaPago
) {

    public static PagoResponse desde(Pago p) {
        var f = p.getFinanciamiento();
        var cuota = p.getCuota();
        return new PagoResponse(
                p.getId(),
                f.getId(),
                f.getVentaId(),
                f.getClienteId(),
                p.getTipo(),
                cuota != null ? cuota.getId() : null,
                cuota != null ? cuota.getNumero() : null,
                p.getMonto(),
                p.getMontoMora(),
                p.getMedioPago(),
                p.getReferencia(),
                p.getRegistradoPorId(),
                p.getFechaPago());
    }
}
