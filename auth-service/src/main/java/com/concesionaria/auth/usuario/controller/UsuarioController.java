package com.concesionaria.auth.usuario.controller;

import com.concesionaria.auth.usuario.dto.CambiarEstadoRequest;
import com.concesionaria.auth.usuario.dto.CambiarRolRequest;
import com.concesionaria.auth.usuario.dto.CrearEmpleadoRequest;
import com.concesionaria.auth.usuario.dto.UsuarioResponse;
import com.concesionaria.auth.usuario.enums.Rol;
import com.concesionaria.auth.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Usuarios", description = "Empleados, roles y activación de cuentas (RF-01, RF-06). Solo ADMIN")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** RF-01: registrar un empleado (ADMIN o VENDEDOR). */
    @Operation(summary = "Crear un empleado (ADMIN o VENDEDOR)", description = "RF-01: registrar un empleado (ADMIN o VENDEDOR).\n\n**Requiere login** (cualquier rol).")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearEmpleado(@Valid @RequestBody CrearEmpleadoRequest request) {
        UsuarioResponse creado = usuarioService.crearEmpleado(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** Lista paginada con filtros opcionales: ?rol=VENDEDOR&activo=true&texto=perez&page=0&size=20 */
    @Operation(summary = "Buscar usuarios", description = "Lista paginada con filtros opcionales: ?rol=VENDEDOR&activo=true&texto=perez&page=0&size=20\n\n**Requiere login** (cualquier rol).")
    @GetMapping
    public Page<UsuarioResponse> buscar(@RequestParam(required = false) Rol rol,
                                        @RequestParam(required = false) Boolean activo,
                                        @RequestParam(required = false) String texto,
                                        @ParameterObject @PageableDefault(size = 20, sort = "apellido", direction = Sort.Direction.ASC)
                                        Pageable pageable) {
        return usuarioService.buscar(rol, activo, texto, pageable);
    }

    @Operation(summary = "Ver un usuario", description = "**Requiere login** (cualquier rol).")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    /** RF-06 */
    @Operation(summary = "Cambiar el rol de un usuario", description = "RF-06\n\n**Requiere login** (cualquier rol).")
    @PatchMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id,
                                      @Valid @RequestBody CambiarRolRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarRol(id, request.rol(), Long.valueOf(jwt.getSubject()));
    }

    /** RF-06 */
    @Operation(summary = "Activar o desactivar un usuario", description = "RF-06\n\n**Requiere login** (cualquier rol).")
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarEstado(id, request.activo(), Long.valueOf(jwt.getSubject()));
    }
}
