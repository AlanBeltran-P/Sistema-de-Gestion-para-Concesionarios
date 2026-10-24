package com.concesionaria.financiamiento.plan.controller;

import com.concesionaria.common.dto.CambiarActivoRequest;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.financiamiento.plan.dto.PlanRequest;
import com.concesionaria.financiamiento.plan.dto.PlanResponse;
import com.concesionaria.financiamiento.plan.dto.SimulacionRequest;
import com.concesionaria.financiamiento.plan.dto.SimulacionResponse;
import com.concesionaria.financiamiento.plan.service.PlanService;
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

@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /** Público: planes activos. ?incluirInactivos=true solo para ADMIN. */
    @GetMapping
    public List<PlanResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivos,
                                     @AuthenticationPrincipal Jwt jwt) {
        return planService.listar(incluirInactivos && esAdmin(jwt));
    }

    @GetMapping("/{id}")
    public PlanResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return planService.obtener(id, esAdmin(jwt));
    }

    /** RF-29: público (también lo usan los vendedores y venta-service). */
    @PostMapping("/simulacion")
    public SimulacionResponse simular(@Valid @RequestBody SimulacionRequest request) {
        return planService.simular(request);
    }

    /** RF-28 */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> crear(@Valid @RequestBody PlanRequest request) {
        PlanResponse creado = planService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlanResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return planService.actualizar(id, request);
    }

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
