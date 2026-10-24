package com.concesionaria.venta.tasacion.dto;

import com.concesionaria.venta.tasacion.enums.EstadoTasacion;
import jakarta.validation.constraints.NotNull;

/** El cliente acepta o rechaza el valor ofrecido (lo registra el vendedor): ACEPTADA o RECHAZADA. */
public record CambiarEstadoTasacionRequest(@NotNull EstadoTasacion estado) {
}
