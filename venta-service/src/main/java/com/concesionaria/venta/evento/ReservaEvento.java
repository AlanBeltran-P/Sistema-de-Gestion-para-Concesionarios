package com.concesionaria.venta.evento;

import com.concesionaria.common.evento.EventoIntegracion;
import com.concesionaria.venta.reserva.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Routing keys: reserva.creada (notificacion-service confirma la reserva por email, RF-36),
 * reserva.vencida (RF-22) y reserva.anulada (RF-26).
 */
public record ReservaEvento(
        String eventoId,
        String tipo,
        Long reservaId,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        Long vehiculoId,
        String vehiculoDescripcion,
        BigDecimal montoAnticipo,
        LocalDateTime fechaVencimiento,
        String estado,
        String motivo,
        LocalDateTime fecha
) implements EventoIntegracion {

    public static ReservaEvento creada(Reserva r, String clienteEmail) {
        return de(r, "creada", clienteEmail);
    }

    public static ReservaEvento vencida(Reserva r) {
        return de(r, "vencida", null);
    }

    public static ReservaEvento anulada(Reserva r) {
        return de(r, "anulada", null);
    }

    private static ReservaEvento de(Reserva r, String tipo, String clienteEmail) {
        return new ReservaEvento(
                UUID.randomUUID().toString(),
                tipo,
                r.getId(),
                r.getClienteId(),
                r.getClienteNombre(),
                clienteEmail,
                r.getVehiculoId(),
                r.getVehiculoDescripcion(),
                r.getMontoAnticipo(),
                r.getFechaVencimiento(),
                r.getEstado().name(),
                r.getMotivoAnulacion(),
                LocalDateTime.now());
    }

    @Override
    public String routingKey() {
        return "reserva." + tipo;
    }
}
