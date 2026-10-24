package com.concesionaria.vehiculo.marca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaRequest(@NotBlank @Size(max = 60) String nombre) {
}
