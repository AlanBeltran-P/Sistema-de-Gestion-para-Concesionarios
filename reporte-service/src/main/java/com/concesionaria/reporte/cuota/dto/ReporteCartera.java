package com.concesionaria.reporte.cuota.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * RF-41: cartera de créditos.
 *
 * @param cobrado   todo lo cobrado en cuotas (incluida la mora)
 * @param pendiente saldo de las cuotas que todavía no vencieron
 * @param vencido   saldo de las cuotas vencidas (con mora)
 * @param morosos   clientes con cuotas vencidas, de mayor a menor deuda
 */
public record ReporteCartera(
        LocalDate fecha,
        BigDecimal cobrado,
        BigDecimal pendiente,
        BigDecimal vencido,
        BigDecimal moraTotal,
        long cuotasPagadas,
        long cuotasPendientes,
        long cuotasVencidas,
        List<Moroso> morosos
) {

    public record Moroso(Long clienteId, String clienteNombre, long cuotasVencidas, BigDecimal deudaVencida,
                         LocalDate vencimientoMasAntiguo, long diasMaximoAtraso) {
    }
}
