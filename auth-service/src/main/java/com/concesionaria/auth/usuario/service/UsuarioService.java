package com.concesionaria.auth.usuario.service;

import com.concesionaria.auth.evento.UsuarioRegistradoEvento;
import com.concesionaria.auth.exception.ConflictoException;
import com.concesionaria.auth.exception.NoAutorizadoException;
import com.concesionaria.auth.exception.NoEncontradoException;
import com.concesionaria.auth.exception.SolicitudInvalidaException;
import com.concesionaria.auth.refreshtoken.service.RefreshTokenService;
import com.concesionaria.auth.usuario.Usuario;
import com.concesionaria.auth.usuario.dto.CrearEmpleadoRequest;
import com.concesionaria.auth.usuario.dto.UsuarioResponse;
import com.concesionaria.auth.usuario.enums.Rol;
import com.concesionaria.auth.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventos;

    /** RF-01 */
    @Transactional
    public UsuarioResponse crearEmpleado(CrearEmpleadoRequest request) {
        if (request.rol() == Rol.CLIENTE) {
            throw new SolicitudInvalidaException("Los clientes se registran desde el portal; acá solo ADMIN o VENDEDOR");
        }
        Usuario usuario = crear(request.email(), request.password(), request.nombre(), request.apellido(), request.rol());
        return UsuarioResponse.desde(usuario);
    }

    /** RF-02 */
    @Transactional
    public Usuario registrarCliente(String email, String password, String nombre, String apellido) {
        return crear(email, password, nombre, apellido, Rol.CLIENTE);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> buscar(Rol rol, Boolean activo, String texto, Pageable pageable) {
        String filtro = StringUtils.hasText(texto) ? texto.trim() : null;
        return usuarioRepository.buscar(rol, activo, filtro, pageable).map(UsuarioResponse::desde);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscarPorId(id));
    }

    /** RF-06. Se cierran sus sesiones para que el nuevo rol aplique en el próximo login. */
    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol nuevoRol, Long adminId) {
        if (id.equals(adminId) && nuevoRol != Rol.ADMIN) {
            throw new SolicitudInvalidaException("No podés quitarte el rol ADMIN a vos mismo");
        }
        Usuario usuario = buscarPorId(id);
        if (usuario.getRol() != nuevoRol) {
            usuario.setRol(nuevoRol);
            refreshTokenService.revocarTodos(id);
        }
        return UsuarioResponse.desde(usuario);
    }

    /** RF-06. Al desactivar se cierran todas sus sesiones. */
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo, Long adminId) {
        if (id.equals(adminId) && !activo) {
            throw new SolicitudInvalidaException("No podés desactivar tu propio usuario");
        }
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(activo);
        if (!activo) {
            refreshTokenService.revocarTodos(id);
        }
        return UsuarioResponse.desde(usuario);
    }

    /** RF-05: el usuario logueado cambia su contraseña. Se cierran todas sus sesiones. */
    @Transactional
    public void cambiarPassword(Long id, String passwordActual, String nuevaPassword) {
        Usuario usuario = buscarPorId(id);
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new NoAutorizadoException("La contraseña actual es incorrecta");
        }
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        refreshTokenService.revocarTodos(id);
    }

    private Usuario crear(String email, String password, String nombre, String apellido, Rol rol) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ConflictoException("Ya existe un usuario con el email " + emailNormalizado);
        }
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(emailNormalizado)
                .password(passwordEncoder.encode(password))
                .nombre(nombre.trim())
                .apellido(apellido.trim())
                .rol(rol)
                .build());

        eventos.publishEvent(new UsuarioRegistradoEvento(
                usuario.getId(), usuario.getEmail(), usuario.getNombre(), usuario.getApellido(), rol.name()));
        return usuario;
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Usuario " + id + " no encontrado"));
    }
}
