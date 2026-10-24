package com.concesionaria.venta.venta.repository;

import com.concesionaria.venta.venta.Venta;
import com.concesionaria.venta.venta.enums.EstadoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByVehiculoIdAndEstado(Long vehiculoId, EstadoVenta estado);

    @EntityGraph(attributePaths = {"comision", "tasacion", "reserva", "cotizacion"})
    Optional<Venta> findDetalleById(Long id);

    @EntityGraph(attributePaths = {"comision", "tasacion"})
    @Query("""
            select v from Venta v
            where (:clienteId is null or v.clienteId = :clienteId)
              and (:vendedorId is null or v.vendedorId = :vendedorId)
              and (:estado is null or v.estado = :estado)
              and (:desde is null or v.fechaVenta >= :desde)
              and (:hasta is null or v.fechaVenta < :hasta)
            """)
    Page<Venta> buscar(@Param("clienteId") Long clienteId,
                       @Param("vendedorId") Long vendedorId,
                       @Param("estado") EstadoVenta estado,
                       @Param("desde") LocalDateTime desde,
                       @Param("hasta") LocalDateTime hasta,
                       Pageable pageable);
}
