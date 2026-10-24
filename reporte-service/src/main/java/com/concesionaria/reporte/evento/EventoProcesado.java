package com.concesionaria.reporte.evento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de eventos de RabbitMQ ya aplicados. Si llega un evento repetido,
 * se ignora para no contar dos veces la misma venta o el mismo pago.
 */
@Entity
@Table(name = "eventos_procesados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoProcesado {

    @Id
    @Column(length = 64)
    private String eventoId;

    @Column(nullable = false, length = 60)
    private String tipo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaProcesado;
}
