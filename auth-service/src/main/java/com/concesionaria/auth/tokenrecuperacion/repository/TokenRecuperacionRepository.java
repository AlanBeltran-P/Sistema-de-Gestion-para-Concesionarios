package com.concesionaria.auth.tokenrecuperacion.repository;

import com.concesionaria.auth.tokenrecuperacion.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    /** Busca por el hash del token, no por el token en texto plano. */
    Optional<TokenRecuperacion> findByToken(String tokenHash);

    /** Al pedir un enlace nuevo, los anteriores dejan de servir. */
    @Modifying
    @Query("update TokenRecuperacion t set t.usado = true where t.usuario.id = :usuarioId and t.usado = false")
    int invalidarPendientes(@Param("usuarioId") Long usuarioId);
}
