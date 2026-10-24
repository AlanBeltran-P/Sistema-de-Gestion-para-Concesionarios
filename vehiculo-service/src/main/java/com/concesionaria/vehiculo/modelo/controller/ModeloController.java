package com.concesionaria.vehiculo.modelo.controller;

import com.concesionaria.common.dto.CambiarActivoRequest;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.vehiculo.modelo.dto.ModeloRequest;
import com.concesionaria.vehiculo.modelo.dto.ModeloResponse;
import com.concesionaria.vehiculo.modelo.service.ModeloService;
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
@Tag(name = "Modelos", description = "Modelos de cada marca (RF-07)")
@RestController
@RequestMapping("/api/modelos")
@RequiredArgsConstructor
public class ModeloController {

    private final ModeloService modeloService;

    /** Filtros opcionales: ?marcaId=1&categoriaId=2. ?incluirInactivos=true funciona solo para ADMIN. */
    @Operation(summary = "Listar modelos", description = "Filtros opcionales: ?marcaId=1&categoriaId=2. ?incluirInactivos=true funciona solo para ADMIN.\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping
    public List<ModeloResponse> buscar(@RequestParam(required = false) Long marcaId,
                                       @RequestParam(required = false) Long categoriaId,
                                       @RequestParam(defaultValue = "false") boolean incluirInactivos,
                                       @AuthenticationPrincipal Jwt jwt) {
        boolean esAdmin = jwt != null && UsuarioActual.desde(jwt).esAdmin();
        return modeloService.buscar(marcaId, categoriaId, incluirInactivos && esAdmin);
    }

    @Operation(summary = "Ver un modelo", description = "**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping("/{id}")
    public ModeloResponse obtener(@PathVariable Long id) {
        return modeloService.obtener(id);
    }

    @Operation(summary = "Crear un modelo", description = "**Roles:** ADMIN.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModeloResponse> crear(@Valid @RequestBody ModeloRequest request) {
        ModeloResponse creado = modeloService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @Operation(summary = "Editar un modelo", description = "**Roles:** ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModeloResponse actualizar(@PathVariable Long id, @Valid @RequestBody ModeloRequest request) {
        return modeloService.actualizar(id, request);
    }

    @Operation(summary = "Activar o desactivar un modelo", description = "**Roles:** ADMIN.")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ModeloResponse cambiarActivo(@PathVariable Long id, @Valid @RequestBody CambiarActivoRequest request) {
        return modeloService.cambiarActivo(id, request.activo());
    }
}
