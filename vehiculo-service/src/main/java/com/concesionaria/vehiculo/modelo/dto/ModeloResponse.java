package com.concesionaria.vehiculo.modelo.dto;

import com.concesionaria.vehiculo.modelo.Modelo;

public record ModeloResponse(
        Long id,
        String nombre,
        Long marcaId,
        String marcaNombre,
        Long categoriaId,
        String categoriaNombre,
        boolean activo
) {

    public static ModeloResponse desde(Modelo modelo) {
        return new ModeloResponse(
                modelo.getId(),
                modelo.getNombre(),
                modelo.getMarca().getId(),
                modelo.getMarca().getNombre(),
                modelo.getCategoria().getId(),
                modelo.getCategoria().getNombre(),
                modelo.isActivo());
    }
}
