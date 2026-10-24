package com.concesionaria.common.evento;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía a RabbitMQ los EventoIntegracion publicados con ApplicationEventPublisher, recién
 * cuando la transacción se confirma. Así nunca sale un evento de algo que hizo rollback.
 * Se registra solo desde CommonEventosAutoConfiguration.
 */
@Slf4j
@RequiredArgsConstructor
public class PublicadorEventos {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(EventoIntegracion evento) {
        try {
            rabbitTemplate.convertAndSend(exchange, evento.routingKey(), evento);
            log.debug("Evento {} publicado ({})", evento.routingKey(), evento.eventoId());
        } catch (AmqpException e) {
            // La operación ya se guardó; si RabbitMQ no está disponible el evento se pierde.
            log.error("No se pudo publicar el evento {} ({})", evento.routingKey(), evento.eventoId(), e);
        }
    }
}
