package com.concesionaria.venta.cotizacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** RF-18: el cliente pide precio por un vehículo desde el portal. */
public record SolicitarCotizacionRequest(
        @NotNull Long vehiculoId,
        @Size(max = 1000) String mensaje
) {
}
