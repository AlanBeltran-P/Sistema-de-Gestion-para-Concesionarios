package com.concesionaria.venta.vehiculo.dto;

public record CambiarEstadoRemotoRequest(EstadoVehiculo estado, EstadoVehiculo estadoEsperado) {
}
