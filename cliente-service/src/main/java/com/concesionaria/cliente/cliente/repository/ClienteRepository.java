package com.concesionaria.cliente.cliente.repository;

import com.concesionaria.cliente.cliente.Cliente;
import com.concesionaria.cliente.cliente.enums.OrigenCliente;
import com.concesionaria.cliente.cliente.enums.TipoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByUsuarioId(Long usuarioId);

    Optional<Cliente> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipo, String numero);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(TipoDocumento tipo, String numero, Long id);

    /** RF-16: texto busca en nombre, apellido, nombre completo, email y número de documento. */
    @Query("""
            select c from Cliente c
            where (:origen is null or c.origen = :origen)
              and (:texto is null
                   or lower(c.nombre) like lower(concat('%', :texto, '%'))
                   or lower(c.apellido) like lower(concat('%', :texto, '%'))
                   or lower(concat(c.nombre, ' ', c.apellido)) like lower(concat('%', :texto, '%'))
                   or lower(c.email) like lower(concat('%', :texto, '%'))
                   or c.numeroDocumento like concat('%', :texto, '%'))
            """)
    Page<Cliente> buscar(@Param("origen") OrigenCliente origen,
                         @Param("texto") String texto,
                         Pageable pageable);
}
