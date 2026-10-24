package com.concesionaria.cliente.historial.controller;

import com.concesionaria.cliente.historial.dto.HistorialResponse;
import com.concesionaria.cliente.historial.service.HistorialService;
import com.concesionaria.common.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RF-17: historial del cliente (cotizaciones, reservas, compras, financiamientos y pagos). */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public HistorialResponse deCliente(@PathVariable Long id) {
        return historialService.deCliente(id);
    }

    /** El cliente ve el suyo. */
    @GetMapping("/me/historial")
    @PreAuthorize("hasRole('CLIENTE')")
    public HistorialResponse propio(@AuthenticationPrincipal Jwt jwt) {
        return historialService.propio(UsuarioActual.desde(jwt));
    }
}
