package com.concesionaria.reporte.vehiculo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RF-40: inventario por estado y antigüedad en stock.
 *
 * @param enStock             vehículos activos sin vender (disponibles + reservados)
 * @param porEstado           vehículos activos por estado (incluye los vendidos)
 * @param antiguedad          vehículos en stock agrupados por días desde que ingresaron
 * @param masAntiguos         los 10 que más tiempo llevan en stock
 */
public record ReporteInventario(
        LocalDate fecha,
        long enStock,
        BigDecimal valorStock,
        double diasPromedioEnStock,
        Map<String, Long> porEstado,
        Map<String, Long> porCondicion,
        List<RangoAntiguedad> antiguedad,
        List<VehiculoEnStock> masAntiguos
) {

    public record RangoAntiguedad(String rango, long cantidad, BigDecimal valor) {
    }

    public record VehiculoEnStock(Long vehiculoId, String vin, String marca, String modelo, Integer anio,
                                  String condicion, String estado, BigDecimal precio, LocalDateTime fechaIngreso,
                                  long diasEnStock) {
    }
}
