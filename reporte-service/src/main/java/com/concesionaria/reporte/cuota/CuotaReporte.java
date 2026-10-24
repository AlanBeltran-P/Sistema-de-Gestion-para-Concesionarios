package com.concesionaria.reporte.cuota;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Copia de las cuotas armada con los eventos de financiamiento-service.
 * Reporte de cartera: cobradas, pendientes y morosos (RF-41).
 */
@Entity
@Table(name = "cuotas_reporte", indexes = {
        @Index(name = "idx_cr_estado", columnList = "estado"),
        @Index(name = "idx_cr_cliente", columnList = "clienteId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long cuotaId;

    @Column(nullable = false)
    private Long financiamientoId;

    private Long ventaId;

    @Column(nullable = false)
    private Long clienteId;

    @Column(length = 160)
    private String clienteNombre;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal mora = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    /** PENDIENTE, PAGADA_PARCIAL, PAGADA o VENCIDA. */
    @Column(nullable = false, length = 15)
    private String estado;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    private LocalDate fechaPago;
}
