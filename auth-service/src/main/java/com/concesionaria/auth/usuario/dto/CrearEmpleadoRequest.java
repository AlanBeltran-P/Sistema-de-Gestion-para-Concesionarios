package com.concesionaria.auth.usuario.dto;

import com.concesionaria.auth.usuario.enums.Rol;
import com.concesionaria.auth.validacion.PasswordSegura;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** RF-01: el ADMIN registra un empleado. El rol solo puede ser ADMIN o VENDEDOR. */
public record CrearEmpleadoRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @PasswordSegura String password,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellido,
        @NotNull Rol rol
) {
}
