package com.concesionaria.auth.refreshtoken.repository;

import com.concesionaria.auth.refreshtoken.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Busca por el hash del token, no por el token en texto plano. */
    Optional<RefreshToken> findByToken(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revocado = true where r.usuario.id = :usuarioId and r.revocado = false")
    int revocarTodosDelUsuario(@Param("usuarioId") Long usuarioId);
}
