package com.concesionaria.venta.comision.repository;

import com.concesionaria.venta.comision.Comision;
import com.concesionaria.venta.comision.dto.TotalesComision;
import com.concesionaria.venta.comision.enums.EstadoComision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ComisionRepository extends JpaRepository<Comision, Long> {

    @EntityGraph(attributePaths = "venta")
    Optional<Comision> findWithVentaById(Long id);

    @EntityGraph(attributePaths = "venta")
    @Query("""
            select c from Comision c
            where (:vendedorId is null or c.vendedorId = :vendedorId)
              and (:estado is null or c.estado = :estado)
              and (:desde is null or c.fechaCalculo >= :desde)
              and (:hasta is null or c.fechaCalculo < :hasta)
            """)
    Page<Comision> buscar(@Param("vendedorId") Long vendedorId,
                          @Param("estado") EstadoComision estado,
                          @Param("desde") LocalDateTime desde,
                          @Param("hasta") LocalDateTime hasta,
                          Pageable pageable);

    /** Sin contar las anuladas. */
    @Query("""
            select new com.concesionaria.venta.comision.dto.TotalesComision(count(c),
                   coalesce(sum(case when c.estado = com.concesionaria.venta.comision.enums.EstadoComision.PENDIENTE
                                     then c.monto else 0 end), 0),
                   coalesce(sum(case when c.estado = com.concesionaria.venta.comision.enums.EstadoComision.PAGADA
                                     then c.monto else 0 end), 0))
            from Comision c
            where c.estado <> com.concesionaria.venta.comision.enums.EstadoComision.ANULADA
              and (:vendedorId is null or c.vendedorId = :vendedorId)
              and (:desde is null or c.fechaCalculo >= :desde)
              and (:hasta is null or c.fechaCalculo < :hasta)
            """)
    TotalesComision totales(@Param("vendedorId") Long vendedorId,
                     @Param("desde") LocalDateTime desde,
                     @Param("hasta") LocalDateTime hasta);
}
