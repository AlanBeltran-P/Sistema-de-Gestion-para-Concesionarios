package com.concesionaria.venta.vehiculo.dto;

import java.math.BigDecimal;

/** Mismo formato que CrearVehiculoRequest de vehiculo-service. */
public record CrearVehiculoRemotoRequest(
        String vin,
        Long modeloId,
        Integer anio,
        String color,
        Integer kilometraje,
        BigDecimal precio,
        String condicion,
        String origen,
        String descripcion
) {
}
