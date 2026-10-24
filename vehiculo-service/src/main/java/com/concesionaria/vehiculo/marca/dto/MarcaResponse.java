package com.concesionaria.vehiculo.marca.dto;

import com.concesionaria.vehiculo.marca.Marca;

public record MarcaResponse(Long id, String nombre, boolean activo) {

    public static MarcaResponse desde(Marca marca) {
        return new MarcaResponse(marca.getId(), marca.getNombre(), marca.isActivo());
    }
}
