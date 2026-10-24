package com.concesionaria.venta.cotizacion.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** RF-19: el vendedor arma una cotización para un cliente que vino al local (queda RESPONDIDA). */
public record CrearCotizacionRequest(
        @NotNull Long clienteId,
        @NotNull Long vehiculoId,
        @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal descuento,
        Long tasacionId,
        @NotBlank @Size(max = 1000) String respuesta,
        @Min(1) @Max(60) Integer diasValidez
) {

    public ResponderCotizacionRequest comoRespuesta() {
        return new ResponderCotizacionRequest(descuento, tasacionId, respuesta, diasValidez);
    }
}
