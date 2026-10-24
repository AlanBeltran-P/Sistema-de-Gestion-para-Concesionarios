package com.concesionaria.auth.refreshtoken.service;

import com.concesionaria.auth.config.JwtProperties;
import com.concesionaria.common.exception.NoAutorizadoException;
import com.concesionaria.auth.refreshtoken.RefreshToken;
import com.concesionaria.auth.refreshtoken.repository.RefreshTokenRepository;
import com.concesionaria.auth.security.TokenUtils;
import com.concesionaria.auth.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Refresh tokens con rotación: cada vez que se usa uno, se revoca y se entrega uno nuevo.
 * Si llega un token que ya estaba revocado, alguien lo copió: se revocan todos los
 * del usuario y tiene que volver a iniciar sesión.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /** Crea un refresh token y devuelve su valor en texto plano (es la única vez que se ve). */
    @Transactional
    public String crear(Usuario usuario) {
        String token = TokenUtils.generarToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(TokenUtils.hash(token))
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusDays(jwtProperties.refreshTokenDias()))
                .build());
        return token;
    }

    /**
     * Valida el token, lo revoca y devuelve su usuario.
     * noRollbackFor: si se detecta reutilización, la revocación de todos los tokens se tiene que guardar
     * aunque se lance la excepción.
     */
    @Transactional(noRollbackFor = NoAutorizadoException.class)
    public Usuario consumir(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(TokenUtils.hash(token))
                .orElseThrow(() -> new NoAutorizadoException("Refresh token inválido"));
        Usuario usuario = refreshToken.getUsuario();

        if (refreshToken.isRevocado()) {
            log.warn("Se reutilizó un refresh token revocado del usuario {}. Se cierran todas sus sesiones.",
                    usuario.getId());
            refreshTokenRepository.revocarTodosDelUsuario(usuario.getId());
            throw new NoAutorizadoException("Refresh token inválido");
        }
        if (refreshToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new NoAutorizadoException("La sesión venció, volvé a iniciar sesión");
        }
        if (!usuario.isActivo()) {
            throw new NoAutorizadoException("El usuario está desactivado");
        }

        refreshToken.setRevocado(true);
        return usuario;
    }

    /** RF-04: logout. Si el token no existe no hace nada (el resultado es el mismo). */
    @Transactional
    public void revocar(String token) {
        refreshTokenRepository.findByToken(TokenUtils.hash(token))
                .ifPresent(r -> r.setRevocado(true));
    }

    /** Cierra todas las sesiones del usuario (cambio de contraseña, desactivación, cambio de rol). */
    @Transactional
    public void revocarTodos(Long usuarioId) {
        refreshTokenRepository.revocarTodosDelUsuario(usuarioId);
    }
}
