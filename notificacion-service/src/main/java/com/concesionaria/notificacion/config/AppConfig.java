package com.concesionaria.notificacion.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita el reintento programado de los emails que fallaron. */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(NotificacionesProperties.class)
public class AppConfig {
}
