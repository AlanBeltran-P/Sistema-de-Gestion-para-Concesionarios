package com.concesionaria.venta.evento;

import com.concesionaria.common.evento.EventoIntegracion;
import com.concesionaria.venta.cotizacion.Cotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Routing key: cotizacion.respondida. notificacion-service le manda un email al cliente
 * con el precio y la respuesta del vendedor (RF-35).
 */
public record CotizacionEvento(
        String eventoId,
        String tipo,
        Long cotizacionId,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        Long vehiculoId,
        String vehiculoDescripcion,
        Long vendedorId,
        String vendedorNombre,
        BigDecimal precioLista,
        BigDecimal descuento,
        BigDecimal valorTradeIn,
        BigDecimal precioFinal,
        String respuestaVendedor,
        LocalDate fechaValidez,
        LocalDateTime fecha
) implements EventoIntegracion {

    public static CotizacionEvento respondida(Cotizacion c, String clienteEmail) {
        return new CotizacionEvento(
                UUID.randomUUID().toString(),
                "respondida",
                c.getId(),
                c.getClienteId(),
                c.getClienteNombre(),
                clienteEmail,
                c.getVehiculoId(),
                c.getVehiculoDescripcion(),
                c.getVendedorId(),
                c.getVendedorNombre(),
                c.getPrecioLista(),
                c.getDescuento(),
                c.getValorTradeIn(),
                c.getPrecioFinal(),
                c.getRespuestaVendedor(),
                c.getFechaValidez(),
                LocalDateTime.now());
    }

    @Override
    public String routingKey() {
        return "cotizacion." + tipo;
    }
}
