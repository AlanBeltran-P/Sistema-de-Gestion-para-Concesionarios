package com.concesionaria.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Duraciones de los tokens ("jwt.*" de config-repo). La clave jwt.secret la lee common.
 *
 * @param accessTokenMinutos duración del access token
 * @param refreshTokenDias   duración del refresh token
 */
@ConfigurationProperties("jwt")
public record JwtProperties(int accessTokenMinutos, int refreshTokenDias) {
}
