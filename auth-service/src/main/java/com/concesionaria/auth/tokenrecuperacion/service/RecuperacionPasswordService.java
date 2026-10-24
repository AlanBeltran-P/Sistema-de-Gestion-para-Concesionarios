package com.concesionaria.auth.tokenrecuperacion.service;

import com.concesionaria.auth.config.AuthProperties;
import com.concesionaria.auth.evento.RecuperacionPasswordSolicitadaEvento;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.auth.refreshtoken.service.RefreshTokenService;
import com.concesionaria.auth.security.TokenUtils;
import com.concesionaria.auth.tokenrecuperacion.TokenRecuperacion;
import com.concesionaria.auth.tokenrecuperacion.repository.TokenRecuperacionRepository;
import com.concesionaria.auth.usuario.Usuario;
import com.concesionaria.auth.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** RF-05: recuperar la contraseña con un enlace enviado por email. */
@Service
@RequiredArgsConstructor
public class RecuperacionPasswordService {

    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventos;
    private final AuthProperties authProperties;

    /**
     * Genera el token y publica el evento para que notificacion-service mande el email.
     * Si el email no existe no se avisa, para no revelar qué emails están registrados.
     */
    @Transactional
    public void solicitar(String email) {
        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(Usuario::isActivo)
                .ifPresent(this::generarToken);
    }

    @Transactional
    public void restablecer(String token, String nuevaPassword) {
        TokenRecuperacion recuperacion = tokenRecuperacionRepository.findByToken(TokenUtils.hash(token))
                .filter(t -> !t.isUsado() && t.getFechaExpiracion().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new SolicitudInvalidaException("El enlace de recuperación es inválido o venció"));

        recuperacion.setUsado(true);
        Usuario usuario = recuperacion.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        refreshTokenService.revocarTodos(usuario.getId());
    }

    private void generarToken(Usuario usuario) {
        tokenRecuperacionRepository.invalidarPendientes(usuario.getId());

        String token = TokenUtils.generarToken();
        LocalDateTime expiracion = LocalDateTime.now()
                .plusMinutes(authProperties.recuperacionPassword().expiracionMinutos());
        tokenRecuperacionRepository.save(TokenRecuperacion.builder()
                .token(TokenUtils.hash(token))
                .usuario(usuario)
                .fechaExpiracion(expiracion)
                .build());

        eventos.publishEvent(new RecuperacionPasswordSolicitadaEvento(
                usuario.getId(), usuario.getEmail(), usuario.getNombre(), token, expiracion));
    }
}
