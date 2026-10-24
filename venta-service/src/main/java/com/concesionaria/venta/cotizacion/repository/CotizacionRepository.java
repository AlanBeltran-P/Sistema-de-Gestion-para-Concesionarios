package com.concesionaria.venta.cotizacion.repository;

import com.concesionaria.venta.cotizacion.Cotizacion;
import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    boolean existsByClienteIdAndVehiculoIdAndEstadoIn(Long clienteId, Long vehiculoId, Collection<EstadoCotizacion> estados);

    /**
     * @param sinAsignar true: solo las pedidas desde el portal que ningún vendedor tomó todavía
     */
    @Query("""
            select c from Cotizacion c
            where (:estado is null or c.estado = :estado)
              and (:clienteId is null or c.clienteId = :clienteId)
              and (:vendedorId is null or c.vendedorId = :vendedorId)
              and (:sinAsignar = false or c.vendedorId is null)
            """)
    Page<Cotizacion> buscar(@Param("estado") EstadoCotizacion estado,
                            @Param("clienteId") Long clienteId,
                            @Param("vendedorId") Long vendedorId,
                            @Param("sinAsignar") boolean sinAsignar,
                            Pageable pageable);

    /** Las respondidas cuyo precio ya no es válido pasan a VENCIDA. */
    @Modifying(clearAutomatically = true)
    @Query("""
            update Cotizacion c set c.estado = com.concesionaria.venta.cotizacion.enums.EstadoCotizacion.VENCIDA
            where c.estado = com.concesionaria.venta.cotizacion.enums.EstadoCotizacion.RESPONDIDA
              and c.fechaValidez < :hoy
            """)
    int vencerRespondidas(@Param("hoy") LocalDate hoy);
}
