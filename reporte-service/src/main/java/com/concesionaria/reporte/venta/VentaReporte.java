package com.concesionaria.reporte.venta;

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
import java.time.LocalDateTime;

/**
 * Copia de cada venta armada con los eventos de venta-service. Incluye los nombres
 * (vendedor, marca, modelo) para no tener que consultar otros servicios al armar
 * los reportes. RF-39 y RF-42.
 */
@Entity
@Table(name = "ventas_reporte", indexes = {
        @Index(name = "idx_vr_fecha", columnList = "fechaVenta"),
        @Index(name = "idx_vr_vendedor_fecha", columnList = "vendedorId, fechaVenta"),
        @Index(name = "idx_vr_modelo", columnList = "modeloId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long ventaId;

    @Column(length = 30)
    private String numeroComprobante;

    @Column(nullable = false)
    private Long vendedorId;

    @Column(length = 160)
    private String vendedorNombre;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long vehiculoId;

    private Long modeloId;

    @Column(length = 60)
    private String marca;

    @Column(length = 80)
    private String modelo;

    @Column(length = 60)
    private String categoria;

    private Integer anio;

    /** NUEVO o USADO. */
    @Column(length = 10)
    private String condicion;

    /** CONTADO o FINANCIADA. */
    @Column(length = 15)
    private String formaPago;

    @Builder.Default
    @Column(nullable = false)
    private boolean conTradeIn = false;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioFinal;

    @Builder.Default
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoComision = BigDecimal.ZERO;

    /** CONFIRMADA o ANULADA. Las anuladas no suman en los KPIs. */
    @Column(nullable = false, length = 15)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fechaVenta;

    private LocalDateTime fechaAnulacion;
}
