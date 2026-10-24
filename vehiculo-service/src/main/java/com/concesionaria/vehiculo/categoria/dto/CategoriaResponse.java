package com.concesionaria.vehiculo.categoria.dto;

import com.concesionaria.vehiculo.categoria.Categoria;

public record CategoriaResponse(Long id, String nombre, String descripcion, boolean activo) {

    public static CategoriaResponse desde(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(),
                categoria.getDescripcion(), categoria.isActivo());
    }
}
