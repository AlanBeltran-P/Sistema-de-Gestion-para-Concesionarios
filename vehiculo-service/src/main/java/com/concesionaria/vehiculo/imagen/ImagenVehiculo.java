package com.concesionaria.vehiculo.imagen;

import com.concesionaria.vehiculo.vehiculo.Vehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

/** Se guarda la URL o ruta del archivo, no la imagen en la base. RF-10. */
@Entity
@Table(name = "imagenes_vehiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String nombreArchivo;

    @Builder.Default
    @Column(nullable = false)
    private Integer orden = 0;

    /** Imagen que se muestra en el catálogo. */
    @Builder.Default
    @Column(nullable = false)
    private boolean principal = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSubida;
}
