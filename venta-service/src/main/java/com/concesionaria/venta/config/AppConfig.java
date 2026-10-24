package com.concesionaria.venta.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita el job de reservas y cotizaciones vencidas (RF-22). */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(VentasProperties.class)
public class AppConfig {
}
