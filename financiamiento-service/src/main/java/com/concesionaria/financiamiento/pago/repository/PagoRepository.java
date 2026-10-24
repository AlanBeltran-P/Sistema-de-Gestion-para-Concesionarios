package com.concesionaria.financiamiento.pago.repository;

import com.concesionaria.financiamiento.pago.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Mora ya cobrada de una cuota: los pagos cubren primero la mora y después la cuota. */
    @Query("select coalesce(sum(p.montoMora), 0) from Pago p where p.cuota.id = :cuotaId")
    BigDecimal sumarMoraPagada(@Param("cuotaId") Long cuotaId);

    @EntityGraph(attributePaths = {"financiamiento", "cuota"})
    @Query("""
            select p from Pago p
            where (:financiamientoId is null or p.financiamiento.id = :financiamientoId)
              and (:clienteId is null or p.financiamiento.clienteId = :clienteId)
            """)
    Page<Pago> buscar(@Param("financiamientoId") Long financiamientoId,
                      @Param("clienteId") Long clienteId,
                      Pageable pageable);
}
