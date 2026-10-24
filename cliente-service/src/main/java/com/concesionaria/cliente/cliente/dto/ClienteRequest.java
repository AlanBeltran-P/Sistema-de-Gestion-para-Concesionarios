package com.concesionaria.cliente.cliente.dto;

import com.concesionaria.cliente.cliente.enums.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * RF-14 / RF-16: un empleado carga o edita un cliente en el local.
 * El documento es obligatorio y hace falta al menos un contacto (email o teléfono).
 */
public record ClienteRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellido,
        @NotNull TipoDocumento tipoDocumento,
        @NotBlank @Size(max = 20) String numeroDocumento,
        @Email @Size(max = 150) String email,
        @Pattern(regexp = "^\\+?[0-9 ()\\-]{6,30}$", message = "Teléfono inválido") String telefono,
        @Valid DireccionDto direccion,
        @Past LocalDate fechaNacimiento
) {
}
