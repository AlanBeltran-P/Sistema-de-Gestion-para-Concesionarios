package com.concesionaria.reporte.venta.dto;

import com.concesionaria.reporte.venta.VentaReporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una fila del detalle del reporte de ventas. */
public record VentaDetalle(
        Long ventaId,
        String numeroComprobante,
        LocalDateTime fechaVenta,
        Long vendedorId,
        String vendedorNombre,
        Long clienteId,
        String vehiculo,
        String condicion,
        String formaPago,
        boolean conTradeIn,
        BigDecimal precioFinal,
        BigDecimal comision,
        String estado
) {

    public static VentaDetalle desde(VentaReporte v) {
        return new VentaDetalle(v.getVentaId(), v.getNumeroComprobante(), v.getFechaVenta(), v.getVendedorId(),
                v.getVendedorNombre(), v.getClienteId(), v.getMarca() + " " + v.getModelo() + " " + v.getAnio(),
                v.getCondicion(), v.getFormaPago(), v.isConTradeIn(), v.getPrecioFinal(), v.getMontoComision(),
                v.getEstado());
    }
}
