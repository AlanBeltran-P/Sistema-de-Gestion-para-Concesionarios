package com.concesionaria.venta.reserva.repository;

import com.concesionaria.venta.reserva.Reserva;
import com.concesionaria.venta.reserva.enums.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findFirstByVehiculoIdAndEstado(Long vehiculoId, EstadoReserva estado);

    /** RF-22: ids de las reservas activas que ya vencieron (se procesan de a una). */
    @Query("select r.id from Reserva r where r.estado = :estado and r.fechaVencimiento < :ahora order by r.fechaVencimiento")
    List<Long> findIdsVencidas(@Param("estado") EstadoReserva estado, @Param("ahora") LocalDateTime ahora);

    @Query("""
            select r from Reserva r
            where (:clienteId is null or r.clienteId = :clienteId)
              and (:vehiculoId is null or r.vehiculoId = :vehiculoId)
              and (:estado is null or r.estado = :estado)
            """)
    Page<Reserva> buscar(@Param("clienteId") Long clienteId,
                         @Param("vehiculoId") Long vehiculoId,
                         @Param("estado") EstadoReserva estado,
                         Pageable pageable);
}
