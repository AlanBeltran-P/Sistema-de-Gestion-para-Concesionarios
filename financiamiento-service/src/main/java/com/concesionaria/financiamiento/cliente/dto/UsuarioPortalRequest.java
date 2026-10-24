package com.concesionaria.financiamiento.cliente.dto;

import com.concesionaria.common.security.UsuarioActual;

public record UsuarioPortalRequest(Long usuarioId, String email, String nombre) {

    public static UsuarioPortalRequest de(UsuarioActual usuario) {
        return new UsuarioPortalRequest(usuario.id(), usuario.email(), usuario.nombre());
    }
}
