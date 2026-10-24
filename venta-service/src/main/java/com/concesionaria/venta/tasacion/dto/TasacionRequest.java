package com.concesionaria.venta.tasacion.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** RF-20: datos del auto usado que el cliente entrega como parte de pago. */
public record TasacionRequest(
        @NotNull Long clienteId,
        @NotBlank
        @Pattern(regexp = "(?i)^[A-HJ-NPR-Z0-9]{17}$",
                message = "El VIN debe tener 17 caracteres (letras y números, sin I, O ni Q)")
        String vin,
        @NotNull Long modeloId,
        @NotNull @Min(1950) Integer anio,
        @NotBlank @Size(max = 40) String color,
        @NotNull @PositiveOrZero @Max(2_000_000) Integer kilometraje,
        @Size(max = 1000) String observaciones,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal valorTasado,
        @Min(1) @Max(60) Integer diasValidez
) {
}
