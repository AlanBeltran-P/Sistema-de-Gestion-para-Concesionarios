package com.concesionaria.cliente.cliente.controller;

import com.concesionaria.cliente.cliente.dto.ClienteRequest;
import com.concesionaria.cliente.cliente.dto.ClienteResponse;
import com.concesionaria.cliente.cliente.dto.PerfilRequest;
import com.concesionaria.cliente.cliente.dto.UsuarioPortalRequest;
import com.concesionaria.cliente.cliente.enums.OrigenCliente;
import com.concesionaria.cliente.cliente.service.ClienteService;
import com.concesionaria.common.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Hidden;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Todo requiere login (configuración por defecto de common). */
@Tag(name = "Clientes", description = "Clientes del local y perfil del portal (RF-14 a RF-16)")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /** RF-16: búsqueda. ?texto=perez (nombre, email o documento) &origen=LOCAL&page=0&size=20 */
    @Operation(summary = "Buscar clientes", description = "RF-16: búsqueda. ?texto=perez (nombre, email o documento) &origen=LOCAL&page=0&size=20\n\n**Roles:** ADMIN, VENDEDOR.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public Page<ClienteResponse> buscar(@RequestParam(required = false) String texto,
                                        @RequestParam(required = false) OrigenCliente origen,
                                        @ParameterObject @PageableDefault(size = 20, sort = "apellido", direction = Sort.Direction.ASC)
                                        Pageable pageable) {
        return clienteService.buscar(texto, origen, pageable);
    }

    /** RF-16. SISTEMA: venta-service y financiamiento-service consultan los datos del cliente. */
    @Operation(summary = "Ver un cliente", description = "RF-16. SISTEMA: venta-service y financiamiento-service consultan los datos del cliente.\n\n**Roles:** ADMIN, VENDEDOR.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    /**
     * Uso interno: el perfil del usuario logueado en el portal (ej: venta-service cuando un cliente
     * pide una cotización). Si todavía no existe se crea, igual que en GET /me.
     */
    @Hidden
    @PostMapping("/portal")
    @PreAuthorize("hasRole('SISTEMA')")
    public ClienteResponse obtenerOCrearDelPortal(@Valid @RequestBody UsuarioPortalRequest request) {
        return clienteService.perfilPropio(
                new UsuarioActual(request.usuarioId(), request.email(), "CLIENTE", request.nombre()));
    }

    /** RF-14: cliente que viene al local, sin cuenta. */
    @Operation(summary = "Registrar un cliente del local", description = "RF-14: cliente que viene al local, sin cuenta.\n\n**Roles:** ADMIN, VENDEDOR.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        ClienteResponse creado = clienteService.crear(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @Operation(summary = "Editar un cliente", description = "**Roles:** ADMIN, VENDEDOR.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    /** RF-15: el cliente logueado ve su perfil. */
    @Operation(summary = "Ver mi perfil", description = "RF-15: el cliente logueado ve su perfil.\n\n**Roles:** CLIENTE.")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return clienteService.perfilPropio(UsuarioActual.desde(jwt));
    }

    /** RF-15: el cliente completa o edita su perfil. */
    @Operation(summary = "Completar o editar mi perfil", description = "RF-15: el cliente completa o edita su perfil.\n\n**Roles:** CLIENTE.")
    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse actualizarMiPerfil(@Valid @RequestBody PerfilRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        return clienteService.actualizarPerfil(UsuarioActual.desde(jwt), request);
    }
}
