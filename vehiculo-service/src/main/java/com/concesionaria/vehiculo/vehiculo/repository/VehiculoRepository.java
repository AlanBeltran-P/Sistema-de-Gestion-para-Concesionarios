package com.concesionaria.vehiculo.vehiculo.repository;

import com.concesionaria.vehiculo.vehiculo.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long>, JpaSpecificationExecutor<Vehiculo> {

    /** Catálogo: trae modelo, marca y categoría en la misma consulta. Las imágenes se cargan de a lotes. */
    @Override
    @EntityGraph(attributePaths = {"modelo", "modelo.marca", "modelo.categoria"})
    Page<Vehiculo> findAll(Specification<Vehiculo> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"modelo", "modelo.marca", "modelo.categoria", "imagenes"})
    Optional<Vehiculo> findDetalleById(Long id);

    boolean existsByVin(String vin);

    boolean existsByVinAndIdNot(String vin, Long id);
}
