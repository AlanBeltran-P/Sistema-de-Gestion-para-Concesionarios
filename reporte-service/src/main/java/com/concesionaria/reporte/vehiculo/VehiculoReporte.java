package com.concesionaria.reporte.vehiculo;

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
 * Copia del inventario armada con los eventos de vehiculo-service.
 * Reporte por estado y antigüedad en stock (RF-40): antigüedad = hoy - fechaIngreso.
 */
@Entity
@Table(name = "vehiculos_reporte",
        indexes = @Index(name = "idx_vhr_estado", columnList = "estado"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long vehiculoId;

    @Column(length = 17)
    private String vin;

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

    /** DISPONIBLE, RESERVADO o VENDIDO. */
    @Column(nullable = false, length = 15)
    private String estado;

    @Column(precision = 14, scale = 2)
    private BigDecimal precio;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaIngreso;

    private LocalDateTime fechaVenta;
}
