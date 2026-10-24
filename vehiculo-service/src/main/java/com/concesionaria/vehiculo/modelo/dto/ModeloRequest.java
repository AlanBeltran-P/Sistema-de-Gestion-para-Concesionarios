package com.concesionaria.vehiculo.modelo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModeloRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull Long marcaId,
        @NotNull Long categoriaId
) {
}
