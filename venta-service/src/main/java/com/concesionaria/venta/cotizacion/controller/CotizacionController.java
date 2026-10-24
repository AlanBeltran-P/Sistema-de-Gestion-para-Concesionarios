package com.concesionaria.venta.cotizacion.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.cotizacion.dto.CotizacionResponse;
import com.concesionaria.venta.cotizacion.dto.CrearCotizacionRequest;
import com.concesionaria.venta.cotizacion.dto.ResponderCotizacionRequest;
import com.concesionaria.venta.cotizacion.dto.SolicitarCotizacionRequest;
import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;
import com.concesionaria.venta.cotizacion.service.CotizacionService;
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

import java.net.URI;

@Tag(name = "Cotizaciones", description = "Pedidos de precio y respuestas (RF-18, RF-19)")
@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    /** RF-18: el cliente pide una cotización desde el portal. */
    @Operation(summary = "Pedir una cotización (portal)", description = "RF-18: el cliente pide una cotización desde el portal.\n\n**Roles:** CLIENTE.")
    @PostMapping("/solicitudes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<CotizacionResponse> solicitar(@Valid @RequestBody SolicitarCotizacionRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        CotizacionResponse creada = cotizacionService.solicitar(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ubicacion(creada.id())).body(creada);
    }

    /** RF-19: el vendedor crea una cotización para un cliente del local. */
    @Operation(summary = "Crear una cotización para un cliente del local", description = "RF-19: el vendedor crea una cotización para un cliente del local.\n\n**Roles:** ADMIN, VENDEDOR.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<CotizacionResponse> crear(@Valid @RequestBody CrearCotizacionRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        CotizacionResponse creada = cotizacionService.crear(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ubicacion(creada.id())).body(creada);
    }

    /** RF-19 */
    @Operation(summary = "Responder una cotización", description = "RF-19\n\n**Roles:** ADMIN, VENDEDOR.")
    @PutMapping("/{id}/respuesta")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public CotizacionResponse responder(@PathVariable Long id, @Valid @RequestBody ResponderCotizacionRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return cotizacionService.responder(id, request, UsuarioActual.desde(jwt));
    }

    @Operation(summary = "Aceptar una cotización", description = "**Roles:** ADMIN, VENDEDOR, CLIENTE.")
    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public CotizacionResponse aceptar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return cotizacionService.aceptar(id, UsuarioActual.desde(jwt));
    }

    @Operation(summary = "Rechazar una cotización", description = "**Roles:** ADMIN, VENDEDOR, CLIENTE.")
    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public CotizacionResponse rechazar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return cotizacionService.rechazar(id, UsuarioActual.desde(jwt));
    }

    /** ?estado=SOLICITADA&sinAsignar=true: las pedidas desde el portal que nadie atendió. */
    @Operation(summary = "Buscar cotizaciones", description = "?estado=SOLICITADA&sinAsignar=true: las pedidas desde el portal que nadie atendió.\n\n**Roles:** ADMIN, VENDEDOR.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public Page<CotizacionResponse> buscar(@RequestParam(required = false) EstadoCotizacion estado,
                                           @RequestParam(required = false) Long clienteId,
                                           @RequestParam(required = false) Long vendedorId,
                                           @RequestParam(defaultValue = "false") boolean sinAsignar,
                                           @ParameterObject @PageableDefault(size = 20, sort = "fechaSolicitud",
                                                   direction = Sort.Direction.DESC) Pageable pageable) {
        return cotizacionService.buscar(estado, clienteId, vendedorId, sinAsignar, pageable);
    }

    @Operation(summary = "Mis cotizaciones", description = "**Roles:** CLIENTE.")
    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public Page<CotizacionResponse> mias(@AuthenticationPrincipal Jwt jwt,
                                         @ParameterObject @PageableDefault(size = 20, sort = "fechaSolicitud",
                                                 direction = Sort.Direction.DESC) Pageable pageable) {
        return cotizacionService.delCliente(UsuarioActual.desde(jwt), pageable);
    }

    @Operation(summary = "Ver una cotización", description = "**Roles:** ADMIN, VENDEDOR, CLIENTE.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public CotizacionResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return cotizacionService.obtener(id, UsuarioActual.desde(jwt));
    }

    private static URI ubicacion(Long id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/cotizaciones/{id}")
                .buildAndExpand(id).toUri();
    }
}
