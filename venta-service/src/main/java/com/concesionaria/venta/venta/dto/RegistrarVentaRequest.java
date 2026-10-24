package com.concesionaria.venta.venta.dto;

import com.concesionaria.venta.venta.enums.FormaPago;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * RF-23.
 * - cotizacionId: si se indica, el precio, el descuento y el trade-in salen de la cotización.
 * - reservaId: opcional; si el cliente tiene una reserva activa del vehículo se usa sola y la seña
 *   se descuenta del saldo.
 * - tasacionId: venta con trade-in (RF-20, RF-24).
 * - FINANCIADA: planFinanciamientoId, cantidadCuotas y montoFinanciado son obligatorios.
 *   La diferencia entre el saldo y lo financiado es la entrega en efectivo.
 */
public record RegistrarVentaRequest(
        @NotNull Long clienteId,
        @NotNull Long vehiculoId,
        Long cotizacionId,
        Long reservaId,
        Long tasacionId,
        @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal descuento,
        @NotNull FormaPago formaPago,
        Long planFinanciamientoId,
        @Positive Integer cantidadCuotas,
        @Positive @Digits(integer = 12, fraction = 2) BigDecimal montoFinanciado
) {
}
