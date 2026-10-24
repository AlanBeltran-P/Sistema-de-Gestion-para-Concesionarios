package com.concesionaria.reporte.config;

import com.concesionaria.common.evento.ColasEventos;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * reporte-service arma su propia copia de los datos con los eventos de los demás servicios.
 * Una cola por servicio de origen, así los eventos de una misma entidad llegan en orden.
 */
@Configuration
public class RabbitConfig {

    public static final String COLA_VEHICULOS = "reporte-service.vehiculos";
    public static final String COLA_VENTAS = "reporte-service.ventas";
    public static final String COLA_CUOTAS = "reporte-service.cuotas";

    @Bean
    Declarables colaVehiculos(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_VEHICULOS, "vehiculo.*", eventosExchange);
    }

    @Bean
    Declarables colaVentas(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_VENTAS, "venta.*", eventosExchange);
    }

    @Bean
    Declarables colaCuotas(TopicExchange eventosExchange) {
        return ColasEventos.declarar(COLA_CUOTAS, "cuota.*", eventosExchange);
    }
}
