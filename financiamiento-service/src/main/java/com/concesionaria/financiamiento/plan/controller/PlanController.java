package com.concesionaria.financiamiento.plan.controller;

import com.concesionaria.common.dto.CambiarActivoRequest;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.financiamiento.plan.dto.PlanRequest;
import com.concesionaria.financiamiento.plan.dto.PlanResponse;
import com.concesionaria.financiamiento.plan.dto.SimulacionRequest;
import com.concesionaria.financiamiento.plan.dto.SimulacionResponse;
import com.concesionaria.financiamiento.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Tag(name = "Planes de financiamiento", description = "Planes y simulador de cuotas (RF-28, RF-29)")
@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /** Público: planes activos. ?incluirInactivos=true solo para ADMIN. */
    @Operation(summary = "Listar planes de financiamiento", description = "Público: planes activos. ?incluirInactivos=true solo para ADMIN.\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping
    public List<PlanResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivos,
                                     @AuthenticationPrincipal Jwt jwt) {
        return planService.listar(incluirInactivos && esAdmin(jwt));
    }

    @Operation(summary = "Ver un plan", description = "**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping("/{id}")
    public PlanResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return planService.obtener(id, esAdmin(jwt));
    }

    /** RF-29: público (también lo usan los vendedores y venta-service). */
    @Operation(summary = "Simular cuotas", description = "RF-29: público (también lo usan los vendedores y venta-service).\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @PostMapping("/simulacion")
    public SimulacionResponse simular(@Valid @RequestBody SimulacionRequest request) {
        return planService.simular(request);
    }

    /** RF-28 */
    @Operation(summary = "Crear un plan", description = "RF-28\n\n**Roles:** ADMIN.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> crear(@Valid @RequestBody PlanRequest request) {
        PlanResponse creado = planService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @Operation(summary = "Editar un plan", description = "**Roles:** ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlanResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return planService.actualizar(id, request);
    }

    @Operation(summary = "Activar o desactivar un plan", description = "**Roles:** ADMIN.")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public PlanResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarActivoRequest request) {
        return planService.cambiarEstado(id, request.activo());
    }

    /** ADMIN o un servicio interno (ven también los planes desactivados). */
    private static boolean esAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }
        UsuarioActual usuario = UsuarioActual.desde(jwt);
        return usuario.esAdmin() || usuario.esSistema();
    }
}
