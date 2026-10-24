package com.concesionaria.venta.plan.dto;

import java.math.BigDecimal;

/** @param anticipo todo lo que no se financia (efectivo + seña + trade-in) */
public record SimulacionRemotaRequest(Long planId, BigDecimal precio, BigDecimal anticipo, Integer cantidadCuotas) {
}
