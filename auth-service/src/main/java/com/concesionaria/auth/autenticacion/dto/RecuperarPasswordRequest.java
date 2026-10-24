package com.concesionaria.auth.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** RF-05: pedir el enlace de recuperación. */
public record RecuperarPasswordRequest(@NotBlank @Email String email) {
}
