package com.concesionaria.vehiculo.vehiculo.dto;

import com.concesionaria.vehiculo.imagen.dto.ImagenResponse;
import com.concesionaria.vehiculo.vehiculo.Vehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.CondicionVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.OrigenVehiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Detalle completo del vehículo (RF-13). */
public record VehiculoResponse(
        Long id,
        String vin,
        Long marcaId,
        String marca,
        Long modeloId,
        String modelo,
        Long categoriaId,
        String categoria,
        Integer anio,
        String color,
        Integer kilometraje,
        BigDecimal precio,
        CondicionVehiculo condicion,
        EstadoVehiculo estado,
        OrigenVehiculo origen,
        String descripcion,
        boolean activo,
        List<ImagenResponse> imagenes,
        LocalDateTime fechaIngreso
) {

    public static VehiculoResponse desde(Vehiculo v) {
        var modelo = v.getModelo();
        return new VehiculoResponse(
                v.getId(),
                v.getVin(),
                modelo.getMarca().getId(),
                modelo.getMarca().getNombre(),
                modelo.getId(),
                modelo.getNombre(),
                modelo.getCategoria().getId(),
                modelo.getCategoria().getNombre(),
                v.getAnio(),
                v.getColor(),
                v.getKilometraje(),
                v.getPrecio(),
                v.getCondicion(),
                v.getEstado(),
                v.getOrigen(),
                v.getDescripcion(),
                v.isActivo(),
                v.getImagenes().stream().map(i -> ImagenResponse.desde(v.getId(), i)).toList(),
                v.getFechaIngreso());
    }
}
