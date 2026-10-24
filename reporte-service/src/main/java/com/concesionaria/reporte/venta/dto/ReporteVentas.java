package com.concesionaria.reporte.venta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * RF-39: ventas de un período, por vendedor. Las anuladas solo se cuentan aparte.
 *
 * @param montoTotal suma de los precios finales de las ventas confirmadas
 */
public record ReporteVentas(
        LocalDate desde,
        LocalDate hasta,
        long cantidadVentas,
        long ventasAnuladas,
        BigDecimal montoTotal,
        BigDecimal ticketPromedio,
        BigDecimal comisiones,
        long conTradeIn,
        List<PorVendedor> porVendedor,
        List<PorGrupo> porFormaPago,
        List<PorGrupo> porCondicion
) {

    public record PorVendedor(Long vendedorId, String vendedorNombre, long cantidad, BigDecimal monto,
                              BigDecimal comisiones) {
    }

    public record PorGrupo(String grupo, long cantidad, BigDecimal monto) {
    }
}
