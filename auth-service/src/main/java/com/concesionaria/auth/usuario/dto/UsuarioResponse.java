package com.concesionaria.auth.usuario.dto;

import com.concesionaria.auth.usuario.Usuario;
import com.concesionaria.auth.usuario.enums.Rol;

import java.time.LocalDateTime;

/** Datos públicos del usuario. Nunca incluye la contraseña. */
public record UsuarioResponse(
        Long id,
        String email,
        String nombre,
        String apellido,
        Rol rol,
        boolean activo,
        LocalDateTime fechaCreacion
) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getFechaCreacion());
    }
}
