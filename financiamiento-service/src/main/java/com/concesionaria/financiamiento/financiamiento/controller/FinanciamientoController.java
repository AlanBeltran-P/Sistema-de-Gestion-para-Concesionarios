package com.concesionaria.financiamiento.financiamiento.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.financiamiento.financiamiento.dto.EstadoCuentaResponse;
import com.concesionaria.financiamiento.financiamiento.dto.FinanciamientoResumenResponse;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;
import com.concesionaria.financiamiento.financiamiento.service.FinanciamientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Los financiamientos se crean solos al confirmar una venta financiada (RF-30). */
@RestController
@RequestMapping("/api/financiamientos")
@RequiredArgsConstructor
public class FinanciamientoController {

    private final FinanciamientoService financiamientoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public Page<FinanciamientoResumenResponse> buscar(@RequestParam(required = false) Long clienteId,
                                                      @RequestParam(required = false) EstadoFinanciamiento estado,
                                                      @PageableDefault(size = 20, sort = "fechaCreacion",
                                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return financiamientoService.buscar(clienteId, estado, pageable);
    }

    /** RF-32: estado de cuenta del cliente logueado. */
    @GetMapping("/mios")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<EstadoCuentaResponse> mios(@AuthenticationPrincipal Jwt jwt) {
        return financiamientoService.delCliente(UsuarioActual.desde(jwt));
    }

    /** RF-32 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public EstadoCuentaResponse estadoCuenta(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return financiamientoService.estadoCuenta(id, UsuarioActual.desde(jwt));
    }

    @GetMapping("/venta/{ventaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public EstadoCuentaResponse deVenta(@PathVariable Long ventaId) {
        return financiamientoService.deVenta(ventaId);
    }
}
