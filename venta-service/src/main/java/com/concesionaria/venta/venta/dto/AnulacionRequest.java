package com.concesionaria.venta.venta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RF-26: anular una venta o una reserva. */
public record AnulacionRequest(@NotBlank @Size(max = 500) String motivo) {
}
