package com.concesionaria.common.evento;

/**
 * Evento que se publica en RabbitMQ para otros servicios.
 * Para publicarlo: applicationEventPublisher.publishEvent(evento) dentro de una transacción;
 * PublicadorEventos lo envía cuando la transacción se confirma.
 * eventoId es único: los consumidores lo usan para ignorar mensajes repetidos.
 */
public interface EventoIntegracion {

    String eventoId();

    /** Routing key en el exchange "concesionaria.eventos" (ej: usuario.registrado). */
    String routingKey();
}
