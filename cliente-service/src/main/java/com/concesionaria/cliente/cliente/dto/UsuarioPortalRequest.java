package com.concesionaria.cliente.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Datos del JWT del cliente que otro servicio reenvía para obtener (o crear) su perfil. */
public record UsuarioPortalRequest(
        @NotNull Long usuarioId,
        @NotBlank @Email String email,
        String nombre
) {
}
