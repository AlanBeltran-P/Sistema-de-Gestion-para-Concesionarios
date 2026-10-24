package com.concesionaria.financiamiento.evento;

import com.concesionaria.common.evento.EventoIntegracion;
import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.financiamiento.Financiamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Foto de una cuota después de cada cambio.
 * - cuota.actualizada: se generó, se pagó, cambió la mora o se anuló. reporte-service la usa
 *   para el reporte de cartera (RF-41).
 * - cuota.por-vencer y cuota.vencida: notificacion-service manda el aviso al cliente (RF-37).
 * reporte-service escucha "cuota.*" porque todas traen la foto completa.
 */
public record CuotaEvento(
        String eventoId,
        String tipo,
        Long cuotaId,
        Long financiamientoId,
        Long ventaId,
        String numeroComprobante,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        Integer numero,
        Integer cantidadCuotas,
        BigDecimal monto,
        BigDecimal mora,
        BigDecimal montoPagado,
        BigDecimal saldo,
        String estado,
        LocalDate fechaVencimiento,
        LocalDate fechaPago,
        LocalDateTime fecha
) implements EventoIntegracion {

    public static CuotaEvento actualizada(Cuota c) {
        return de(c, "actualizada");
    }

    public static CuotaEvento porVencer(Cuota c) {
        return de(c, "por-vencer");
    }

    public static CuotaEvento vencida(Cuota c) {
        return de(c, "vencida");
    }

    private static CuotaEvento de(Cuota c, String tipo) {
        Financiamiento f = c.getFinanciamiento();
        return new CuotaEvento(
                UUID.randomUUID().toString(),
                tipo,
                c.getId(),
                f.getId(),
                f.getVentaId(),
                f.getNumeroComprobante(),
                f.getClienteId(),
                f.getClienteNombre(),
                f.getClienteEmail(),
                c.getNumero(),
                f.getCantidadCuotas(),
                c.getMonto(),
                c.getMora(),
                c.getMontoPagado(),
                c.getMonto().add(c.getMora()).subtract(c.getMontoPagado()).max(BigDecimal.ZERO),
                c.getEstado().name(),
                c.getFechaVencimiento(),
                c.getFechaPago(),
                LocalDateTime.now());
    }

    @Override
    public String routingKey() {
        return "cuota." + tipo;
    }
}
