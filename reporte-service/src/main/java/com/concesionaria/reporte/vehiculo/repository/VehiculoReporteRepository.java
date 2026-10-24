package com.concesionaria.reporte.vehiculo.repository;

import com.concesionaria.reporte.vehiculo.VehiculoReporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculoReporteRepository extends JpaRepository<VehiculoReporte, Long> {

    Optional<VehiculoReporte> findByVehiculoId(Long vehiculoId);

    List<VehiculoReporte> findByActivoTrue();

    long countByActivoTrueAndEstado(String estado);
}
