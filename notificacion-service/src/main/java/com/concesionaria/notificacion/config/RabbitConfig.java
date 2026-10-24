package com.concesionaria.notificacion.config;

import com.concesionaria.common.evento.ColasEventos;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Una cola por cada evento que dispara un email (RF-34 a RF-37). */
@Configuration
public class RabbitConfig {

    public static final String COLA_USUARIO_REGISTRADO = "notificacion-service.usuario-registrado";
    public static final String COLA_RECUPERACION = "notificacion-service.recuperacion-password";
    public static final String COLA_COTIZACION = "notificacion-service.cotizacion-respondida";
    public static final String COLA_RESERVA = "notificacion-service.reserva-creada";
    public static final String COLA_VENTA = "notificacion-service.venta-confirmada";
    public static final String COLA_CUOTA_POR_VENCER = "notificacion-service.cuota-por-vencer";
    public static final String COLA_CUOTA_VENCIDA = "notificacion-service.cuota-vencida";

    @Bean
    Declarables colaUsuarioRegistrado(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_USUARIO_REGISTRADO, "usuario.registrado", eventosExchange);
    }

    @Bean
    Declarables colaRecuperacion(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_RECUPERACION, "usuario.recuperacion-password", eventosExchange);
    }

    @Bean
    Declarables colaCotizacion(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_COTIZACION, "cotizacion.respondida", eventosExchange);
    }

    @Bean
    Declarables colaReserva(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_RESERVA, "reserva.creada", eventosExchange);
    }

    @Bean
    Declarables colaVenta(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_VENTA, "venta.confirmada", eventosExchange);
    }

    @Bean
    Declarables colaCuotaPorVencer(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_CUOTA_POR_VENCER, "cuota.por-vencer", eventosExchange);
    }

    @Bean
    Declarables colaCuotaVencida(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_CUOTA_VENCIDA, "cuota.vencida", eventosExchange);
    }
}
