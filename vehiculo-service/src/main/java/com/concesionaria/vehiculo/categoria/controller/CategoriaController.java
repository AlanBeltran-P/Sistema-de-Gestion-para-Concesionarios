package com.concesionaria.vehiculo.categoria.controller;

import com.concesionaria.common.dto.CambiarActivoRequest;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.vehiculo.categoria.dto.CategoriaRequest;
import com.concesionaria.vehiculo.categoria.dto.CategoriaResponse;
import com.concesionaria.vehiculo.categoria.service.CategoriaService;
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
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /** Por defecto solo las activas. ?incluirInactivas=true funciona solo para ADMIN. */
    @GetMapping
    public List<CategoriaResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivas,
                                          @AuthenticationPrincipal Jwt jwt) {
        boolean esAdmin = jwt != null && UsuarioActual.desde(jwt).esAdmin();
        return categoriaService.listar(incluirInactivas && esAdmin);
    }

    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return categoriaService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse creada = categoriaService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri()).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaResponse cambiarActivo(@PathVariable Long id, @Valid @RequestBody CambiarActivoRequest request) {
        return categoriaService.cambiarActivo(id, request.activo());
    }
}
