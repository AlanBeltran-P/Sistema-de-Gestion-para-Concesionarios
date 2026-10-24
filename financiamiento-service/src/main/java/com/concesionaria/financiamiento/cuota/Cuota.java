package com.concesionaria.financiamiento.cuota;

import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Una cuota del cronograma. RF-30 a RF-33, RF-37. */
@Entity
@Table(name = "cuotas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"financiamiento_id", "numero"}),
        // El job diario (RF-33, RF-37) busca cuotas por estado y fecha de vencimiento
        indexes = @Index(name = "idx_cuota_estado_vencimiento", columnList = "estado, fechaVencimiento"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financiamiento_id", nullable = false)
    private Financiamiento financiamiento;

    /** 1, 2, 3, ... */
    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal capital;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal interes;

    /** capital + interes. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    /** Mora acumulada por atraso (RF-33). */
    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal mora = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    /** Fecha en que se terminó de pagar. */
    private LocalDate fechaPago;

    /** Evita mandar dos veces el recordatorio de "cuota próxima a vencer" (RF-37). */
    @Builder.Default
    @Column(nullable = false)
    private boolean recordatorioEnviado = false;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
