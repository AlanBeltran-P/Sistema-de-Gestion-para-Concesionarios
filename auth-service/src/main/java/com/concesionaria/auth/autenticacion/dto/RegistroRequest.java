package com.concesionaria.auth.autenticacion.dto;

import com.concesionaria.auth.validacion.PasswordSegura;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RF-02: auto-registro de un cliente desde el portal. */
public record RegistroRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @PasswordSegura String password,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellido
) {
}
