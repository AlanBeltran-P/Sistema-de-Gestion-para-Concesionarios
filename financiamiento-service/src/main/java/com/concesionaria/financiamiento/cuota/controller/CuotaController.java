package com.concesionaria.financiamiento.cuota.controller;

import com.concesionaria.financiamiento.cuota.dto.CuotaResponse;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.cuota.repository.CuotaRepository;
import com.concesionaria.financiamiento.cuota.service.VencimientoCuotasService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cuotas")
@RequiredArgsConstructor
public class CuotaController {

    private final CuotaRepository cuotaRepository;
    private final VencimientoCuotasService vencimientoService;

    /** RF-32: cuotas pendientes o vencidas, de todos los clientes o de uno. Ej: ?estado=VENCIDA */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public Page<CuotaResponse> buscar(@RequestParam(required = false) EstadoCuota estado,
                                      @RequestParam(required = false) Long clienteId,
                                      @RequestParam(required = false) Long financiamientoId,
                                      @PageableDefault(size = 20, sort = "fechaVencimiento",
                                              direction = Sort.Direction.ASC) Pageable pageable) {
        return cuotaRepository.buscar(estado, clienteId, financiamientoId, pageable).map(CuotaResponse::desde);
    }

    /**
     * RF-33: corre a mano el proceso diario de vencimientos y mora (normalmente lo hace el job).
     * ?fecha=2026-12-31 lo corre como si fuera ese día.
     */
    @PostMapping("/procesar-vencimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public VencimientoCuotasService.Resultado procesar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return vencimientoService.procesar(fecha != null ? fecha : LocalDate.now());
    }
}
