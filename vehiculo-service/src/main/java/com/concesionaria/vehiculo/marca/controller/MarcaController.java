package com.concesionaria.vehiculo.marca.controller;

import com.concesionaria.common.dto.CambiarActivoRequest;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.vehiculo.marca.dto.MarcaRequest;
import com.concesionaria.vehiculo.marca.dto.MarcaResponse;
import com.concesionaria.vehiculo.marca.service.MarcaService;
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

/** RF-07. Lectura pública; alta, edición y activación solo ADMIN. */
@Tag(name = "Marcas", description = "Marcas de vehículos (RF-07)")
@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    /** Por defecto solo las activas. ?incluirInactivas=true funciona solo para ADMIN. */
    @Operation(summary = "Listar marcas", description = "Por defecto solo las activas. ?incluirInactivas=true funciona solo para ADMIN.\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping
    public List<MarcaResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivas,
                                      @AuthenticationPrincipal Jwt jwt) {
        boolean esAdmin = jwt != null && UsuarioActual.desde(jwt).esAdmin();
        return marcaService.listar(incluirInactivas && esAdmin);
    }

    @Operation(summary = "Ver una marca", description = "**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping("/{id}")
    public MarcaResponse obtener(@PathVariable Long id) {
        return marcaService.obtener(id);
    }

    @Operation(summary = "Crear una marca", description = "**Roles:** ADMIN.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody MarcaRequest request) {
        MarcaResponse creada = marcaService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri()).body(creada);
    }

    @Operation(summary = "Editar una marca", description = "**Roles:** ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MarcaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MarcaRequest request) {
        return marcaService.actualizar(id, request);
    }

    @Operation(summary = "Activar o desactivar una marca", description = "**Roles:** ADMIN.")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public MarcaResponse cambiarActivo(@PathVariable Long id, @Valid @RequestBody CambiarActivoRequest request) {
        return marcaService.cambiarActivo(id, request.activo());
    }
}
