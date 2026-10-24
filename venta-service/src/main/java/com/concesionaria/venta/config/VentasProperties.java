package com.concesionaria.venta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/** Reglas de negocio configurables (config-repo/venta-service.yml). */
@ConfigurationProperties(prefix = "ventas")
public record VentasProperties(
        Comision comision,
        BigDecimal descuentoMaximoVendedor,
        Cotizacion cotizacion,
        Tasacion tasacion,
        Reserva reserva,
        BigDecimal margenReventaTradeIn,
        Empresa empresa
) {

    public record Comision(BigDecimal porcentajeNuevo, BigDecimal porcentajeUsado) {
    }

    public record Cotizacion(int diasValidez) {
    }

    public record Tasacion(int diasValidez) {
    }

    public record Reserva(BigDecimal anticipoMinimoPorcentaje, int diasVigencia, int diasVigenciaMaximo) {
    }

    public record Empresa(String nombre, String cuit, String direccion, String telefono) {
    }
}
