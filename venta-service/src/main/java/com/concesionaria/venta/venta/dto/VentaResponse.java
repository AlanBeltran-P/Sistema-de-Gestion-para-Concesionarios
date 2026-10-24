package com.concesionaria.venta.venta.dto;

import com.concesionaria.venta.comision.enums.EstadoComision;
import com.concesionaria.venta.venta.Venta;
import com.concesionaria.venta.venta.enums.EstadoVenta;
import com.concesionaria.venta.venta.enums.FormaPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @param entregaEfectivo lo que se paga al firmar: saldo - financiado (todo el saldo si es de contado)
 * @param comision        null para el CLIENTE (no le corresponde verla)
 */
public record VentaResponse(
        Long id,
        String numeroComprobante,
        Long clienteId,
        String clienteNombre,
        Long vehiculoId,
        String vehiculo,
        Long vendedorId,
        String vendedorNombre,
        Long cotizacionId,
        Long reservaId,
        Long tasacionId,
        Long vehiculoTradeInId,
        FormaPago formaPago,
        BigDecimal precioVehiculo,
        BigDecimal descuento,
        BigDecimal precioFinal,
        BigDecimal valorTradeIn,
        BigDecimal montoAnticipoReserva,
        BigDecimal saldoAPagar,
        BigDecimal entregaEfectivo,
        Long planFinanciamientoId,
        String planNombre,
        Integer cantidadCuotas,
        BigDecimal montoFinanciado,
        BigDecimal valorCuota,
        Comision comision,
        EstadoVenta estado,
        String motivoAnulacion,
        LocalDateTime fechaVenta,
        LocalDateTime fechaAnulacion
) {

    public record Comision(Long id, BigDecimal porcentaje, BigDecimal monto, EstadoComision estado) {
    }

    public static VentaResponse desde(Venta v, boolean incluirComision) {
        var c = v.getComision();
        var tasacion = v.getTasacion();
        BigDecimal financiado = v.getMontoFinanciado() != null ? v.getMontoFinanciado() : BigDecimal.ZERO;
        return new VentaResponse(
                v.getId(),
                v.getNumeroComprobante(),
                v.getClienteId(),
                v.getClienteNombre(),
                v.getVehiculoId(),
                v.getVehiculoDescripcion(),
                v.getVendedorId(),
                v.getVendedorNombre(),
                v.getCotizacion() != null ? v.getCotizacion().getId() : null,
                v.getReserva() != null ? v.getReserva().getId() : null,
                tasacion != null ? tasacion.getId() : null,
                tasacion != null ? tasacion.getVehiculoIngresadoId() : null,
                v.getFormaPago(),
                v.getPrecioVehiculo(),
                v.getDescuento(),
                v.getPrecioFinal(),
                v.getValorTradeIn(),
                v.getMontoAnticipoReserva(),
                v.getSaldoAPagar(),
                v.getSaldoAPagar().subtract(financiado),
                v.getPlanFinanciamientoId(),
                v.getPlanNombre(),
                v.getCantidadCuotas(),
                v.getMontoFinanciado(),
                v.getValorCuota(),
                incluirComision && c != null ? new Comision(c.getId(), c.getPorcentaje(), c.getMonto(), c.getEstado()) : null,
                v.getEstado(),
                v.getMotivoAnulacion(),
                v.getFechaVenta(),
                v.getFechaAnulacion());
    }
}
