package com.concesionaria.financiamiento.cuota.dto;

import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * @param saldo      lo que falta pagar (monto + mora - pagado)
 * @param diasAtraso días desde el vencimiento, si está vencida
 */
public record CuotaResponse(
        Long id,
        Long financiamientoId,
        Long clienteId,
        String clienteNombre,
        Integer numero,
        LocalDate fechaVencimiento,
        BigDecimal capital,
        BigDecimal interes,
        BigDecimal monto,
        BigDecimal mora,
        BigDecimal montoPagado,
        BigDecimal saldo,
        EstadoCuota estado,
        LocalDate fechaPago,
        long diasAtraso
) {

    public static CuotaResponse desde(Cuota c) {
        var f = c.getFinanciamiento();
        long atraso = c.getEstado() == EstadoCuota.VENCIDA
                ? Math.max(0, ChronoUnit.DAYS.between(c.getFechaVencimiento(), LocalDate.now()))
                : 0;
        return new CuotaResponse(
                c.getId(),
                f.getId(),
                f.getClienteId(),
                f.getClienteNombre(),
                c.getNumero(),
                c.getFechaVencimiento(),
                c.getCapital(),
                c.getInteres(),
                c.getMonto(),
                c.getMora(),
                c.getMontoPagado(),
                saldo(c),
                c.getEstado(),
                c.getFechaPago(),
                atraso);
    }

    public static BigDecimal saldo(Cuota c) {
        if (!c.getEstado().impaga()) {
            return BigDecimal.ZERO;
        }
        return c.getMonto().add(c.getMora()).subtract(c.getMontoPagado()).max(BigDecimal.ZERO);
    }
}
