package com.concesionaria.venta.cotizacion.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * RF-19: el vendedor responde con el precio, un descuento opcional y, si el cliente entrega
 * su auto, la tasación (RF-20).
 */
public record ResponderCotizacionRequest(
        @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal descuento,
        Long tasacionId,
        @NotBlank @Size(max = 1000) String respuesta,
        @Min(1) @Max(60) Integer diasValidez
) {
}
