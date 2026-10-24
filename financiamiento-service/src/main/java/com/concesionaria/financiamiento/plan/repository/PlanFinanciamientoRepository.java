package com.concesionaria.financiamiento.plan.repository;

import com.concesionaria.financiamiento.plan.PlanFinanciamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanFinanciamientoRepository extends JpaRepository<PlanFinanciamiento, Long> {

    List<PlanFinanciamiento> findAllByOrderByNombreAsc();

    List<PlanFinanciamiento> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
