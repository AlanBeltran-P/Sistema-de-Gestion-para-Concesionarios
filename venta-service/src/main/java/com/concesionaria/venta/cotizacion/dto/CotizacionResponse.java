package com.concesionaria.venta.cotizacion.dto;

import com.concesionaria.venta.cotizacion.Cotizacion;
import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CotizacionResponse(
        Long id,
        Long clienteId,
        String clienteNombre,
        Long vehiculoId,
        String vehiculo,
        Long vendedorId,
        String vendedorNombre,
        EstadoCotizacion estado,
        BigDecimal precioLista,
        BigDecimal descuento,
        Long tasacionId,
        BigDecimal valorTradeIn,
        BigDecimal precioFinal,
        String mensajeCliente,
        String respuestaVendedor,
        LocalDate fechaValidez,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaRespuesta
) {

    public static CotizacionResponse desde(Cotizacion c) {
        return new CotizacionResponse(
                c.getId(),
                c.getClienteId(),
                c.getClienteNombre(),
                c.getVehiculoId(),
                c.getVehiculoDescripcion(),
                c.getVendedorId(),
                c.getVendedorNombre(),
                c.getEstado(),
                c.getPrecioLista(),
                c.getDescuento(),
                c.getTasacion() != null ? c.getTasacion().getId() : null,
                c.getValorTradeIn(),
                c.getPrecioFinal(),
                c.getMensajeCliente(),
                c.getRespuestaVendedor(),
                c.getFechaValidez(),
                c.getFechaSolicitud(),
                c.getFechaRespuesta());
    }
}
