package com.concesionaria.financiamiento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param diasAvisoVencimiento con cuántos días de anticipación se recuerda una cuota (RF-37)
 */
@ConfigurationProperties(prefix = "financiamiento")
public record FinanciamientoProperties(int diasAvisoVencimiento) {

    public FinanciamientoProperties {
        if (diasAvisoVencimiento <= 0) {
            diasAvisoVencimiento = 3;
        }
    }
}
