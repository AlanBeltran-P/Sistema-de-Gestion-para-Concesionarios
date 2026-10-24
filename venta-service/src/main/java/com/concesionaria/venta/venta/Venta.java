package com.concesionaria.venta.venta;

import com.concesionaria.venta.comision.Comision;
import com.concesionaria.venta.cotizacion.Cotizacion;
import com.concesionaria.venta.reserva.Reserva;
import com.concesionaria.venta.tasacion.TasacionTradeIn;
import com.concesionaria.venta.venta.enums.EstadoVenta;
import com.concesionaria.venta.venta.enums.FormaPago;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Venta de contado o financiada, con o sin trade-in. RF-23 a RF-27.
 * Si es FINANCIADA, financiamiento-service genera el cronograma de cuotas
 * a partir de planFinanciamientoId, cantidadCuotas y montoFinanciado (RF-30).
 */
@Entity
@Table(name = "ventas", indexes = {
        @Index(name = "idx_venta_vendedor_fecha", columnList = "vendedorId, fechaVenta"),
        @Index(name = "idx_venta_cliente", columnList = "clienteId"),
        @Index(name = "idx_venta_vehiculo", columnList = "vehiculoId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número que se imprime en el contrato o comprobante PDF (RF-27). */
    @Column(nullable = false, unique = true, length = 30)
    private String numeroComprobante;

    @Column(nullable = false)
    private Long clienteId;

    @Column(length = 160)
    private String clienteNombre;

    @Column(nullable = false)
    private Long vehiculoId;

    @Column(length = 200)
    private String vehiculoDescripcion;

    @Column(nullable = false)
    private Long vendedorId;

    @Column(length = 160)
    private String vendedorNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", unique = true)
    private Reserva reserva;

    /** Si no es null, la venta es con trade-in. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasacion_id", unique = true)
    private TasacionTradeIn tasacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private FormaPago formaPago;

    // ---- Montos ----

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioVehiculo;

    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    /** precioVehiculo - descuento. Es la base para la comisión. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioFinal;

    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTradeIn = BigDecimal.ZERO;

    /** Seña de la reserva, que se descuenta del total. */
    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoAnticipoReserva = BigDecimal.ZERO;

    /** precioFinal - valorTradeIn - montoAnticipoReserva. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoAPagar;

    // ---- Datos de financiación (solo si formaPago = FINANCIADA) ----

    /** Id del plan en financiamiento-service. */
    private Long planFinanciamientoId;

    @Column(length = 80)
    private String planNombre;

    private Integer cantidadCuotas;

    @Column(precision = 14, scale = 2)
    private BigDecimal montoFinanciado;

    /** Valor de cada cuota según la simulación de financiamiento-service. */
    @Column(precision = 14, scale = 2)
    private BigDecimal valorCuota;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoVenta estado = EstadoVenta.CONFIRMADA;

    @OneToOne(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Comision comision;

    @Column(length = 500)
    private String motivoAnulacion;

    private LocalDateTime fechaAnulacion;

    /** Id del ADMIN que la anuló (RF-26). */
    private Long anuladaPorId;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaVenta;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public void asignarComision(Comision comision) {
        this.comision = comision;
        if (comision != null) {
            comision.setVenta(this);
        }
    }
}
