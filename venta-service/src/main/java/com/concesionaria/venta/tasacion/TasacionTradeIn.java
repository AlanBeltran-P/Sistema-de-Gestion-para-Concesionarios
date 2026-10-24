package com.concesionaria.venta.tasacion;

import com.concesionaria.venta.tasacion.enums.EstadoTasacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tasación del auto usado que el cliente entrega como parte de pago (RF-20).
 * Los datos del auto se copian acá porque todavía no está en el inventario;
 * al concretarse la venta se da de alta en vehiculo-service (RF-24).
 */
@Entity
@Table(name = "tasaciones_trade_in",
        indexes = @Index(name = "idx_tasacion_cliente", columnList = "clienteId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TasacionTradeIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long vendedorId;

    @Column(nullable = false, length = 17)
    private String vin;

    /** Id del modelo en vehiculo-service, necesario para darlo de alta en el inventario. */
    @Column(nullable = false)
    private Long modeloId;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(nullable = false)
    private Integer kilometraje;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTasado;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoTasacion estado = EstadoTasacion.PENDIENTE;

    /** La tasación tiene validez limitada porque el valor del auto cambia. */
    private LocalDate fechaValidez;

    /** Id del vehículo creado en vehiculo-service cuando ingresa al inventario (RF-24). */
    private Long vehiculoIngresadoId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaTasacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
