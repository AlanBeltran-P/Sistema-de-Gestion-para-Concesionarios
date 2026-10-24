package com.concesionaria.financiamiento.pago.dto;

import com.concesionaria.financiamiento.pago.enums.MedioPago;
import com.concesionaria.financiamiento.pago.enums.TipoPago;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * RF-31.
 *
 * @param cuotaId solo para tipo CUOTA; si no se indica se aplica a la cuota impaga más antigua
 */
public record RegistrarPagoRequest(
        @NotNull Long financiamientoId,
        @NotNull TipoPago tipo,
        Long cuotaId,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal monto,
        @NotNull MedioPago medioPago,
        @Size(max = 100) String referencia
) {
}
