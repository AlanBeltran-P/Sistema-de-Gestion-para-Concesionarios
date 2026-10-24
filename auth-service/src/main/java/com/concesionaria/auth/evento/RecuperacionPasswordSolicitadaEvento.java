package com.concesionaria.auth.evento;

import com.concesionaria.common.evento.EventoIntegracion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Se publica cuando alguien pide recuperar su contraseña.
 * notificacion-service manda el email con el enlace que contiene el token (RF-34).
 * Es el único lugar donde viaja el token sin hashear.
 */
public record RecuperacionPasswordSolicitadaEvento(
        String eventoId,
        Long usuarioId,
        String email,
        String nombre,
        String token,
        LocalDateTime fechaExpiracion
) implements EventoIntegracion {

    public static final String ROUTING_KEY = "usuario.recuperacion-password";

    public RecuperacionPasswordSolicitadaEvento(Long usuarioId, String email, String nombre,
                                                String token, LocalDateTime fechaExpiracion) {
        this(UUID.randomUUID().toString(), usuarioId, email, nombre, token, fechaExpiracion);
    }

    @Override
    public String routingKey() {
        return ROUTING_KEY;
    }
}
