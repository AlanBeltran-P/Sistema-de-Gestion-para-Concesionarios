package com.concesionaria.venta.venta.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.venta.dto.AnulacionRequest;
import com.concesionaria.venta.venta.dto.RegistrarVentaRequest;
import com.concesionaria.venta.venta.dto.VentaResponse;
import com.concesionaria.venta.venta.enums.EstadoVenta;
import com.concesionaria.venta.venta.service.ComprobantePdfService;
import com.concesionaria.venta.venta.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final ComprobantePdfService comprobantePdfService;

    /** RF-23 */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody RegistrarVentaRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        VentaResponse creada = ventaService.registrar(request, UsuarioActual.desde(jwt));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri()).body(creada);
    }

    /** RF-26 */
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public VentaResponse anular(@PathVariable Long id, @Valid @RequestBody AnulacionRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        return ventaService.anular(id, request.motivo(), UsuarioActual.desde(jwt));
    }

    /** ?vendedorId=&desde=2026-01-01&hasta=2026-01-31&estado=CONFIRMADA */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'SISTEMA')")
    public Page<VentaResponse> buscar(@RequestParam(required = false) Long clienteId,
                                      @RequestParam(required = false) Long vendedorId,
                                      @RequestParam(required = false) EstadoVenta estado,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                      @PageableDefault(size = 20, sort = "fechaVenta",
                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return ventaService.buscar(clienteId, vendedorId, estado, desde, hasta, pageable);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public Page<VentaResponse> mias(@AuthenticationPrincipal Jwt jwt,
                                    @PageableDefault(size = 20, sort = "fechaVenta",
                                            direction = Sort.Direction.DESC) Pageable pageable) {
        return ventaService.delCliente(UsuarioActual.desde(jwt), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public VentaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ventaService.obtener(id, UsuarioActual.desde(jwt));
    }

    /** RF-27: contrato / comprobante en PDF. El cliente puede bajar el de sus compras. */
    @GetMapping("/{id}/comprobante")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CLIENTE')")
    public ResponseEntity<byte[]> comprobante(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ComprobantePdfService.Comprobante pdf = comprobantePdfService.generar(id, UsuarioActual.desde(jwt));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(pdf.nombreArchivo()).build().toString())
                .body(pdf.contenido());
    }
}
