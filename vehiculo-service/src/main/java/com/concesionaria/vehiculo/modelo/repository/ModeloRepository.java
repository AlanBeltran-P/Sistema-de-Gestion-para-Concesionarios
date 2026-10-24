package com.concesionaria.vehiculo.modelo.repository;

import com.concesionaria.vehiculo.modelo.Modelo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModeloRepository extends JpaRepository<Modelo, Long> {

    /** Filtros opcionales (null = sin filtro). Trae marca y categoría en la misma consulta. */
    @EntityGraph(attributePaths = {"marca", "categoria"})
    @Query("""
            select m from Modelo m
            where (:marcaId is null or m.marca.id = :marcaId)
              and (:categoriaId is null or m.categoria.id = :categoriaId)
              and (:incluirInactivos = true or m.activo = true)
            order by m.marca.nombre, m.nombre
            """)
    List<Modelo> buscar(@Param("marcaId") Long marcaId,
                        @Param("categoriaId") Long categoriaId,
                        @Param("incluirInactivos") boolean incluirInactivos);

    @EntityGraph(attributePaths = {"marca", "categoria"})
    Optional<Modelo> findWithMarcaAndCategoriaById(Long id);

    boolean existsByMarcaIdAndNombreIgnoreCase(Long marcaId, String nombre);

    boolean existsByMarcaIdAndNombreIgnoreCaseAndIdNot(Long marcaId, String nombre, Long id);
}
