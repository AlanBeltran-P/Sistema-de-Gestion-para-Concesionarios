package com.concesionaria.auth.autenticacion.controller;

import com.concesionaria.auth.autenticacion.dto.CambiarPasswordRequest;
import com.concesionaria.auth.autenticacion.dto.LoginRequest;
import com.concesionaria.auth.autenticacion.dto.RecuperarPasswordRequest;
import com.concesionaria.auth.autenticacion.dto.RefreshTokenRequest;
import com.concesionaria.auth.autenticacion.dto.RegistroRequest;
import com.concesionaria.auth.autenticacion.dto.RestablecerPasswordRequest;
import com.concesionaria.auth.autenticacion.dto.TokenResponse;
import com.concesionaria.auth.autenticacion.service.AuthService;
import com.concesionaria.auth.tokenrecuperacion.service.RecuperacionPasswordService;
import com.concesionaria.auth.usuario.dto.UsuarioResponse;
import com.concesionaria.auth.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints públicos de autenticación, más /me para el usuario logueado. */
@Tag(name = "Autenticación", description = "Registro, login, refresh, logout y recuperación de contraseña (RF-02 a RF-05)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final RecuperacionPasswordService recuperacionPasswordService;

    /** RF-02: el cliente se registra solo en el portal. */
    @Operation(summary = "Registrarse como cliente en el portal", description = "RF-02: el cliente se registra solo en el portal.\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    /** RF-03 */
    @Operation(summary = "Iniciar sesión", description = "RF-03\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Renovar el access token con el refresh token", description = "**Público** (no requiere login).")
    @SecurityRequirements
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    /** RF-04 */
    @Operation(summary = "Cerrar sesión", description = "RF-04\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }

    /** RF-05: siempre responde 204, exista o no el email. */
    @Operation(summary = "Pedir el email para recuperar la contraseña", description = "RF-05: siempre responde 204, exista o no el email.\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @PostMapping("/recuperar-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recuperarPassword(@Valid @RequestBody RecuperarPasswordRequest request) {
        recuperacionPasswordService.solicitar(request.email());
    }

    /** RF-05 */
    @Operation(summary = "Elegir una contraseña nueva con el token del email", description = "RF-05\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @PostMapping("/restablecer-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecerPassword(@Valid @RequestBody RestablecerPasswordRequest request) {
        recuperacionPasswordService.restablecer(request.token(), request.nuevaPassword());
    }

    @Operation(summary = "Ver mis datos", description = "**Requiere login** (cualquier rol).")
    @GetMapping("/me")
    public UsuarioResponse yo(@AuthenticationPrincipal Jwt jwt) {
        return authService.usuarioActual(Long.valueOf(jwt.getSubject()));
    }

    /** RF-05: cambiar la contraseña estando logueado. Cierra todas las sesiones. */
    @Operation(summary = "Cambiar mi contraseña", description = "RF-05: cambiar la contraseña estando logueado. Cierra todas las sesiones.\n\n**Requiere login** (cualquier rol).")
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        usuarioService.cambiarPassword(Long.valueOf(jwt.getSubject()),
                request.passwordActual(), request.nuevaPassword());
    }
}
