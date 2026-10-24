package com.concesionaria.venta.cotizacion.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.cotizacion.dto.CotizacionResponse;
import com.concesionaria.venta.cotizacion.dto.CrearCotizacionRequest;
import com.concesionaria.venta.cotizacion.dto.ResponderCotizacionRequest;
import com.concesionaria.venta.cotizacion.dto.SolicitarCotizacionRequest;
import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;
import com.concesionaria.venta.cotizacion.service.CotizacionService;
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

import java.net.URI;

@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    /** RF-18: el cliente pide una cotización desde el portal. */
    @PostMapping("/solicitudes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<CotizacionResponse> solicitar(@Valid @RequestBody SolicitarCotizacionRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        CotizacionResponse creada = cotizacionService.solicitar(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ubicacion(creada.id())).body(creada);
    }

    /** RF-19: el vendedor crea una cotización para un cliente del local. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<CotizacionResponse> crear(@Valid @RequestBody CrearCotizacionRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        CotizacionResponse creada = cotizacionService.crear(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ubicacion(creada.id())).body(creada);
    }

    /** RF-19 */
    @PutMapping("/{id}/respuesta")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public CotizacionResponse responder(@PathVariable Long id, @Valid @RequestBody ResponderCotizacionRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return cotizacionService.responder(id, request, UsuarioActual.desde(jwt));
    }

    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public CotizacionResponse aceptar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return cotizacionService.aceptar(id, UsuarioActual.desde(jwt));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public CotizacionResponse rechazar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return cotizacionService.rechazar(id, UsuarioActual.desde(jwt));
    }

    /** ?estado=SOLICITADA&sinAsignar=true: las pedidas desde el portal que nadie atendió. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public Page<CotizacionResponse> buscar(@RequestParam(required = false) EstadoCotizacion estado,
                                           @RequestParam(required = false) Long clienteId,
                                           @RequestParam(required = false) Long vendedorId,
                                           @RequestParam(defaultValue = "false") boolean sinAsignar,
                                           @PageableDefault(size = 20, sort = "fechaSolicitud",
                                                   direction = Sort.Direction.DESC) Pageable pageable) {
        return cotizacionService.buscar(estado, clienteId, vendedorId, sinAsignar, pageable);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public Page<CotizacionResponse> mias(@AuthenticationPrincipal Jwt jwt,
                                         @PageableDefault(size = 20, sort = "fechaSolicitud",
                                                 direction = Sort.Direction.DESC) Pageable pageable) {
        return cotizacionService.delCliente(UsuarioActual.desde(jwt), pageable);
    }

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
