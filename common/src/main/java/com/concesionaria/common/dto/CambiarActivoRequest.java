package com.concesionaria.common.dto;

import jakarta.validation.constraints.NotNull;

/** Activar (true) o desactivar (false) un registro con baja lógica. */
public record CambiarActivoRequest(@NotNull Boolean activo) {
}
