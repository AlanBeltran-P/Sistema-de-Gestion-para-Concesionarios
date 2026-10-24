package com.concesionaria.auth.usuario.repository;

import com.concesionaria.auth.usuario.Usuario;
import com.concesionaria.auth.usuario.enums.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRol(Rol rol);

    /** Búsqueda para el ADMIN. Los filtros en null no se aplican. */
    @Query("""
            select u from Usuario u
            where (:rol is null or u.rol = :rol)
              and (:activo is null or u.activo = :activo)
              and (:texto is null
                   or lower(u.email) like lower(concat('%', :texto, '%'))
                   or lower(u.nombre) like lower(concat('%', :texto, '%'))
                   or lower(u.apellido) like lower(concat('%', :texto, '%')))
            """)
    Page<Usuario> buscar(@Param("rol") Rol rol,
                         @Param("activo") Boolean activo,
                         @Param("texto") String texto,
                         Pageable pageable);
}
