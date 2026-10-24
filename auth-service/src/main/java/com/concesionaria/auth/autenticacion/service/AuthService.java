package com.concesionaria.auth.autenticacion.service;

import com.concesionaria.auth.autenticacion.dto.LoginRequest;
import com.concesionaria.auth.autenticacion.dto.RegistroRequest;
import com.concesionaria.auth.autenticacion.dto.TokenResponse;
import com.concesionaria.auth.exception.NoAutorizadoException;
import com.concesionaria.auth.refreshtoken.service.RefreshTokenService;
import com.concesionaria.auth.security.JwtService;
import com.concesionaria.auth.usuario.Usuario;
import com.concesionaria.auth.usuario.dto.UsuarioResponse;
import com.concesionaria.auth.usuario.repository.UsuarioRepository;
import com.concesionaria.auth.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro, login, refresh y logout (RF-02 a RF-04). */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /** RF-02 */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        Usuario usuario = usuarioService.registrarCliente(
                request.email(), request.password(), request.nombre(), request.apellido());
        return UsuarioResponse.desde(usuario);
    }

    /** RF-03. El mensaje de error es el mismo si el email no existe o la contraseña es incorrecta. */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new NoAutorizadoException(CREDENCIALES_INVALIDAS));
        if (!usuario.isActivo()) {
            throw new NoAutorizadoException("El usuario está desactivado");
        }
        return emitirTokens(usuario);
    }

    /** Entrega un access token nuevo y rota el refresh token. */
    @Transactional(noRollbackFor = NoAutorizadoException.class)
    public TokenResponse refresh(String refreshToken) {
        Usuario usuario = refreshTokenService.consumir(refreshToken);
        return emitirTokens(usuario);
    }

    /** RF-04 */
    public void logout(String refreshToken) {
        refreshTokenService.revocar(refreshToken);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse usuarioActual(Long usuarioId) {
        return usuarioService.obtener(usuarioId);
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        String accessToken = jwtService.generarAccessToken(usuario);
        String refreshToken = refreshTokenService.crear(usuario);
        return new TokenResponse(accessToken, refreshToken,
                jwtService.duracionAccessToken().toSeconds(), UsuarioResponse.desde(usuario));
    }
}
