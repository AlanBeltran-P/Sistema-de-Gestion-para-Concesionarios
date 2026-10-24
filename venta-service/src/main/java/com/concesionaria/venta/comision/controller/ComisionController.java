package com.concesionaria.venta.comision.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.comision.dto.ComisionResponse;
import com.concesionaria.venta.comision.dto.ResumenComisionesResponse;
import com.concesionaria.venta.comision.enums.EstadoComision;
import com.concesionaria.venta.comision.service.ComisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** RF-25. Un VENDEDOR solo ve sus propias comisiones; el ADMIN ve todas. */
@Tag(name = "Comisiones", description = "Comisiones de los vendedores (RF-25)")
@RestController
@RequestMapping("/api/comisiones")
@RequiredArgsConstructor
public class ComisionController {

    private final ComisionService comisionService;

    @Operation(summary = "Buscar comisiones", description = "**Roles:** ADMIN, VENDEDOR.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public Page<ComisionResponse> buscar(@RequestParam(required = false) Long vendedorId,
                                         @RequestParam(required = false) EstadoComision estado,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                         @ParameterObject @PageableDefault(size = 20, sort = "fechaCalculo",
                                                 direction = Sort.Direction.DESC) Pageable pageable,
                                         @AuthenticationPrincipal Jwt jwt) {
        return comisionService.buscar(vendedorVisible(vendedorId, jwt), estado, desde, hasta, pageable);
    }

    @Operation(summary = "Totales de comisiones", description = "**Roles:** ADMIN, VENDEDOR.")
    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResumenComisionesResponse resumen(@RequestParam(required = false) Long vendedorId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                             @AuthenticationPrincipal Jwt jwt) {
        return comisionService.resumen(vendedorVisible(vendedorId, jwt), desde, hasta);
    }

    @Operation(summary = "Marcar una comisión como pagada", description = "**Roles:** ADMIN.")
    @PatchMapping("/{id}/pagar")
    @PreAuthorize("hasRole('ADMIN')")
    public ComisionResponse pagar(@PathVariable Long id) {
        return comisionService.marcarPagada(id);
    }

    /** El vendedor siempre ve las suyas, aunque pida las de otro. */
    private static Long vendedorVisible(Long pedido, Jwt jwt) {
        UsuarioActual usuario = UsuarioActual.desde(jwt);
        return usuario.esAdmin() ? pedido : usuario.id();
    }
}
