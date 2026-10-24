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
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final RecuperacionPasswordService recuperacionPasswordService;

    /** RF-02: el cliente se registra solo en el portal. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    /** RF-03 */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    /** RF-04 */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }

    /** RF-05: siempre responde 204, exista o no el email. */
    @PostMapping("/recuperar-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recuperarPassword(@Valid @RequestBody RecuperarPasswordRequest request) {
        recuperacionPasswordService.solicitar(request.email());
    }

    /** RF-05 */
    @PostMapping("/restablecer-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecerPassword(@Valid @RequestBody RestablecerPasswordRequest request) {
        recuperacionPasswordService.restablecer(request.token(), request.nuevaPassword());
    }

    @GetMapping("/me")
    public UsuarioResponse yo(@AuthenticationPrincipal Jwt jwt) {
        return authService.usuarioActual(Long.valueOf(jwt.getSubject()));
    }

    /** RF-05: cambiar la contraseña estando logueado. Cierra todas las sesiones. */
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        usuarioService.cambiarPassword(Long.valueOf(jwt.getSubject()),
                request.passwordActual(), request.nuevaPassword());
    }
}
