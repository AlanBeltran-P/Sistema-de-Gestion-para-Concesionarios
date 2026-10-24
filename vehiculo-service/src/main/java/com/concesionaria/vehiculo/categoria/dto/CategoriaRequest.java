package com.concesionaria.vehiculo.categoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 60) String nombre,
        @Size(max = 255) String descripcion
) {
}
