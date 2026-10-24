package com.concesionaria.venta.comision.dto;

import com.concesionaria.venta.comision.Comision;
import com.concesionaria.venta.comision.enums.EstadoComision;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComisionResponse(
        Long id,
        Long ventaId,
        String numeroComprobante,
        Long vendedorId,
        String vendedorNombre,
        BigDecimal baseCalculo,
        BigDecimal porcentaje,
        BigDecimal monto,
        EstadoComision estado,
        LocalDateTime fechaCalculo,
        LocalDateTime fechaPago
) {

    public static ComisionResponse desde(Comision c) {
        var venta = c.getVenta();
        return new ComisionResponse(
                c.getId(),
                venta.getId(),
                venta.getNumeroComprobante(),
                c.getVendedorId(),
                venta.getVendedorNombre(),
                c.getBaseCalculo(),
                c.getPorcentaje(),
                c.getMonto(),
                c.getEstado(),
                c.getFechaCalculo(),
                c.getFechaPago());
    }
}
