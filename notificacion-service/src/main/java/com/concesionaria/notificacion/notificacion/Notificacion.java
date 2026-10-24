package com.concesionaria.notificacion.notificacion;

import com.concesionaria.notificacion.notificacion.enums.EstadoNotificacion;
import com.concesionaria.notificacion.notificacion.enums.TipoNotificacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Historial de emails enviados (RF-38). Se crea uno por cada evento recibido de RabbitMQ. */
@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notificacion_destinatario", columnList = "destinatario"),
        @Index(name = "idx_notificacion_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Id único del evento recibido. RabbitMQ puede entregar el mismo mensaje dos veces;
     * con esto se evita mandar el email repetido.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String eventoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacion tipo;

    /** Email del destinatario. */
    @Column(nullable = false, length = 150)
    private String destinatario;

    /** Id del usuario o cliente destinatario, si se conoce. */
    private Long usuarioId;

    @Column(nullable = false, length = 200)
    private String asunto;

    /** Cuerpo del email (HTML). El length hace que en MySQL sea TEXT y no TINYTEXT (255 bytes). */
    @Lob
    @Column(nullable = false, length = 65_535)
    private String cuerpo;

    /** Id de la entidad relacionada (cotización, reserva, venta o cuota). */
    private Long referenciaId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    @Builder.Default
    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(length = 1000)
    private String ultimoError;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaEnvio;
}
