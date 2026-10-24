package com.concesionaria.financiamiento.plan.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * RF-29.
 *
 * @param precio   precio final del vehículo
 * @param anticipo todo lo que se paga sin financiar (efectivo + seña + valor del trade-in)
 */
public record SimulacionRequest(
        @NotNull Long planId,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal precio,
        @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal anticipo,
        @NotNull @Positive Integer cantidadCuotas
) {
}
