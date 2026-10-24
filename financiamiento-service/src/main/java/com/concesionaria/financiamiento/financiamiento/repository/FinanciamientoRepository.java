package com.concesionaria.financiamiento.financiamiento.repository;

import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FinanciamientoRepository extends JpaRepository<Financiamiento, Long> {

    boolean existsByVentaId(Long ventaId);

    @EntityGraph(attributePaths = "plan")
    Optional<Financiamiento> findByVentaId(Long ventaId);

    @EntityGraph(attributePaths = "plan")
    Optional<Financiamiento> findWithPlanById(Long id);

    @EntityGraph(attributePaths = "plan")
    List<Financiamiento> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    @EntityGraph(attributePaths = "plan")
    @Query("""
            select f from Financiamiento f
            where (:clienteId is null or f.clienteId = :clienteId)
              and (:estado is null or f.estado = :estado)
            """)
    Page<Financiamiento> buscar(@Param("clienteId") Long clienteId,
                                @Param("estado") EstadoFinanciamiento estado,
                                Pageable pageable);
}
