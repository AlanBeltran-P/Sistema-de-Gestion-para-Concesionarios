package com.concesionaria.venta.tasacion.repository;

import com.concesionaria.venta.tasacion.TasacionTradeIn;
import com.concesionaria.venta.tasacion.enums.EstadoTasacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TasacionRepository extends JpaRepository<TasacionTradeIn, Long> {

    @Query("""
            select t from TasacionTradeIn t
            where (:clienteId is null or t.clienteId = :clienteId)
              and (:estado is null or t.estado = :estado)
            """)
    Page<TasacionTradeIn> buscar(@Param("clienteId") Long clienteId,
                                 @Param("estado") EstadoTasacion estado,
                                 Pageable pageable);
}
