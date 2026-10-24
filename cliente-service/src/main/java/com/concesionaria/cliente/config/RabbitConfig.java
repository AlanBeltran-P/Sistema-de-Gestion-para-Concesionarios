package com.concesionaria.cliente.config;

import com.concesionaria.cliente.evento.UsuarioRegistradoEvento;
import com.concesionaria.common.evento.ColasEventos;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Colas que escucha cliente-service. */
@Configuration
public class RabbitConfig {

    public static final String COLA_USUARIO_REGISTRADO = "cliente-service.usuario-registrado";

    @Bean
    Declarables colaUsuarioRegistrado(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_USUARIO_REGISTRADO, UsuarioRegistradoEvento.ROUTING_KEY, eventosExchange);
    }
}
