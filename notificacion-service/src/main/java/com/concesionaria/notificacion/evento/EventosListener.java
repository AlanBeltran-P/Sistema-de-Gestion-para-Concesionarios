package com.concesionaria.notificacion.evento;

import com.concesionaria.notificacion.config.RabbitConfig;
import com.concesionaria.notificacion.notificacion.enums.TipoNotificacion;
import com.concesionaria.notificacion.notificacion.service.NotificacionService;
import com.concesionaria.notificacion.notificacion.service.PlantillasEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Cada evento recibido se convierte en un email (RF-34 a RF-37). */
@Component
@RequiredArgsConstructor
public class EventosListener {

    private final NotificacionService notificaciones;
    private final PlantillasEmail plantillas;

    @RabbitListener(queues = RabbitConfig.COLA_USUARIO_REGISTRADO)
    public void bienvenida(Eventos.UsuarioRegistrado e) {
        notificaciones.notificar(e.eventoId(), TipoNotificacion.BIENVENIDA, e.email(), e.usuarioId(), e.usuarioId(),
                plantillas.bienvenida(e));
    }

    @RabbitListener(queues = RabbitConfig.COLA_RECUPERACION)
    public void recuperacionPassword(Eventos.RecuperacionPassword e) {
        notificaciones.notificar(e.eventoId(), TipoNotificacion.RECUPERACION_PASSWORD, e.email(), e.usuarioId(),
                e.usuarioId(), plantillas.recuperacionPassword(e));
    }

    @RabbitListener(queues = RabbitConfig.COLA_COTIZACION)
    public void cotizacionRespondida(Eventos.CotizacionRespondida e) {
        notificaciones.notificar(e.eventoId(), TipoNotificacion.COTIZACION_RESPONDIDA, e.clienteEmail(), null,
                e.cotizacionId(), plantillas.cotizacionRespondida(e));
    }

    @RabbitListener(queues = RabbitConfig.COLA_RESERVA)
    public void reservaConfirmada(Eventos.ReservaCreada e) {
        notificaciones.notificar(e.eventoId(), TipoNotificacion.RESERVA_CONFIRMADA, e.clienteEmail(), null,
                e.reservaId(), plantillas.reservaConfirmada(e));
    }

    @RabbitListener(queues = RabbitConfig.COLA_VENTA)
    public void ventaConfirmada(Eventos.VentaConfirmada e) {
        notificaciones.notificar(e.eventoId(), TipoNotificacion.VENTA_CONFIRMADA, e.clienteEmail(), null,
                e.ventaId(), plantillas.ventaConfirmada(e));
    }

    @RabbitListener(queues = RabbitConfig.COLA_CUOTA_POR_VENCER)
    public void cuotaPorVencer(Eventos.Cuota e) {
        notificaciones.notificar(e.eventoId(), TipoNotificacion.CUOTA_POR_VENCER, e.clienteEmail(), null,
                e.cuotaId(), plantillas.cuotaPorVencer(e));
    }

    @RabbitListener(queues = RabbitConfig.COLA_CUOTA_VENCIDA)
    public void cuotaVencida(Eventos.Cuota e) {
        notificaciones.notificar(e.eventoId(), TipoNotificacion.CUOTA_VENCIDA, e.clienteEmail(), null,
                e.cuotaId(), plantillas.cuotaVencida(e));
    }
}
