package com.concesionaria.vehiculo.vehiculo;

import com.concesionaria.vehiculo.imagen.ImagenVehiculo;
import com.concesionaria.vehiculo.modelo.Modelo;
import com.concesionaria.vehiculo.vehiculo.enums.CondicionVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.OrigenVehiculo;
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
import java.util.ArrayList;
import java.util.List;

/** RF-08 a RF-13. La marca y la categoría se obtienen a través del modelo. */
@Entity
@Table(name = "vehiculos", indexes = {
        @Index(name = "idx_vehiculo_estado", columnList = "estado"),
        @Index(name = "idx_vehiculo_precio", columnList = "precio"),
        @Index(name = "idx_vehiculo_anio", columnList = "anio")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private Modelo modelo;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(nullable = false)
    private Integer kilometraje;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CondicionVehiculo condicion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigenVehiculo origen = OrigenVehiculo.COMPRA;

    @Column(length = 1000)
    private String descripcion;

    /** Baja lógica (RF-09): el vehículo deja de mostrarse pero no se borra. */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    private LocalDateTime fechaBaja;

    @Builder.Default
    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ImagenVehiculo> imagenes = new ArrayList<>();

    /** Evita que dos usuarios reserven o vendan el mismo vehículo al mismo tiempo. */
    @Version
    private Long version;

    /** Fecha de ingreso al stock; sirve para el reporte de antigüedad (RF-40). */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaIngreso;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public void agregarImagen(ImagenVehiculo imagen) {
        imagenes.add(imagen);
        imagen.setVehiculo(this);
    }

    public void quitarImagen(ImagenVehiculo imagen) {
        imagenes.remove(imagen);
        imagen.setVehiculo(null);
    }
}
