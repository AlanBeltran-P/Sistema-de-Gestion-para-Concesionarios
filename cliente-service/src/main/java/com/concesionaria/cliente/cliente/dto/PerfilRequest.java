package com.concesionaria.cliente.cliente.dto;

import com.concesionaria.cliente.cliente.enums.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * RF-15: el cliente completa o edita su perfil. Todo es opcional (puede completarlo de a partes),
 * pero el documento va completo: tipo y número juntos. Nombre y email se manejan desde su cuenta.
 */
public record PerfilRequest(
        TipoDocumento tipoDocumento,
        @Size(max = 20) String numeroDocumento,
        @Pattern(regexp = "^\\+?[0-9 ()\\-]{6,30}$", message = "Teléfono inválido") String telefono,
        @Valid DireccionDto direccion,
        @Past LocalDate fechaNacimiento
) {
}
