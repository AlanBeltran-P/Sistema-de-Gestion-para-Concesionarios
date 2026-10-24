package com.concesionaria.venta.reserva.dto;

import com.concesionaria.venta.reserva.Reserva;
import com.concesionaria.venta.reserva.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservaResponse(
        Long id,
        Long clienteId,
        String clienteNombre,
        Long vehiculoId,
        String vehiculo,
        Long vendedorId,
        Long cotizacionId,
        BigDecimal montoAnticipo,
        LocalDateTime fechaVencimiento,
        EstadoReserva estado,
        String motivoAnulacion,
        LocalDateTime fechaAnulacion,
        LocalDateTime fechaReserva
) {

    public static ReservaResponse desde(Reserva r) {
        return new ReservaResponse(
                r.getId(),
                r.getClienteId(),
                r.getClienteNombre(),
                r.getVehiculoId(),
                r.getVehiculoDescripcion(),
                r.getVendedorId(),
                r.getCotizacion() != null ? r.getCotizacion().getId() : null,
                r.getMontoAnticipo(),
                r.getFechaVencimiento(),
                r.getEstado(),
                r.getMotivoAnulacion(),
                r.getFechaAnulacion(),
                r.getFechaReserva());
    }
}
