package com.concesionaria.vehiculo.vehiculo.dto;

import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;
import jakarta.validation.constraints.NotNull;

/**
 * RF-11.
 *
 * @param estadoEsperado opcional: si el vehículo no está en ese estado se rechaza con 409.
 *                       Lo usa venta-service para que dos reservas o ventas simultáneas del
 *                       mismo auto no salgan bien las dos.
 */
public record CambiarEstadoVehiculoRequest(@NotNull EstadoVehiculo estado, EstadoVehiculo estadoEsperado) {
}
