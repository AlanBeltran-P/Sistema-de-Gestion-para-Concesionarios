package com.concesionaria.cliente.cliente.controller;

import com.concesionaria.cliente.cliente.dto.ClienteRequest;
import com.concesionaria.cliente.cliente.dto.ClienteResponse;
import com.concesionaria.cliente.cliente.dto.PerfilRequest;
import com.concesionaria.cliente.cliente.enums.OrigenCliente;
import com.concesionaria.cliente.cliente.service.ClienteService;
import com.concesionaria.common.security.UsuarioActual;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /** RF-16: búsqueda. ?texto=perez (nombre, email o documento) &origen=LOCAL&page=0&size=20 */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public Page<ClienteResponse> buscar(@RequestParam(required = false) String texto,
                                        @RequestParam(required = false) OrigenCliente origen,
                                        @PageableDefault(size = 20, sort = "apellido", direction = Sort.Direction.ASC)
                                        Pageable pageable) {
        return clienteService.buscar(texto, origen, pageable);
    }

    /** RF-16 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    /** RF-14: cliente que viene al local, sin cuenta. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        ClienteResponse creado = clienteService.crear(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    /** RF-15: el cliente logueado ve su perfil. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return clienteService.perfilPropio(UsuarioActual.desde(jwt));
    }

    /** RF-15: el cliente completa o edita su perfil. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse actualizarMiPerfil(@Valid @RequestBody PerfilRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        return clienteService.actualizarPerfil(UsuarioActual.desde(jwt), request);
    }
}
