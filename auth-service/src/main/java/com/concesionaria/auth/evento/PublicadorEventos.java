package com.concesionaria.auth.evento;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Los servicios publican eventos con ApplicationEventPublisher y acá se envían a RabbitMQ
 * recién cuando la transacción se confirma. Así nunca sale un evento de algo que
 * terminó haciendo rollback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PublicadorEventos {

    private final RabbitTemplate rabbitTemplate;

    @Value("${eventos.exchange}")
    private String exchange;

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
