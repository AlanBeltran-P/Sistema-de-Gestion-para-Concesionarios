package com.concesionaria.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades "jwt.*" de config-repo.
 *
 * @param secret             clave HS256 en Base64 (mínimo 32 bytes)
 * @param accessTokenMinutos duración del access token
 * @param refreshTokenDias   duración del refresh token
 */
@ConfigurationProperties("jwt")
public record JwtProperties(String secret, int accessTokenMinutos, int refreshTokenDias) {
}
