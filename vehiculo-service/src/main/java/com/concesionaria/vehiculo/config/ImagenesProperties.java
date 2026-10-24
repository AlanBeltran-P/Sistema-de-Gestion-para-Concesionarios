package com.concesionaria.vehiculo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * "vehiculos.imagenes.*" de config-repo/vehiculo-service.yml.
 *
 * @param directorio     carpeta donde se guardan los archivos
 * @param maxPorVehiculo cantidad máxima de imágenes por vehículo
 */
@ConfigurationProperties("vehiculos.imagenes")
public record ImagenesProperties(String directorio, int maxPorVehiculo) {
}
