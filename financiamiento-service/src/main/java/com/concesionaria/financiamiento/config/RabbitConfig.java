package com.concesionaria.financiamiento.config;

import com.concesionaria.common.evento.ColasEventos;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Colas que escucha financiamiento-service.
 * Una sola cola para venta.confirmada y venta.anulada: así se procesan en el orden en que
 * ocurrieron (nunca se anula un financiamiento antes de crearlo).
 */
@Configuration
public class RabbitConfig {

    public static final String COLA_VENTAS = "financiamiento-service.ventas";

    @Bean
    Declarables colaVentas(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_VENTAS, "venta.*", eventosExchange);
    }
}
