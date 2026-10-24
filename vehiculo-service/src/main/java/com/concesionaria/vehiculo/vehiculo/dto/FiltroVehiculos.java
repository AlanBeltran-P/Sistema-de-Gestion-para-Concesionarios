package com.concesionaria.vehiculo.vehiculo.dto;

import com.concesionaria.vehiculo.vehiculo.enums.CondicionVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;

import java.math.BigDecimal;

/**
 * Filtros del catálogo (RF-12), todos opcionales. Se leen de los query params:
 * /api/vehiculos?marcaId=1&precioMax=20000000&anioMin=2020&condicion=USADO&sort=precio,asc
 *
 * @param texto            busca en marca, modelo o VIN
 * @param estado           solo empleados (el público ve siempre DISPONIBLE)
 * @param incluirInactivos solo empleados: incluye los dados de baja
 */
public record FiltroVehiculos(
        Long marcaId,
        Long modeloId,
        Long categoriaId,
        CondicionVehiculo condicion,
        BigDecimal precioMin,
        BigDecimal precioMax,
        Integer anioMin,
        Integer anioMax,
        Integer kmMax,
        String texto,
        EstadoVehiculo estado,
        Boolean incluirInactivos
) {
}
