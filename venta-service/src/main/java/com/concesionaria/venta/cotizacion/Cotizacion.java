package com.concesionaria.venta.cotizacion;

import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;
import com.concesionaria.venta.tasacion.TasacionTradeIn;
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

/**
 * RF-18, RF-19. clienteId, vehiculoId y vendedorId son ids de otros microservicios
 * (cliente-service, vehiculo-service y auth-service), por eso no son relaciones JPA.
 */
@Entity
@Table(name = "cotizaciones", indexes = {
        @Index(name = "idx_cotizacion_cliente", columnList = "clienteId"),
        @Index(name = "idx_cotizacion_vendedor", columnList = "vendedorId"),
        @Index(name = "idx_cotizacion_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long vehiculoId;

    /** Null mientras ningún vendedor la tomó (cotización pedida desde el portal). */
    private Long vendedorId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCotizacion estado = EstadoCotizacion.SOLICITADA;

    /** Precio del vehículo al momento de cotizar. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioLista;

    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    /** Valor de la tasación descontado, si hay trade-in. */
    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTradeIn = BigDecimal.ZERO;

    /** precioLista - descuento - valorTradeIn. */
    @Column(precision = 14, scale = 2)
    private BigDecimal precioFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasacion_id")
    private TasacionTradeIn tasacion;

    @Column(length = 1000)
    private String mensajeCliente;

    @Column(length = 1000)
    private String respuestaVendedor;

    private LocalDateTime fechaRespuesta;

    /** Hasta cuándo es válido el precio cotizado. */
    private LocalDate fechaValidez;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
