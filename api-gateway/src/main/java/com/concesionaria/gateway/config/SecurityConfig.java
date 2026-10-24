package com.concesionaria.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * Primera barrera de seguridad: el gateway rechaza con 401 cualquier pedido a una ruta
 * privada que no traiga un JWT válido, y reenvía el header Authorization al servicio.
 * Qué rol puede hacer qué lo decide cada microservicio, que vuelve a validar el token.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /** Debe coincidir con JwtService.ISSUER de auth-service. */
    private static final String ISSUER = "auth-service";

    private static final String[] AUTH_PUBLICOS = {
            "/api/auth/registro",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/recuperar-password",
            "/api/auth/restablecer-password"
    };

    /** RF-12, RF-13: catálogo público (solo lectura). */
    private static final String[] CATALOGO_PUBLICO = {
            "/api/vehiculos/**",
            "/api/marcas/**",
            "/api/modelos/**",
            "/api/categorias/**"
    };

    private static final String[] DOCUMENTACION = {
            "/swagger-ui.html", "/swagger-ui/**", "/webjars/**", "/v3/api-docs/**", "/docs/*/v3/api-docs"
    };

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, AUTH_PUBLICOS).permitAll()
                        .pathMatchers(HttpMethod.GET, CATALOGO_PUBLICO).permitAll()
                        // RF-29: simular cuotas sin estar logueado (planes activos + simulación)
                        .pathMatchers(HttpMethod.GET, "/api/planes", "/api/planes/*").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/planes/simulacion").permitAll()
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // Documentación de la API (Swagger UI y la especificación de cada servicio)
                        .pathMatchers(HttpMethod.GET, DOCUMENTACION).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        byte[] clave = Base64.getDecoder().decode(secret);
        SecretKey key = new SecretKeySpec(clave, "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // Valida firma, vencimiento y que lo haya emitido auth-service
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed-origins}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Location"));
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
