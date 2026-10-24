package com.concesionaria.auth.usuario.dto;

import com.concesionaria.auth.usuario.enums.Rol;
import jakarta.validation.constraints.NotNull;

/** RF-06 */
public record CambiarRolRequest(@NotNull Rol rol) {
}
