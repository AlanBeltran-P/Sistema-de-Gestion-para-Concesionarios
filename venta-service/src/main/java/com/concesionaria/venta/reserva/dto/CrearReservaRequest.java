package com.concesionaria.venta.reserva.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * RF-21.
 *
 * @param clienteId    obligatorio si reserva un vendedor; un cliente del portal no lo manda (se usa el suyo)
 * @param cotizacionId opcional: la seña mínima se calcula sobre el precio cotizado
 * @param diasVigencia opcional (por defecto el configurado, con un máximo)
 */
public record CrearReservaRequest(
        Long clienteId,
        @NotNull Long vehiculoId,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal montoAnticipo,
        Long cotizacionId,
        @Min(1) @Max(90) Integer diasVigencia
) {
}
