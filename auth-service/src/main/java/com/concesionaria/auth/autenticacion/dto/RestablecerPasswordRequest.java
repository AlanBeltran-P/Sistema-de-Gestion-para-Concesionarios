package com.concesionaria.auth.autenticacion.dto;

import com.concesionaria.auth.validacion.PasswordSegura;
import jakarta.validation.constraints.NotBlank;

/** RF-05: nueva contraseña con el token que llegó por email. */
public record RestablecerPasswordRequest(@NotBlank String token, @PasswordSegura String nuevaPassword) {
}
