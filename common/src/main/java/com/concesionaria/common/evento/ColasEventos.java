package com.concesionaria.common.evento;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;

/**
 * Para declarar la cola de un consumidor con su cola de errores (.dlq):
 * <pre>
 * &#64;Bean
 * Declarables colaUsuarioRegistrado(TopicExchange eventosExchange) {
 *     return ColasEventos.declarar("cliente-service.usuario-registrado", "usuario.registrado", eventosExchange);
 * }
 * </pre>
 * Los mensajes que fallan después de los reintentos van a {cola}.dlq en vez de perderse.
 */
public final class ColasEventos {

    private ColasEventos() {
    }

    public static Declarables declarar(String cola, String routingKey, TopicExchange exchange) {
        String dlq = cola + ".dlq";
        Queue principal = QueueBuilder.durable(cola)
                .deadLetterExchange("")
                .deadLetterRoutingKey(dlq)
                .build();
        Queue errores = QueueBuilder.durable(dlq).build();
        Binding binding = BindingBuilder.bind(principal).to(exchange).with(routingKey);
        return new Declarables(principal, errores, binding);
    }
}
