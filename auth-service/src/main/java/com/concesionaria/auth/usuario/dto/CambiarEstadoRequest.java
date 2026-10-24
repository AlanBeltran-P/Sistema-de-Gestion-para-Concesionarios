package com.concesionaria.auth.usuario.dto;

import jakarta.validation.constraints.NotNull;

/** RF-06: activar (true) o desactivar (false) un usuario. */
public record CambiarEstadoRequest(@NotNull Boolean activo) {
}
