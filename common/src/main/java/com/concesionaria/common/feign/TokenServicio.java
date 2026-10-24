package com.concesionaria.common.feign;

import com.concesionaria.common.security.JwtConstantes;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;

/**
 * JWT con rol SISTEMA que usa un servicio para llamar a otro (Feign). Se firma con la misma
 * clave compartida, así el servicio que lo recibe lo valida igual que el de un usuario.
 * Quien llama ya validó los permisos del usuario; el servicio llamado confía en el rol SISTEMA.
 * Se reutiliza hasta un minuto antes de vencer.
 */
public class TokenServicio {

    private static final Duration DURACION = Duration.ofMinutes(10);
    private static final Duration MARGEN = Duration.ofMinutes(1);

    private final JwtEncoder encoder;
    private final String servicio;

    private volatile String token;
    private volatile Instant vence = Instant.EPOCH;

    public TokenServicio(SecretKey clave, String servicio) {
        this.encoder = NimbusJwtEncoder.withSecretKey(clave).algorithm(MacAlgorithm.HS256).build();
        this.servicio = servicio;
    }

    public String obtener() {
        if (Instant.now().isAfter(vence.minus(MARGEN))) {
            synchronized (this) {
                if (Instant.now().isAfter(vence.minus(MARGEN))) {
                    generar();
                }
            }
        }
        return token;
    }

    private void generar() {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(DURACION);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConstantes.ISSUER)
                .subject("0")
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim(JwtConstantes.CLAIM_ROL, JwtConstantes.ROL_SISTEMA)
                .claim(JwtConstantes.CLAIM_NOMBRE, servicio)
                .claim(JwtConstantes.CLAIM_EMAIL, servicio + "@sistema")
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        vence = expira;
    }
}
