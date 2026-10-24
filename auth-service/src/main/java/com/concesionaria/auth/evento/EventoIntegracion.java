package com.concesionaria.auth.evento;

/**
 * Evento que se publica en RabbitMQ para otros servicios.
 * eventoId es único: los consumidores lo usan para ignorar mensajes repetidos.
 */
public interface EventoIntegracion {

    String eventoId();

    /** Routing key en el exchange "concesionaria.eventos" (ej: usuario.registrado). */
    String routingKey();
}
