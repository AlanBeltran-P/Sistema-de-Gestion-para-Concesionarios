package com.concesionaria.auth.usuario.controller;

import com.concesionaria.auth.usuario.dto.CambiarEstadoRequest;
import com.concesionaria.auth.usuario.dto.CambiarRolRequest;
import com.concesionaria.auth.usuario.dto.CrearEmpleadoRequest;
import com.concesionaria.auth.usuario.dto.UsuarioResponse;
import com.concesionaria.auth.usuario.enums.Rol;
import com.concesionaria.auth.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Gestión de usuarios. Todo el controller es solo para ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** RF-01: registrar un empleado (ADMIN o VENDEDOR). */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearEmpleado(@Valid @RequestBody CrearEmpleadoRequest request) {
        UsuarioResponse creado = usuarioService.crearEmpleado(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** Lista paginada con filtros opcionales: ?rol=VENDEDOR&activo=true&texto=perez&page=0&size=20 */
    @GetMapping
    public Page<UsuarioResponse> buscar(@RequestParam(required = false) Rol rol,
                                        @RequestParam(required = false) Boolean activo,
                                        @RequestParam(required = false) String texto,
                                        @PageableDefault(size = 20, sort = "apellido", direction = Sort.Direction.ASC)
                                        Pageable pageable) {
        return usuarioService.buscar(rol, activo, texto, pageable);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    /** RF-06 */
    @PatchMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id,
                                      @Valid @RequestBody CambiarRolRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarRol(id, request.rol(), Long.valueOf(jwt.getSubject()));
    }

    /** RF-06 */
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarEstado(id, request.activo(), Long.valueOf(jwt.getSubject()));
    }
}
