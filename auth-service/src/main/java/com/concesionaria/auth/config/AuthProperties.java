package com.concesionaria.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades "auth.*" de config-repo/auth-service.yml. */
@ConfigurationProperties("auth")
public record AuthProperties(RecuperacionPassword recuperacionPassword, AdminInicial adminInicial) {

    public record RecuperacionPassword(int expiracionMinutos) {
    }

    public record AdminInicial(String email, String password, String nombre, String apellido) {
    }
}
