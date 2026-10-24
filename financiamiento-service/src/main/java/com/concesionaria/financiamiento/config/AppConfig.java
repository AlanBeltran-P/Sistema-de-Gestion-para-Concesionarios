package com.concesionaria.financiamiento.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita el job diario de vencimientos y mora (RF-33, RF-37). */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(FinanciamientoProperties.class)
public class AppConfig {
}
