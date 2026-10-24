package com.concesionaria.financiamiento.cuota.controller;

import com.concesionaria.financiamiento.cuota.dto.CuotaResponse;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.cuota.repository.CuotaRepository;
import com.concesionaria.financiamiento.cuota.service.VencimientoCuotasService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Cuotas", description = "Cuotas pendientes o vencidas y proceso de vencimientos (RF-32, RF-33)")
@RestController
@RequestMapping("/api/cuotas")
@RequiredArgsConstructor
public class CuotaController {

    private final CuotaRepository cuotaRepository;
    private final VencimientoCuotasService vencimientoService;

    /** RF-32: cuotas pendientes o vencidas, de todos los clientes o de uno. Ej: ?estado=VENCIDA */
    @Operation(summary = "Buscar cuotas (pendientes, vencidas...)", description = "RF-32: cuotas pendientes o vencidas, de todos los clientes o de uno. Ej: ?estado=VENCIDA\n\n**Roles:** ADMIN, VENDEDOR.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public Page<CuotaResponse> buscar(@RequestParam(required = false) EstadoCuota estado,
                                      @RequestParam(required = false) Long clienteId,
                                      @RequestParam(required = false) Long financiamientoId,
                                      @ParameterObject @PageableDefault(size = 20, sort = "fechaVencimiento",
                                              direction = Sort.Direction.ASC) Pageable pageable) {
        return cuotaRepository.buscar(estado, clienteId, financiamientoId, pageable).map(CuotaResponse::desde);
    }

    /**
     * RF-33: corre a mano el proceso diario de vencimientos y mora (normalmente lo hace el job).
     * ?fecha=2026-12-31 lo corre como si fuera ese día.
     */
    @Operation(summary = "Correr ahora el proceso de vencimientos y mora", description = "RF-33: corre a mano el proceso diario de vencimientos y mora (normalmente lo hace el job). ?fecha=2026-12-31 lo corre como si fuera ese día.\n\n**Roles:** ADMIN.")
    @PostMapping("/procesar-vencimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public VencimientoCuotasService.Resultado procesar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return vencimientoService.procesar(fecha != null ? fecha : LocalDate.now());
    }
}
