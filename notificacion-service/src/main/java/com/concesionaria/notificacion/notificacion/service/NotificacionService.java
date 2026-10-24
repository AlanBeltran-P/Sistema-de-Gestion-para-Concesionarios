package com.concesionaria.notificacion.notificacion.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.notificacion.config.NotificacionesProperties;
import com.concesionaria.notificacion.notificacion.Notificacion;
import com.concesionaria.notificacion.notificacion.dto.NotificacionResponse;
import com.concesionaria.notificacion.notificacion.enums.EstadoNotificacion;
import com.concesionaria.notificacion.notificacion.enums.TipoNotificacion;
import com.concesionaria.notificacion.notificacion.repository.NotificacionRepository;
import com.concesionaria.notificacion.notificacion.service.PlantillasEmail.Email;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Guarda cada email en el historial (RF-38) y lo envía. Si el SMTP falla, queda FALLIDA y el
 * job lo reintenta. El mismo evento nunca genera dos emails (eventoId único).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    /** El token de recuperación no queda guardado en el historial una vez enviado. */
    private static final Pattern TOKEN = Pattern.compile("token=[A-Za-z0-9_\\-]+");

    private final NotificacionRepository notificacionRepository;
    private final JavaMailSender mailSender;
    private final NotificacionesProperties propiedades;
    private final TransactionTemplate transactionTemplate;

    /**
     * @param referenciaId id de la cotización, reserva, venta o cuota (o del usuario)
     */
    public void notificar(String eventoId, TipoNotificacion tipo, String destinatario, Long usuarioId,
                          Long referenciaId, Email email) {
        if (!StringUtils.hasText(destinatario)) {
            log.warn("Evento {} ({}) sin email de destino: no se notifica", eventoId, tipo);
            return;
        }
        Long id;
        try {
            id = transactionTemplate.execute(estado -> {
                if (notificacionRepository.existsByEventoId(eventoId)) {
                    return null;
                }
                return notificacionRepository.saveAndFlush(Notificacion.builder()
                        .eventoId(eventoId)
                        .tipo(tipo)
                        .destinatario(destinatario.trim())
                        .usuarioId(usuarioId)
                        .referenciaId(referenciaId)
                        .asunto(recortar(email.asunto(), 200))
                        .cuerpo(email.cuerpo())
                        .build()).getId();
            });
        } catch (DataIntegrityViolationException e) {
            id = null; // otro consumidor guardó el mismo evento al mismo tiempo
        }
        if (id == null) {
            log.info("Evento {} repetido: el email ya se había generado", eventoId);
            return;
        }
        enviar(id);
    }

    /** Job: reintenta los emails que fallaron, hasta el máximo de intentos. */
    @Scheduled(cron = "${notificaciones.cron-reintentos:0 */5 * * * *}")
    public void reintentarFallidas() {
        for (Long id : notificacionRepository.findIdsParaReintentar(EstadoNotificacion.FALLIDA, propiedades.maxIntentos())) {
            enviar(id);
        }
    }

    /** RF-38: el ADMIN reenvía un email (por ejemplo, si el cliente dice que no le llegó). */
    public NotificacionResponse reenviar(Long id) {
        Notificacion n = buscar(id);
        if (n.getTipo() == TipoNotificacion.RECUPERACION_PASSWORD && n.getEstado() == EstadoNotificacion.ENVIADA) {
            throw new ConflictoException("El link de recuperación ya no se guarda: el usuario tiene que pedir uno nuevo");
        }
        enviar(id);
        return NotificacionResponse.detalle(buscar(id));
    }

    @Transactional(readOnly = true)
    public Page<NotificacionResponse> buscar(TipoNotificacion tipo, EstadoNotificacion estado, String destinatario,
                                             Long referenciaId, Pageable pageable) {
        return notificacionRepository.buscar(tipo, estado, StringUtils.hasText(destinatario) ? destinatario.trim() : null,
                referenciaId, pageable).map(NotificacionResponse::resumen);
    }

    @Transactional(readOnly = true)
    public NotificacionResponse obtener(Long id) {
        return NotificacionResponse.detalle(buscar(id));
    }

    /** Envía fuera de la transacción (el SMTP puede tardar) y después guarda el resultado. */
    private void enviar(Long id) {
        Notificacion n = buscar(id);
        String error = null;
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            helper.setFrom(propiedades.remitente());
            helper.setTo(n.getDestinatario());
            helper.setSubject(n.getAsunto());
            helper.setText(n.getCuerpo(), true);
            mailSender.send(mensaje);
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("No se pudo enviar el email {} a {}: {}", id, n.getDestinatario(), error);
        }
        String errorFinal = error;
        transactionTemplate.executeWithoutResult(estado -> {
            Notificacion actual = buscar(id);
            actual.setIntentos(actual.getIntentos() + 1);
            if (errorFinal == null) {
                actual.setEstado(EstadoNotificacion.ENVIADA);
                actual.setFechaEnvio(LocalDateTime.now());
                actual.setUltimoError(null);
                if (actual.getTipo() == TipoNotificacion.RECUPERACION_PASSWORD) {
                    actual.setCuerpo(TOKEN.matcher(actual.getCuerpo()).replaceAll("token=[oculto]"));
                }
            } else {
                actual.setEstado(EstadoNotificacion.FALLIDA);
                actual.setUltimoError(recortar(errorFinal, 1000));
            }
        });
    }

    private Notificacion buscar(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Notificación " + id + " no encontrada"));
    }

    private static String recortar(String texto, int max) {
        return texto == null || texto.length() <= max ? texto : texto.substring(0, max);
    }
}
