package com.concesionaria.financiamiento.plan.dto;

import com.concesionaria.financiamiento.plan.service.CalculadoraCuotas.CuotaCalculada;

import java.math.BigDecimal;
import java.util.List;

/** RF-29: resultado de la simulación, con el detalle de cada cuota. */
public record SimulacionResponse(
        Long planId,
        String plan,
        BigDecimal tasaAnual,
        BigDecimal precio,
        BigDecimal anticipo,
        BigDecimal anticipoMinimo,
        BigDecimal montoFinanciado,
        int cantidadCuotas,
        BigDecimal valorCuota,
        BigDecimal totalIntereses,
        BigDecimal totalAPagar,
        List<CuotaCalculada> cuotas
) {
}
