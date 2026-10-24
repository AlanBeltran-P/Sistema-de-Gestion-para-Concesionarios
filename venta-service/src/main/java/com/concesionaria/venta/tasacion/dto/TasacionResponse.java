package com.concesionaria.venta.tasacion.dto;

import com.concesionaria.venta.tasacion.TasacionTradeIn;
import com.concesionaria.venta.tasacion.enums.EstadoTasacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** @param vehiculoIngresadoId id del vehículo en el inventario, cuando se usó en una venta (RF-24) */
public record TasacionResponse(
        Long id,
        Long clienteId,
        Long vendedorId,
        String vin,
        Long modeloId,
        String modelo,
        Integer anio,
        String color,
        Integer kilometraje,
        String observaciones,
        BigDecimal valorTasado,
        EstadoTasacion estado,
        LocalDate fechaValidez,
        Long vehiculoIngresadoId,
        LocalDateTime fechaTasacion
) {

    public static TasacionResponse desde(TasacionTradeIn t) {
        return new TasacionResponse(
                t.getId(),
                t.getClienteId(),
                t.getVendedorId(),
                t.getVin(),
                t.getModeloId(),
                t.getModeloDescripcion(),
                t.getAnio(),
                t.getColor(),
                t.getKilometraje(),
                t.getObservaciones(),
                t.getValorTasado(),
                t.getEstado(),
                t.getFechaValidez(),
                t.getVehiculoIngresadoId(),
                t.getFechaTasacion());
    }
}
