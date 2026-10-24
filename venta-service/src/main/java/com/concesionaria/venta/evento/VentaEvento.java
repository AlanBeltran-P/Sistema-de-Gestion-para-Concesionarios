package com.concesionaria.venta.evento;

import com.concesionaria.common.evento.EventoIntegracion;
import com.concesionaria.venta.venta.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Foto completa de la venta. Routing keys: venta.confirmada y venta.anulada.
 * - financiamiento-service: si es FINANCIADA genera (o anula) el cronograma de cuotas (RF-30).
 * - notificacion-service: email de confirmación de la compra (RF-36).
 * - reporte-service: reportes de ventas, comisiones y KPIs (RF-39, RF-42). Trae los nombres
 *   del vendedor y del vehículo para no tener que consultar otros servicios.
 */
public record VentaEvento(
        String eventoId,
        String tipo,
        Long ventaId,
        String numeroComprobante,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        Long vendedorId,
        String vendedorNombre,
        Long vehiculoId,
        Long modeloId,
        String marca,
        String modelo,
        String categoria,
        Integer anio,
        String condicion,
        String formaPago,
        boolean conTradeIn,
        BigDecimal precioVehiculo,
        BigDecimal descuento,
        BigDecimal precioFinal,
        BigDecimal valorTradeIn,
        BigDecimal montoAnticipoReserva,
        BigDecimal saldoAPagar,
        Long planFinanciamientoId,
        String planNombre,
        Integer cantidadCuotas,
        BigDecimal montoFinanciado,
        BigDecimal valorCuota,
        BigDecimal montoComision,
        String estado,
        LocalDateTime fechaVenta,
        LocalDateTime fechaAnulacion,
        LocalDateTime fecha
) implements EventoIntegracion {

    /** Datos del vehículo vendido que no se guardan en la venta. */
    public record DatosVehiculo(Long modeloId, String marca, String modelo, String categoria, Integer anio,
                                String condicion) {
    }

    public static VentaEvento confirmada(Venta v, String clienteEmail, DatosVehiculo vehiculo) {
        return de(v, "confirmada", clienteEmail, vehiculo);
    }

    public static VentaEvento anulada(Venta v, DatosVehiculo vehiculo) {
        return de(v, "anulada", null, vehiculo);
    }

    private static VentaEvento de(Venta v, String tipo, String clienteEmail, DatosVehiculo d) {
        return new VentaEvento(
                UUID.randomUUID().toString(),
                tipo,
                v.getId(),
                v.getNumeroComprobante(),
                v.getClienteId(),
                v.getClienteNombre(),
                clienteEmail,
                v.getVendedorId(),
                v.getVendedorNombre(),
                v.getVehiculoId(),
                d.modeloId(),
                d.marca(),
                d.modelo(),
                d.categoria(),
                d.anio(),
                d.condicion(),
                v.getFormaPago().name(),
                v.getTasacion() != null,
                v.getPrecioVehiculo(),
                v.getDescuento(),
                v.getPrecioFinal(),
                v.getValorTradeIn(),
                v.getMontoAnticipoReserva(),
                v.getSaldoAPagar(),
                v.getPlanFinanciamientoId(),
                v.getPlanNombre(),
                v.getCantidadCuotas(),
                v.getMontoFinanciado(),
                v.getValorCuota(),
                v.getComision() != null ? v.getComision().getMonto() : BigDecimal.ZERO,
                v.getEstado().name(),
                v.getFechaVenta(),
                v.getFechaAnulacion(),
                LocalDateTime.now());
    }

    @Override
    public String routingKey() {
        return "venta." + tipo;
    }
}
