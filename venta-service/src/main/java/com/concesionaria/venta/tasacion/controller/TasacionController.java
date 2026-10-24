package com.concesionaria.venta.tasacion.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.tasacion.dto.CambiarEstadoTasacionRequest;
import com.concesionaria.venta.tasacion.dto.TasacionRequest;
import com.concesionaria.venta.tasacion.dto.TasacionResponse;
import com.concesionaria.venta.tasacion.enums.EstadoTasacion;
import com.concesionaria.venta.tasacion.service.TasacionService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** RF-20: solo empleados. */
@Tag(name = "Tasaciones (trade-in)", description = "Tasación del auto usado del cliente (RF-20)")
@RestController
@RequestMapping("/api/tasaciones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class TasacionController {

    private final TasacionService tasacionService;

    @Operation(summary = "Tasar el auto usado de un cliente", description = "**Roles:** ADMIN, VENDEDOR.")
    @PostMapping
    public ResponseEntity<TasacionResponse> crear(@Valid @RequestBody TasacionRequest request,
                                                  @AuthenticationPrincipal Jwt jwt) {
        TasacionResponse creada = tasacionService.crear(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri()).body(creada);
    }

    @Operation(summary = "Buscar tasaciones", description = "**Roles:** ADMIN, VENDEDOR.")
    @GetMapping
    public Page<TasacionResponse> buscar(@RequestParam(required = false) Long clienteId,
                                         @RequestParam(required = false) EstadoTasacion estado,
                                         @ParameterObject @PageableDefault(size = 20, sort = "fechaTasacion",
                                                 direction = Sort.Direction.DESC) Pageable pageable) {
        return tasacionService.buscar(clienteId, estado, pageable);
    }

    @Operation(summary = "Ver una tasación", description = "**Roles:** ADMIN, VENDEDOR.")
    @GetMapping("/{id}")
    public TasacionResponse obtener(@PathVariable Long id) {
        return tasacionService.obtener(id);
    }

    @Operation(summary = "Registrar si el cliente acepta o rechaza la tasación", description = "**Roles:** ADMIN, VENDEDOR.")
    @PatchMapping("/{id}/estado")
    public TasacionResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoTasacionRequest request) {
        return tasacionService.cambiarEstado(id, request.estado());
    }
}
