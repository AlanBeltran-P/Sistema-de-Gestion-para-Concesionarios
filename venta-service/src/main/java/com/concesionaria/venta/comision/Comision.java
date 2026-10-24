package com.concesionaria.venta.comision;

import com.concesionaria.venta.comision.enums.EstadoComision;
import com.concesionaria.venta.venta.Venta;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Comisión del vendedor por una venta. RF-25. Se guarda el porcentaje usado por si cambia después. */
@Entity
@Table(name = "comisiones",
        indexes = @Index(name = "idx_comision_vendedor", columnList = "vendedorId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private Venta venta;

    @Column(nullable = false)
    private Long vendedorId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal baseCalculo;

    /** Ej: 2.50 = 2,5 %. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoComision estado = EstadoComision.PENDIENTE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCalculo;
}
