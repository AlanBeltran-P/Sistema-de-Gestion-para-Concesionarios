package com.concesionaria.vehiculo.vehiculo.dto;

import com.concesionaria.vehiculo.vehiculo.enums.CondicionVehiculo;
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

/** RF-09. El estado no se cambia acá sino con PATCH /estado. */
public record ActualizarVehiculoRequest(
        @NotBlank
        @Pattern(regexp = "(?i)^[A-HJ-NPR-Z0-9]{17}$",
                message = "El VIN debe tener 17 caracteres (letras y números, sin I, O ni Q)")
        String vin,
        @NotNull Long modeloId,
        @NotNull @Min(1950) Integer anio,
        @NotBlank @Size(max = 40) String color,
        @NotNull @PositiveOrZero @Max(2_000_000) Integer kilometraje,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal precio,
        @NotNull CondicionVehiculo condicion,
        @Size(max = 1000) String descripcion
) {
}
