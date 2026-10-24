package com.concesionaria.financiamiento.plan;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** RF-28. Lo usan la simulación (RF-29) y la generación del cronograma (RF-30). */
@Entity
@Table(name = "planes_financiamiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFinanciamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /** Tasa nominal anual en %. Ej: 45.00 = 45 %. */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal tasaAnual;

    /** Cantidades de cuotas permitidas, ej: 12, 24, 36, 48. */
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "plan_plazos", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "cantidad_cuotas", nullable = false)
    private Set<Integer> plazos = new HashSet<>();

    /** % mínimo del precio que se paga como anticipo. Ej: 30.00 = 30 %. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAnticipoMinimo;

    /** % de mora por día de atraso sobre la cuota. Ej: 0.1000 = 0,1 % diario (RF-33). */
    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal porcentajeMoraDiaria;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
