package com.concesionaria.common.autoconfigure;

import com.concesionaria.common.security.JwtConstantes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Cada servicio valida el JWT por su cuenta (además del gateway):
 * - SecretKey y JwtDecoder con la clave compartida jwt.secret.
 * - El claim "rol" se convierte en ROLE_ADMIN, ROLE_VENDEDOR o ROLE_CLIENTE,
 *   así funcionan hasRole(...) y @PreAuthorize("hasRole('ADMIN')").
 * - Un SecurityFilterChain por defecto (todo requiere token) que el servicio puede
 *   reemplazar definiendo el suyo, por ejemplo para abrir endpoints públicos.
 */
@AutoConfiguration(beforeName = {
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration",
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity
@EnableMethodSecurity
public class CommonSeguridadAutoConfiguration {

    /** Rutas abiertas en cualquier servicio (salud y documentación de la API). */
    public static final String[] RUTAS_PUBLICAS_BASE = {
            "/actuator/health/**", "/actuator/info", "/error",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    @Bean
    @ConditionalOnMissingBean
    SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret) {
        byte[] clave = Base64.getDecoder().decode(secret);
        if (clave.length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 bytes (256 bits) para HS256");
        }
        return new SecretKeySpec(clave, "HmacSHA256");
    }

    @Bean
    @ConditionalOnMissingBean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // Valida firma, vencimiento y que lo haya emitido auth-service
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtConstantes.ISSUER));
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(JwtConstantes.CLAIM_ROL);
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    /** Por defecto todo requiere un JWT válido; los permisos por rol van con @PreAuthorize. */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain securityFilterChainPorDefecto(HttpSecurity http) throws Exception {
        configurarBase(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(RUTAS_PUBLICAS_BASE).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    /**
     * Configuración común para usar desde el SecurityFilterChain propio de un servicio:
     * sin sesión, sin CSRF (la API usa tokens, no cookies) y validando el JWT.
     */
    public static HttpSecurity configurarBase(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
    }
}
