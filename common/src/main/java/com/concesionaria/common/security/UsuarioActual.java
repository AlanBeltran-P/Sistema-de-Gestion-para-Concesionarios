package com.concesionaria.common.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Quién hace el request, leído del JWT. Uso en un controller:
 * <pre>
 * public X algo(@AuthenticationPrincipal Jwt jwt) {
 *     UsuarioActual usuario = UsuarioActual.desde(jwt);
 * }
 * </pre>
 */
public record UsuarioActual(Long id, String email, String rol, String nombre) {

    public static UsuarioActual desde(Jwt jwt) {
        return new UsuarioActual(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(JwtConstantes.CLAIM_EMAIL),
                jwt.getClaimAsString(JwtConstantes.CLAIM_ROL),
                jwt.getClaimAsString(JwtConstantes.CLAIM_NOMBRE));
    }

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }

    public boolean esVendedor() {
        return "VENDEDOR".equals(rol);
    }

    public boolean esCliente() {
        return "CLIENTE".equals(rol);
    }

    /** ADMIN o VENDEDOR. */
    public boolean esEmpleado() {
        return esAdmin() || esVendedor();
    }
}
