package com.concesionaria.financiamiento.evento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Copia (con los campos que se usan acá) del evento que publica venta-service.
 * Routing keys: venta.confirmada, venta.anulada.
 */
public record VentaEvento(
        String eventoId,
        String tipo,
        Long ventaId,
        String numeroComprobante,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        String formaPago,
        BigDecimal precioFinal,
        BigDecimal saldoAPagar,
        Long planFinanciamientoId,
        Integer cantidadCuotas,
        BigDecimal montoFinanciado,
        LocalDateTime fechaVenta
) {

    public boolean financiada() {
        return "FINANCIADA".equals(formaPago);
    }
}
