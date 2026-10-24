package com.concesionaria.reporte.reporte.dto;

import com.concesionaria.reporte.venta.dto.ReporteVentas.PorVendedor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** RF-42: KPIs del período. */
public record Dashboard(
        LocalDate desde,
        LocalDate hasta,
        BigDecimal ingresos,
        long unidadesVendidas,
        BigDecimal ticketPromedio,
        BigDecimal comisiones,
        List<TopModelo> topModelos,
        List<PorVendedor> topVendedores,
        List<PorMes> ventasPorMes,
        long stockDisponible,
        BigDecimal carteraVencida,
        long clientesMorosos
) {

    public record TopModelo(Long modeloId, String marca, String modelo, long unidades, BigDecimal ingresos) {
    }

    /** @param mes formato "2026-10" */
    public record PorMes(String mes, long unidades, BigDecimal ingresos) {
    }
}
