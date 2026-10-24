package com.concesionaria.vehiculo.config;

import com.concesionaria.common.autoconfigure.CommonSeguridadAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * El catálogo se consulta sin login (RF-12, RF-13). Todo lo demás requiere token y
 * los permisos por rol se definen con @PreAuthorize en cada controller.
 * Si un GET público trae token, se usa para saber si es un empleado (ve más datos).
 */
@Configuration
public class SecurityConfig {

    private static final String[] LECTURA_PUBLICA = {
            "/api/vehiculos/**", "/api/marcas/**", "/api/modelos/**", "/api/categorias/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CommonSeguridadAutoConfiguration.configurarBase(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, LECTURA_PUBLICA).permitAll()
                        .requestMatchers(CommonSeguridadAutoConfiguration.RUTAS_PUBLICAS_BASE).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
