package com.concesionaria.cliente.evento;

import java.time.LocalDateTime;

/** Copia del evento que publica auth-service al crear un usuario (mismos campos). */
public record UsuarioRegistradoEvento(
        String eventoId,
        Long usuarioId,
        String email,
        String nombre,
        String apellido,
        String rol,
        LocalDateTime fecha
) {

    public static final String ROUTING_KEY = "usuario.registrado";
}
