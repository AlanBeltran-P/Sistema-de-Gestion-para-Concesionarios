package com.concesionaria.venta.comision.dto;

import java.math.BigDecimal;

/** Totales de comisiones de un vendedor (o de todos) en un período. Las anuladas no suman. */
public record ResumenComisionesResponse(
        Long vendedorId,
        long cantidadVentas,
        BigDecimal totalPendiente,
        BigDecimal totalPagado,
        BigDecimal total
) {
}
