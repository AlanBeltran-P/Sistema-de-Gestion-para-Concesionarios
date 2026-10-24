package com.concesionaria.common.security;

/** Nombres compartidos entre quien emite el JWT (auth-service) y quienes lo validan. */
public final class JwtConstantes {

    /** Claim "iss" de todos los tokens. */
    public static final String ISSUER = "auth-service";

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";

    private JwtConstantes() {
    }
}
