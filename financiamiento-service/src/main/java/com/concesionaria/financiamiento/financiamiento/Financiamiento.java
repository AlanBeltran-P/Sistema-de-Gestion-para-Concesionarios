package com.concesionaria.financiamiento.financiamiento;

import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;
import com.concesionaria.financiamiento.pago.Pago;
import com.concesionaria.financiamiento.plan.PlanFinanciamiento;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Crédito generado al confirmar una venta financiada (RF-30).
 * Se copian la tasa y las condiciones del plan para que no cambien si el plan se edita.
 */
@Entity
@Table(name = "financiamientos",
        indexes = @Index(name = "idx_financiamiento_cliente", columnList = "clienteId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Financiamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id de la venta en venta-service. Una venta tiene como máximo un financiamiento. */
    @Column(nullable = false, unique = true)
    private Long ventaId;

    @Column(nullable = false)
    private Long clienteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanFinanciamiento plan;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoAnticipo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoFinanciado;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal tasaAnualAplicada;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal porcentajeMoraDiariaAplicado;

    @Column(nullable = false)
    private Integer cantidadCuotas;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorCuota;

    @Column(nullable = false)
    private LocalDate fechaPrimerVencimiento;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoFinanciamiento estado = EstadoFinanciamiento.ACTIVO;

    @Builder.Default
    @OneToMany(mappedBy = "financiamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    private List<Cuota> cuotas = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "financiamiento", cascade = CascadeType.ALL)
    @OrderBy("fechaPago ASC")
    private List<Pago> pagos = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public void agregarCuota(Cuota cuota) {
        cuotas.add(cuota);
        cuota.setFinanciamiento(this);
    }

    public void agregarPago(Pago pago) {
        pagos.add(pago);
        pago.setFinanciamiento(this);
    }
}
