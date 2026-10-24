package com.concesionaria.auth.security;

import com.concesionaria.auth.config.JwtProperties;
import com.concesionaria.auth.usuario.Usuario;
import com.concesionaria.common.security.JwtConstantes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Emite el access token. Claims: sub = id del usuario, email, rol y nombre completo.
 * Los demás servicios leen estos claims para saber quién hace el request.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String generarAccessToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConstantes.ISSUER)
                .subject(usuario.getId().toString())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracionAccessToken()))
                .claim(JwtConstantes.CLAIM_EMAIL, usuario.getEmail())
                .claim(JwtConstantes.CLAIM_ROL, usuario.getRol().name())
                .claim(JwtConstantes.CLAIM_NOMBRE, usuario.getNombre() + " " + usuario.getApellido())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Duration duracionAccessToken() {
        return Duration.ofMinutes(jwtProperties.accessTokenMinutos());
    }
}
