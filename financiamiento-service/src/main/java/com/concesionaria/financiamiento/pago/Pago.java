package com.concesionaria.financiamiento.pago;

import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import com.concesionaria.financiamiento.pago.enums.MedioPago;
import com.concesionaria.financiamiento.pago.enums.TipoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago del anticipo o de una cuota. RF-31. Si es del anticipo, cuota queda en null. */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financiamiento_id", nullable = false)
    private Financiamiento financiamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id")
    private Cuota cuota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoPago tipo;

    /** Total cobrado, incluida la mora. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    /** Parte del monto que corresponde a mora. */
    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoMora = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedioPago medioPago;

    /** Nro. de transferencia, de cheque, etc. */
    @Column(length = 100)
    private String referencia;

    /** Id del vendedor (auth-service) que registró el pago. */
    @Column(nullable = false)
    private Long registradoPorId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPago;
}
