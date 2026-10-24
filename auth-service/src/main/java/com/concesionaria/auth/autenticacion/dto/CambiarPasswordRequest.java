package com.concesionaria.auth.autenticacion.dto;

import com.concesionaria.auth.validacion.PasswordSegura;
import jakarta.validation.constraints.NotBlank;

/** RF-05: el usuario logueado cambia su contraseña. */
public record CambiarPasswordRequest(@NotBlank String passwordActual, @PasswordSegura String nuevaPassword) {
}
