package com.concesionaria.auth.config;

import com.concesionaria.auth.usuario.Usuario;
import com.concesionaria.auth.usuario.enums.Rol;
import com.concesionaria.auth.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer ADMIN al arrancar si todavía no hay ninguno.
 * Sin esto nadie podría registrar empleados (RF-01 requiere un ADMIN).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicialLoader implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByRol(Rol.ADMIN)) {
            return;
        }
        AuthProperties.AdminInicial admin = authProperties.adminInicial();
        usuarioRepository.save(Usuario.builder()
                .email(admin.email().toLowerCase())
                .password(passwordEncoder.encode(admin.password()))
                .nombre(admin.nombre())
                .apellido(admin.apellido())
                .rol(Rol.ADMIN)
                .build());
        log.warn("Se creó el ADMIN inicial {}. Cambiá su contraseña después del primer login.", admin.email());
    }
}
