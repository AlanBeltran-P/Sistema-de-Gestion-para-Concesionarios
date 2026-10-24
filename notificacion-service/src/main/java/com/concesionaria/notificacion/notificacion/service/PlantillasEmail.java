package com.concesionaria.notificacion.notificacion.service;

import com.concesionaria.notificacion.config.NotificacionesProperties;
import com.concesionaria.notificacion.evento.Eventos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Asunto y cuerpo (HTML simple) de cada email. Todos los datos que vienen de los eventos
 * se escapan para que no se pueda inyectar HTML.
 */
@Component
@RequiredArgsConstructor
public class PlantillasEmail {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificacionesProperties propiedades;

    public record Email(String asunto, String cuerpo) {
    }

    /** RF-34 */
    public Email bienvenida(Eventos.UsuarioRegistrado e) {
        String texto = "CLIENTE".equals(e.rol())
                ? "Ya podés entrar al portal para ver el catálogo, pedir cotizaciones y reservar tu próximo auto."
                : "Se creó tu cuenta de " + e.rol().toLowerCase() + ". Ya podés iniciar sesión con este email.";
        return new Email("¡Bienvenido/a a " + propiedades.empresa() + "!",
                plantilla("Hola " + h(e.nombre()) + ",", "<p>" + texto + "</p>"));
    }

    /** RF-34. El link lleva el token: después de enviarlo se oculta en el historial. */
    public Email recuperacionPassword(Eventos.RecuperacionPassword e) {
        String link = UriComponentsBuilder.fromUriString(propiedades.urlFrontend())
                .path("/restablecer-password").queryParam("token", e.token()).build().toUriString();
        return new Email("Recuperá tu contraseña",
                plantilla("Hola " + h(e.nombre()) + ",",
                        "<p>Recibimos un pedido para cambiar tu contraseña. Para elegir una nueva, entrá a este link:</p>"
                                + "<p><a href=\"" + h(link) + "\">" + h(link) + "</a></p>"
                                + "<p>El link vence el " + fechaHora(e.fechaExpiracion()) + ". Si no lo pediste vos, "
                                + "ignorá este email: tu contraseña no cambia.</p>"));
    }

    /** RF-35 */
    public Email cotizacionRespondida(Eventos.CotizacionRespondida e) {
        StringBuilder filas = new StringBuilder()
                .append(fila("Vehículo", h(e.vehiculoDescripcion())))
                .append(fila("Precio de lista", pesos(e.precioLista())));
        if (positivo(e.descuento())) {
            filas.append(fila("Descuento", "- " + pesos(e.descuento())));
        }
        if (positivo(e.valorTradeIn())) {
            filas.append(fila("Tu auto (trade-in)", "- " + pesos(e.valorTradeIn())));
        }
        filas.append(fila("<b>Precio final</b>", "<b>" + pesos(e.precioFinal()) + "</b>"))
                .append(fila("Válida hasta", fecha(e.fechaValidez())));
        return new Email("Respondimos tu cotización por " + e.vehiculoDescripcion(),
                plantilla("Hola " + h(e.clienteNombre()) + ",",
                        "<p>" + h(e.vendedorNombre()) + " respondió tu cotización:</p>"
                                + "<blockquote>" + h(e.respuestaVendedor()) + "</blockquote>"
                                + tabla(filas.toString())
                                + "<p>Podés aceptarla desde el portal o reservar el auto con una seña.</p>"));
    }

    /** RF-36 */
    public Email reservaConfirmada(Eventos.ReservaCreada e) {
        return new Email("Reserva confirmada: " + e.vehiculoDescripcion(),
                plantilla("Hola " + h(e.clienteNombre()) + ",",
                        "<p>Tu reserva N° " + e.reservaId() + " quedó confirmada.</p>"
                                + tabla(fila("Vehículo", h(e.vehiculoDescripcion()))
                                + fila("Seña", pesos(e.montoAnticipo()))
                                + fila("Vence", fechaHora(e.fechaVencimiento())))
                                + "<p>Si no concretás la compra antes del vencimiento, la reserva se libera.</p>"));
    }

    /** RF-36 */
    public Email ventaConfirmada(Eventos.VentaConfirmada e) {
        String vehiculo = e.marca() + " " + e.modelo() + " " + e.anio();
        StringBuilder filas = new StringBuilder()
                .append(fila("Comprobante", h(e.numeroComprobante())))
                .append(fila("Vehículo", h(vehiculo)))
                .append(fila("Precio final", pesos(e.precioFinal())));
        if (positivo(e.valorTradeIn())) {
            filas.append(fila("Trade-in", "- " + pesos(e.valorTradeIn())));
        }
        if (positivo(e.montoAnticipoReserva())) {
            filas.append(fila("Seña", "- " + pesos(e.montoAnticipoReserva())));
        }
        filas.append(fila("Saldo", pesos(e.saldoAPagar())));
        if ("FINANCIADA".equals(e.formaPago())) {
            filas.append(fila("Financiación", h(e.planNombre()) + ": " + e.cantidadCuotas() + " cuotas de "
                    + pesos(e.valorCuota())));
        } else {
            filas.append(fila("Forma de pago", "Contado"));
        }
        return new Email("¡Felicitaciones por tu " + vehiculo + "!",
                plantilla("Hola " + h(e.clienteNombre()) + ",",
                        "<p>Confirmamos tu compra. Te atendió " + h(e.vendedorNombre()) + ".</p>" + tabla(filas.toString())
                                + "<p>Podés descargar el comprobante desde el portal.</p>"));
    }

    /** RF-37 */
    public Email cuotaPorVencer(Eventos.Cuota e) {
        return new Email("Tu cuota " + e.numero() + " vence el " + fecha(e.fechaVencimiento()),
                plantilla("Hola " + h(e.clienteNombre()) + ",",
                        "<p>Te recordamos que se acerca el vencimiento de una cuota de tu financiación.</p>"
                                + tabla(fila("Cuota", e.numero() + " de " + e.cantidadCuotas())
                                + fila("Importe", pesos(e.saldo()))
                                + fila("Vence", fecha(e.fechaVencimiento())))
                                + "<p>Si ya la pagaste, ignorá este mensaje.</p>"));
    }

    /** RF-37 */
    public Email cuotaVencida(Eventos.Cuota e) {
        return new Email("Tu cuota " + e.numero() + " está vencida",
                plantilla("Hola " + h(e.clienteNombre()) + ",",
                        "<p>La cuota " + e.numero() + " de " + e.cantidadCuotas() + " venció el "
                                + fecha(e.fechaVencimiento()) + " y todavía figura impaga.</p>"
                                + tabla(fila("Importe de la cuota", pesos(e.monto()))
                                + fila("Mora", pesos(e.mora()))
                                + fila("<b>Total a pagar hoy</b>", "<b>" + pesos(e.saldo()) + "</b>"))
                                + "<p>La mora aumenta cada día de atraso. Acercate al local para regularizarla.</p>"));
    }

    // ---------------------------------------------------------------- HTML

    private String plantilla(String saludo, String contenido) {
        return """
                <div style="font-family:Arial,sans-serif;font-size:14px;color:#222;max-width:560px">
                <h2 style="color:#1f3a60">%s</h2>
                <p>%s</p>
                %s
                <p style="color:#777;font-size:12px;margin-top:24px">Este email lo envió automáticamente %s. No hace falta responderlo.</p>
                </div>""".formatted(h(propiedades.empresa()), saludo, contenido, h(propiedades.empresa()));
    }

    private static String tabla(String filas) {
        return "<table style=\"border-collapse:collapse;margin:12px 0\">" + filas + "</table>";
    }

    private static String fila(String etiqueta, String valor) {
        return "<tr><td style=\"padding:4px 12px 4px 0;color:#555\">" + etiqueta + "</td><td style=\"padding:4px 0\">"
                + valor + "</td></tr>";
    }

    private static String h(String texto) {
        return texto == null ? "" : HtmlUtils.htmlEscape(texto);
    }

    private static boolean positivo(BigDecimal monto) {
        return monto != null && monto.signum() > 0;
    }

    private static String pesos(BigDecimal monto) {
        return monto == null ? "-" : NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")).format(monto);
    }

    private static String fecha(LocalDate f) {
        return f == null ? "-" : f.format(FECHA);
    }

    private static String fechaHora(LocalDateTime f) {
        return f == null ? "-" : f.format(FECHA_HORA);
    }
}
