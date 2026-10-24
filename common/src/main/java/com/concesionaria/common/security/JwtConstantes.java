package com.concesionaria.common.security;

/** Nombres compartidos entre quien emite el JWT (auth-service) y quienes lo validan. */
public final class JwtConstantes {

    /** Claim "iss" de todos los tokens. */
    public static final String ISSUER = "auth-service";

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";

    /**
     * Rol de los tokens que generan los propios servicios para llamarse entre sí
     * (ej: venta-service marca un vehículo como VENDIDO). No lo tiene ningún usuario.
     */
    public static final String ROL_SISTEMA = "SISTEMA";

    private JwtConstantes() {
    }
}
