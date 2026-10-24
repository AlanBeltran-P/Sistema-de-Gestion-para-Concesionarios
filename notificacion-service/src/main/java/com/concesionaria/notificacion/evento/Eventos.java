package com.concesionaria.notificacion.evento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Copias (solo con los campos que se usan para armar los emails) de los eventos que publican
 * los otros servicios. Los campos que no están acá se ignoran al leer el JSON.
 */
public final class Eventos {

    private Eventos() {
    }

    /** auth-service: usuario.registrado */
    public record UsuarioRegistrado(String eventoId, Long usuarioId, String email, String nombre, String apellido,
                                    String rol) {
    }

    /** auth-service: usuario.recuperacion-password */
    public record RecuperacionPassword(String eventoId, Long usuarioId, String email, String nombre, String token,
                                       LocalDateTime fechaExpiracion) {
    }

    /** venta-service: cotizacion.respondida */
    public record CotizacionRespondida(String eventoId, Long cotizacionId, Long clienteId, String clienteNombre,
                                       String clienteEmail, String vehiculoDescripcion, String vendedorNombre,
                                       BigDecimal precioLista, BigDecimal descuento, BigDecimal valorTradeIn,
                                       BigDecimal precioFinal, String respuestaVendedor, LocalDate fechaValidez) {
    }

    /** venta-service: reserva.creada */
    public record ReservaCreada(String eventoId, Long reservaId, Long clienteId, String clienteNombre,
                                String clienteEmail, String vehiculoDescripcion, BigDecimal montoAnticipo,
                                LocalDateTime fechaVencimiento) {
    }

    /** venta-service: venta.confirmada */
    public record VentaConfirmada(String eventoId, Long ventaId, String numeroComprobante, Long clienteId,
                                  String clienteNombre, String clienteEmail, String vendedorNombre, String marca,
                                  String modelo, Integer anio, String formaPago, BigDecimal precioFinal,
                                  BigDecimal valorTradeIn, BigDecimal montoAnticipoReserva, BigDecimal saldoAPagar,
                                  String planNombre, Integer cantidadCuotas, BigDecimal montoFinanciado,
                                  BigDecimal valorCuota) {
    }

    /** financiamiento-service: cuota.por-vencer y cuota.vencida */
    public record Cuota(String eventoId, String tipo, Long cuotaId, Long financiamientoId, String numeroComprobante,
                        Long clienteId, String clienteNombre, String clienteEmail, Integer numero,
                        Integer cantidadCuotas, BigDecimal monto, BigDecimal mora, BigDecimal saldo,
                        LocalDate fechaVencimiento) {
    }
}
