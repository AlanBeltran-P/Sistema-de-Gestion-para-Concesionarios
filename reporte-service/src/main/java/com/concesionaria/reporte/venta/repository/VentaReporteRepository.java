package com.concesionaria.reporte.venta.repository;

import com.concesionaria.reporte.venta.VentaReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaReporteRepository extends JpaRepository<VentaReporte, Long> {

    Optional<VentaReporte> findByVentaId(Long ventaId);

    /** Ventas del período (confirmadas y anuladas), opcionalmente de un vendedor. */
    @Query("""
            select v from VentaReporte v
            where v.fechaVenta >= :desde and v.fechaVenta < :hasta
              and (:vendedorId is null or v.vendedorId = :vendedorId)
            order by v.fechaVenta
            """)
    List<VentaReporte> delPeriodo(@Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta,
                                  @Param("vendedorId") Long vendedorId);
}
