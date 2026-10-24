package com.concesionaria.notificacion.notificacion.repository;

import com.concesionaria.notificacion.notificacion.Notificacion;
import com.concesionaria.notificacion.notificacion.enums.EstadoNotificacion;
import com.concesionaria.notificacion.notificacion.enums.TipoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    boolean existsByEventoId(String eventoId);

    /** Las que fallaron y todavía tienen intentos. */
    @Query("select n.id from Notificacion n where n.estado = :estado and n.intentos < :maxIntentos order by n.id")
    List<Long> findIdsParaReintentar(@Param("estado") EstadoNotificacion estado, @Param("maxIntentos") int maxIntentos);

    @Query("""
            select n from Notificacion n
            where (:tipo is null or n.tipo = :tipo)
              and (:estado is null or n.estado = :estado)
              and (:destinatario is null or lower(n.destinatario) = lower(:destinatario))
              and (:referenciaId is null or n.referenciaId = :referenciaId)
            """)
    Page<Notificacion> buscar(@Param("tipo") TipoNotificacion tipo,
                              @Param("estado") EstadoNotificacion estado,
                              @Param("destinatario") String destinatario,
                              @Param("referenciaId") Long referenciaId,
                              Pageable pageable);
}
