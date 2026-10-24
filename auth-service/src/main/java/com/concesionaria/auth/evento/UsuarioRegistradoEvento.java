package com.concesionaria.auth.evento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Se publica al crear cualquier usuario.
 * - notificacion-service: email de bienvenida (RF-34).
 * - cliente-service: si el rol es CLIENTE, crea su perfil de cliente vinculado al usuarioId.
 */
public record UsuarioRegistradoEvento(
        String eventoId,
        Long usuarioId,
        String email,
        String nombre,
        String apellido,
        String rol,
        LocalDateTime fecha
) implements EventoIntegracion {

    public static final String ROUTING_KEY = "usuario.registrado";

    public UsuarioRegistradoEvento(Long usuarioId, String email, String nombre, String apellido, String rol) {
        this(UUID.randomUUID().toString(), usuarioId, email, nombre, apellido, rol, LocalDateTime.now());
    }

    @Override
    public String routingKey() {
        return ROUTING_KEY;
    }
}
