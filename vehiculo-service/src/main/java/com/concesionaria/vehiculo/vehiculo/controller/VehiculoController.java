package com.concesionaria.vehiculo.vehiculo.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.vehiculo.vehiculo.dto.ActualizarVehiculoRequest;
import com.concesionaria.vehiculo.vehiculo.dto.CambiarEstadoVehiculoRequest;
import com.concesionaria.vehiculo.vehiculo.dto.CrearVehiculoRequest;
import com.concesionaria.vehiculo.vehiculo.dto.FiltroVehiculos;
import com.concesionaria.vehiculo.vehiculo.dto.VehiculoResponse;
import com.concesionaria.vehiculo.vehiculo.dto.VehiculoResumenResponse;
import com.concesionaria.vehiculo.vehiculo.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Vehículos", description = "Inventario y catálogo público (RF-08, RF-09, RF-11 a RF-13)")
@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    /**
     * RF-12: catálogo con filtros y paginado (público).
     * Ej: /api/vehiculos?marcaId=1&precioMax=25000000&condicion=USADO&page=0&size=12&sort=precio,asc
     */
    @Operation(summary = "Catálogo de vehículos con filtros", description = "RF-12: catálogo con filtros y paginado (público). Ej: /api/vehiculos?marcaId=1&precioMax=25000000&condicion=USADO&page=0&size=12&sort=precio,asc\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping
    public Page<VehiculoResumenResponse> buscar(@ParameterObject @ModelAttribute FiltroVehiculos filtro,
                                                @ParameterObject @PageableDefault(size = 12, sort = "fechaIngreso",
                                                        direction = Sort.Direction.DESC) Pageable pageable,
                                                @AuthenticationPrincipal Jwt jwt) {
        return vehiculoService.buscar(filtro, pageable, esEmpleado(jwt));
    }

    /** RF-13: detalle (público). */
    @Operation(summary = "Ver el detalle de un vehículo", description = "RF-13: detalle (público).\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return vehiculoService.obtener(id, esEmpleado(jwt));
    }

    /** RF-08. SISTEMA: venta-service da de alta el auto recibido por trade-in (RF-24). */
    @Operation(summary = "Registrar un vehículo", description = "RF-08. SISTEMA: venta-service da de alta el auto recibido por trade-in (RF-24).\n\n**Roles:** ADMIN, VENDEDOR.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody CrearVehiculoRequest request) {
        VehiculoResponse creado = vehiculoService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    /** RF-09 */
    @Operation(summary = "Editar un vehículo", description = "RF-09\n\n**Roles:** ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VehiculoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarVehiculoRequest request) {
        return vehiculoService.actualizar(id, request);
    }

    /** RF-11. SISTEMA: venta-service al reservar, vender, anular o vencer una reserva. */
    @Operation(summary = "Cambiar el estado (disponible, reservado, vendido)", description = "RF-11. SISTEMA: venta-service al reservar, vender, anular o vencer una reserva.\n\n**Roles:** ADMIN, VENDEDOR.")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public VehiculoResponse cambiarEstado(@PathVariable Long id,
                                          @Valid @RequestBody CambiarEstadoVehiculoRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        return vehiculoService.cambiarEstado(id, request.estado(), request.estadoEsperado(), UsuarioActual.desde(jwt));
    }

    /** RF-09: baja lógica. SISTEMA: venta-service al anular una venta con trade-in. */
    @Operation(summary = "Dar de baja un vehículo", description = "RF-09: baja lógica. SISTEMA: venta-service al anular una venta con trade-in.\n\n**Roles:** ADMIN.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SISTEMA')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Long id) {
        vehiculoService.darDeBaja(id);
    }

    @Operation(summary = "Reactivar un vehículo dado de baja", description = "**Roles:** ADMIN.")
    @PostMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SISTEMA')")
    public VehiculoResponse reactivar(@PathVariable Long id) {
        return vehiculoService.reactivar(id);
    }

    private boolean esEmpleado(Jwt jwt) {
        return jwt != null && UsuarioActual.desde(jwt).esEmpleado();
    }
}
