package com.concesionaria.venta.vehiculo.dto;

import java.math.BigDecimal;

/** Lo que se usa del detalle de vehiculo-service (el resto de los campos se ignora). */
public record VehiculoRemoto(
        Long id,
        String vin,
        Long modeloId,
        String marca,
        String modelo,
        String categoria,
        Integer anio,
        String color,
        Integer kilometraje,
        BigDecimal precio,
        String condicion,
        EstadoVehiculo estado,
        String origen,
        boolean activo
) {

    /** Ej: "Toyota Corolla 2024". */
    public String descripcion() {
        return marca + " " + modelo + " " + anio;
    }
}
