package com.concesionaria.cliente.cliente;

import com.concesionaria.cliente.cliente.enums.OrigenCliente;
import com.concesionaria.cliente.cliente.enums.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * RF-14 a RF-17. El historial (RF-17) no se guarda acá: se arma consultando
 * venta-service y financiamiento-service con el id del cliente.
 */
@Entity
@Table(name = "clientes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_documento", "numero_documento"}),
        indexes = @Index(name = "idx_cliente_apellido", columnList = "apellido"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id del usuario en auth-service. Es null si el cliente se cargó en el local sin cuenta. */
    @Column(unique = true)
    private Long usuarioId;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    /** Puede quedar vacío hasta que el cliente complete su perfil (RF-15). */
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private TipoDocumento tipoDocumento;

    @Column(length = 20)
    private String numeroDocumento;

    @Column(unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Embedded
    private Direccion direccion;

    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigenCliente origen;

    /** Id del vendedor (auth-service) que lo registró en el local. */
    private Long registradoPorId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
