package com.concesionaria.financiamiento.plan.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

/** RF-28. Los porcentajes van como número: 45 = 45 %. */
public record PlanRequest(
        @NotBlank @Size(max = 80) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @DecimalMin("0") @DecimalMax("500") @Digits(integer = 4, fraction = 2) BigDecimal tasaAnual,
        @NotEmpty(message = "Indicá al menos un plazo")
        Set<@NotNull @Min(1) @Max(120) Integer> plazos,
        @NotNull @DecimalMin("0") @DecimalMax("95") @Digits(integer = 3, fraction = 2) BigDecimal porcentajeAnticipoMinimo,
        @NotNull @DecimalMin("0") @DecimalMax("5") @Digits(integer = 2, fraction = 4) BigDecimal porcentajeMoraDiaria
) {
}
