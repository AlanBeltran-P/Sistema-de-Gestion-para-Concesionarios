package com.concesionaria.reporte.evento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Copias (con los campos que se usan acá) de los eventos de los otros servicios. */
public final class Eventos {

    private Eventos() {
    }

    /** vehiculo-service: vehiculo.* (foto completa del vehículo). */
    public record Vehiculo(String eventoId, String tipo, Long vehiculoId, String vin, Long modeloId, String marca,
                           String modelo, String categoria, Integer anio, String condicion, String estado,
                           BigDecimal precio, boolean activo, LocalDateTime fechaIngreso, LocalDateTime fecha) {
    }

    /** venta-service: venta.confirmada y venta.anulada. */
    public record Venta(String eventoId, String tipo, Long ventaId, String numeroComprobante, Long clienteId,
                        Long vendedorId, String vendedorNombre, Long vehiculoId, Long modeloId, String marca,
                        String modelo, String categoria, Integer anio, String condicion, String formaPago,
                        boolean conTradeIn, BigDecimal precioFinal, BigDecimal montoComision, String estado,
                        LocalDateTime fechaVenta, LocalDateTime fechaAnulacion, LocalDateTime fecha) {
    }

    /** financiamiento-service: cuota.* (foto completa de la cuota). */
    public record Cuota(String eventoId, String tipo, Long cuotaId, Long financiamientoId, Long ventaId,
                        Long clienteId, String clienteNombre, Integer numero, BigDecimal monto, BigDecimal mora,
                        BigDecimal montoPagado, BigDecimal saldo, String estado, LocalDate fechaVencimiento,
                        LocalDate fechaPago, LocalDateTime fecha) {
    }
}
