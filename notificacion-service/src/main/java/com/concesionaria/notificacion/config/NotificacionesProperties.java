package com.concesionaria.notificacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param remitente   "Nombre <email>" que aparece como From
 * @param urlFrontend base del link de recuperación de contraseña
 * @param maxIntentos cuántas veces se intenta mandar un email antes de darlo por perdido
 */
@ConfigurationProperties(prefix = "notificaciones")
public record NotificacionesProperties(String remitente, String empresa, String urlFrontend, int maxIntentos) {
}
