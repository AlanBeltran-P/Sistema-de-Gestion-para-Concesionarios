package com.concesionaria.vehiculo.vehiculo.dto;

import com.concesionaria.vehiculo.imagen.ImagenVehiculo;
import com.concesionaria.vehiculo.imagen.dto.ImagenResponse;
import com.concesionaria.vehiculo.vehiculo.Vehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.CondicionVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;

import java.math.BigDecimal;

/** Tarjeta del catálogo: lo justo para el listado. */
public record VehiculoResumenResponse(
        Long id,
        String marca,
        String modelo,
        String categoria,
        Integer anio,
        String color,
        Integer kilometraje,
        BigDecimal precio,
        CondicionVehiculo condicion,
        EstadoVehiculo estado,
        String imagenPrincipalUrl
) {

    public static VehiculoResumenResponse desde(Vehiculo v) {
        String imagen = v.getImagenes().stream()
                .filter(ImagenVehiculo::isPrincipal)
                .findFirst()
                .map(i -> ImagenResponse.urlPublica(v.getId(), i.getId()))
                .orElse(null);
        return new VehiculoResumenResponse(
                v.getId(),
                v.getModelo().getMarca().getNombre(),
                v.getModelo().getNombre(),
                v.getModelo().getCategoria().getNombre(),
                v.getAnio(),
                v.getColor(),
                v.getKilometraje(),
                v.getPrecio(),
                v.getCondicion(),
                v.getEstado(),
                imagen);
    }
}
