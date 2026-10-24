package com.concesionaria.financiamiento.config;

import com.concesionaria.common.autoconfigure.CommonSeguridadAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * RF-29: cualquiera puede ver los planes activos y simular cuotas sin login.
 * Lo demás requiere token y los permisos por rol van con @PreAuthorize.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CommonSeguridadAutoConfiguration.configurarBase(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/planes", "/api/planes/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/planes/simulacion").permitAll()
                        .requestMatchers(CommonSeguridadAutoConfiguration.RUTAS_PUBLICAS_BASE).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
