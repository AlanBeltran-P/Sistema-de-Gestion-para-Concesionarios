package com.concesionaria.venta.comision.dto;

import java.math.BigDecimal;

/** Resultado de la consulta de totales (Number porque la base puede devolver Long o BigDecimal). */
public record TotalesComision(Number cantidad, Number pendiente, Number pagado) {

    public long cantidadVentas() {
        return cantidad != null ? cantidad.longValue() : 0;
    }

    public BigDecimal totalPendiente() {
        return aDecimal(pendiente);
    }

    public BigDecimal totalPagado() {
        return aDecimal(pagado);
    }

    private static BigDecimal aDecimal(Number n) {
        return n == null ? BigDecimal.ZERO : n instanceof BigDecimal b ? b : new BigDecimal(n.toString());
    }
}
