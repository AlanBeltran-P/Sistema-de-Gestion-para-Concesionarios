package com.concesionaria.reporte.cuota.repository;

import com.concesionaria.reporte.cuota.CuotaReporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuotaReporteRepository extends JpaRepository<CuotaReporte, Long> {

    Optional<CuotaReporte> findByCuotaId(Long cuotaId);

    List<CuotaReporte> findByEstadoNot(String estado);
}
