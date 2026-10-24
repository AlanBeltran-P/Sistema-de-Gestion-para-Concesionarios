package com.concesionaria.financiamiento.pago.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.financiamiento.pago.dto.PagoResponse;
import com.concesionaria.financiamiento.pago.dto.RegistrarPagoRequest;
import com.concesionaria.financiamiento.pago.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    /** RF-31: pago del anticipo o de una cuota. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse registrar(@Valid @RequestBody RegistrarPagoRequest request, @AuthenticationPrincipal Jwt jwt) {
        return pagoService.registrar(request, UsuarioActual.desde(jwt));
    }

    /** SISTEMA: cliente-service arma el historial del cliente (RF-17). */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public Page<PagoResponse> buscar(@RequestParam(required = false) Long financiamientoId,
                                     @RequestParam(required = false) Long clienteId,
                                     @PageableDefault(size = 20, sort = "fechaPago",
                                             direction = Sort.Direction.DESC) Pageable pageable) {
        return pagoService.buscar(financiamientoId, clienteId, pageable);
    }
}
