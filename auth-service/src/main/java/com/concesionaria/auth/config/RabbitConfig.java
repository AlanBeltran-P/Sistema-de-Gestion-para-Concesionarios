package com.concesionaria.auth.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Los eventos se envían como JSON al exchange común "concesionaria.eventos". */
@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange eventosExchange(@Value("${eventos.exchange}") String nombre) {
        return new TopicExchange(nombre, true, false);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
