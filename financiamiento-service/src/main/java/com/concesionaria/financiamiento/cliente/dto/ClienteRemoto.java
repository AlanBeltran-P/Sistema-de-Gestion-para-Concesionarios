package com.concesionaria.financiamiento.cliente.dto;

/** Lo que se usa de la respuesta de cliente-service (el resto de los campos se ignora). */
public record ClienteRemoto(Long id, String nombre, String apellido, String email) {
}
