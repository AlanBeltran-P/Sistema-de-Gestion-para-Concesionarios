package com.concesionaria.auth.autenticacion.dto;

import com.concesionaria.auth.usuario.dto.UsuarioResponse;

/**
 * Respuesta de login y refresh.
 *
 * @param accessToken  JWT que se manda en "Authorization: Bearer ..." en cada request
 * @param refreshToken sirve para pedir un access token nuevo cuando vence; se usa una sola vez
 * @param expiraEn     segundos de vida del access token
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        long expiraEn,
        UsuarioResponse usuario
) {

    public TokenResponse(String accessToken, String refreshToken, long expiraEn, UsuarioResponse usuario) {
        this(accessToken, refreshToken, "Bearer", expiraEn, usuario);
    }
}
