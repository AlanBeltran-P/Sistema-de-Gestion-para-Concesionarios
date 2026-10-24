package com.concesionaria.venta.reserva;

import com.concesionaria.venta.cotizacion.Cotizacion;
import com.concesionaria.venta.reserva.enums.EstadoReserva;
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
import java.time.LocalDateTime;

/** Reserva con anticipo (seña) y fecha de vencimiento. RF-21, RF-22, RF-26. */
@Entity
@Table(name = "reservas", indexes = {
        @Index(name = "idx_reserva_vehiculo", columnList = "vehiculoId"),
        @Index(name = "idx_reserva_cliente", columnList = "clienteId"),
        // El job de vencimiento (RF-22) busca reservas ACTIVAS con fecha vencida
        @Index(name = "idx_reserva_estado_vencimiento", columnList = "estado, fechaVencimiento")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long vehiculoId;

    /** Null si el cliente reservó desde el portal. */
    private Long vendedorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoAnticipo;

    @Column(nullable = false)
    private LocalDateTime fechaVencimiento;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    @Column(length = 500)
    private String motivoAnulacion;

    private LocalDateTime fechaAnulacion;

    /** Id del ADMIN que la anuló. */
    private Long anuladaPorId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaReserva;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
