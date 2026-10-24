package com.concesionaria.venta.vehiculo.dto;

public record ModeloRemoto(Long id, String nombre, String marcaNombre, String categoriaNombre, boolean activo) {

    public String descripcion() {
        return marcaNombre + " " + nombre;
    }
}
