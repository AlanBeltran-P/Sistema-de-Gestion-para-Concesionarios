package com.concesionaria.notificacion.notificacion.dto;

import com.concesionaria.notificacion.notificacion.Notificacion;
import com.concesionaria.notificacion.notificacion.enums.EstadoNotificacion;
import com.concesionaria.notificacion.notificacion.enums.TipoNotificacion;

import java.time.LocalDateTime;

/** RF-38. El cuerpo solo se incluye en el detalle. */
public record NotificacionResponse(
        Long id,
        TipoNotificacion tipo,
        String destinatario,
        Long usuarioId,
        String asunto,
        String cuerpo,
        Long referenciaId,
        EstadoNotificacion estado,
        Integer intentos,
        String ultimoError,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaEnvio
) {

    public static NotificacionResponse resumen(Notificacion n) {
        return de(n, false);
    }

    public static NotificacionResponse detalle(Notificacion n) {
        return de(n, true);
    }

    private static NotificacionResponse de(Notificacion n, boolean conCuerpo) {
        return new NotificacionResponse(
                n.getId(),
                n.getTipo(),
                n.getDestinatario(),
                n.getUsuarioId(),
                n.getAsunto(),
                conCuerpo ? n.getCuerpo() : null,
                n.getReferenciaId(),
                n.getEstado(),
                n.getIntentos(),
                n.getUltimoError(),
                n.getFechaCreacion(),
                n.getFechaEnvio());
    }
}
