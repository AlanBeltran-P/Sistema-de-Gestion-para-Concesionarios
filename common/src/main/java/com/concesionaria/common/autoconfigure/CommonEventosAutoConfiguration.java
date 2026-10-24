package com.concesionaria.common.autoconfigure;

import com.concesionaria.common.evento.PublicadorEventos;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * RabbitMQ común: exchange "concesionaria.eventos" (topic), mensajes en JSON
 * (tanto al enviar como al recibir con @RabbitListener) y el PublicadorEventos.
 */
@AutoConfiguration(afterName = "org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration")
public class CommonEventosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "eventosExchange")
    TopicExchange eventosExchange(@Value("${eventos.exchange}") String nombre) {
        return new TopicExchange(nombre, true, false);
    }

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    PublicadorEventos publicadorEventos(RabbitTemplate rabbitTemplate, @Value("${eventos.exchange}") String exchange) {
        return new PublicadorEventos(rabbitTemplate, exchange);
    }
}
