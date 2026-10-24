package com.concesionaria.vehiculo.vehiculo.dto;

import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;
import jakarta.validation.constraints.NotNull;

/** RF-11 */
public record CambiarEstadoVehiculoRequest(@NotNull EstadoVehiculo estado) {
}
