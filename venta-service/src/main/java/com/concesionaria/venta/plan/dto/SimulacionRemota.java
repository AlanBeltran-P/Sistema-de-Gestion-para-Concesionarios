package com.concesionaria.venta.plan.dto;

import java.math.BigDecimal;

public record SimulacionRemota(
        Long planId,
        String plan,
        BigDecimal tasaAnual,
        BigDecimal montoFinanciado,
        int cantidadCuotas,
        BigDecimal valorCuota,
        BigDecimal totalAPagar
) {
}
