package com.concesionaria.venta.reserva.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.reserva.dto.CrearReservaRequest;
import com.concesionaria.venta.reserva.dto.ReservaResponse;
import com.concesionaria.venta.reserva.enums.EstadoReserva;
import com.concesionaria.venta.reserva.service.ReservaService;
import com.concesionaria.venta.reserva.service.VencimientosJob;
import com.concesionaria.venta.venta.dto.AnulacionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

@Tag(name = "Reservas", description = "Reservas con seña y vencimiento (RF-21, RF-22, RF-26)")
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final VencimientosJob vencimientosJob;

    /** RF-21 */
    @Operation(summary = "Reservar un vehículo con seña", description = "RF-21\n\n**Roles:** ADMIN, VENDEDOR, CLIENTE.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody CrearReservaRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        ReservaResponse creada = reservaService.crear(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri()).body(creada);
    }

    /** RF-26 */
    @Operation(summary = "Anular una reserva", description = "RF-26\n\n**Roles:** ADMIN.")
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public ReservaResponse anular(@PathVariable Long id, @Valid @RequestBody AnulacionRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return reservaService.anular(id, request.motivo(), UsuarioActual.desde(jwt));
    }

    /**
     * RF-22: corre a mano el proceso de vencimientos (normalmente cada 5 minutos).
     * ?fecha=2026-12-31T10:00:00 lo corre como si fuera ese momento.
     */
    @Operation(summary = "Correr ahora el vencimiento de reservas y cotizaciones", description = "RF-22: corre a mano el proceso de vencimientos (normalmente cada 5 minutos). ?fecha=2026-12-31T10:00:00 lo corre como si fuera ese momento.\n\n**Roles:** ADMIN.")
    @PostMapping("/procesar-vencimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public VencimientosJob.Resultado procesarVencimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        return vencimientosJob.procesar(fecha != null ? fecha : LocalDateTime.now());
    }

    @Operation(summary = "Buscar reservas", description = "**Roles:** ADMIN, VENDEDOR.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public Page<ReservaResponse> buscar(@RequestParam(required = false) Long clienteId,
                                        @RequestParam(required = false) Long vehiculoId,
                                        @RequestParam(required = false) EstadoReserva estado,
                                        @ParameterObject @PageableDefault(size = 20, sort = "fechaReserva",
                                                direction = Sort.Direction.DESC) Pageable pageable) {
        return reservaService.buscar(clienteId, vehiculoId, estado, pageable);
    }

    @Operation(summary = "Mis reservas", description = "**Roles:** CLIENTE.")
    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public Page<ReservaResponse> mias(@AuthenticationPrincipal Jwt jwt,
                                      @ParameterObject @PageableDefault(size = 20, sort = "fechaReserva",
                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return reservaService.delCliente(UsuarioActual.desde(jwt), pageable);
    }

    @Operation(summary = "Ver una reserva", description = "**Roles:** ADMIN, VENDEDOR, CLIENTE.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public ReservaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return reservaService.obtener(id, UsuarioActual.desde(jwt));
    }
}
