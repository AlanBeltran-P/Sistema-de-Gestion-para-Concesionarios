package com.concesionaria.financiamiento.cuota.repository;

import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    /** RF-33: cuotas impagas cuya fecha ya pasó, de créditos vigentes. */
    @EntityGraph(attributePaths = {"financiamiento", "financiamiento.plan"})
    @Query("""
            select c from Cuota c
            where c.estado in :estados
              and c.fechaVencimiento < :hoy
              and c.financiamiento.estado in :estadosFinanciamiento
            """)
    List<Cuota> findVencidasAl(@Param("estados") Collection<EstadoCuota> estados,
                               @Param("hoy") LocalDate hoy,
                               @Param("estadosFinanciamiento") Collection<EstadoFinanciamiento> estadosFinanciamiento);

    /** RF-37: cuotas que vencen entre hoy y hasta, sin recordatorio enviado. */
    @EntityGraph(attributePaths = "financiamiento")
    @Query("""
            select c from Cuota c
            where c.estado in :estados
              and c.recordatorioEnviado = false
              and c.fechaVencimiento between :hoy and :hasta
              and c.financiamiento.estado in :estadosFinanciamiento
            """)
    List<Cuota> findPorVencer(@Param("estados") Collection<EstadoCuota> estados,
                              @Param("hoy") LocalDate hoy,
                              @Param("hasta") LocalDate hasta,
                              @Param("estadosFinanciamiento") Collection<EstadoFinanciamiento> estadosFinanciamiento);

    /** RF-32: listado de cuotas para los vendedores (ej: todas las vencidas). */
    @EntityGraph(attributePaths = "financiamiento")
    @Query("""
            select c from Cuota c
            where (:estado is null or c.estado = :estado)
              and (:clienteId is null or c.financiamiento.clienteId = :clienteId)
              and (:financiamientoId is null or c.financiamiento.id = :financiamientoId)
            """)
    Page<Cuota> buscar(@Param("estado") EstadoCuota estado,
                       @Param("clienteId") Long clienteId,
                       @Param("financiamientoId") Long financiamientoId,
                       Pageable pageable);
}
