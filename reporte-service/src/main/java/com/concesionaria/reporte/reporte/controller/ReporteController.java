package com.concesionaria.reporte.reporte.controller;

import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.reporte.cuota.dto.ReporteCartera;
import com.concesionaria.reporte.cuota.service.CuotaReporteService;
import com.concesionaria.reporte.reporte.dto.Dashboard;
import com.concesionaria.reporte.reporte.service.DashboardService;
import com.concesionaria.reporte.vehiculo.dto.ReporteInventario;
import com.concesionaria.reporte.vehiculo.service.VehiculoReporteService;
import com.concesionaria.reporte.venta.dto.ReporteVentas;
import com.concesionaria.reporte.venta.dto.VentaDetalle;
import com.concesionaria.reporte.venta.service.VentaReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * RF-39 a RF-42. Solo ADMIN. Las fechas van en formato 2026-10-31 y "hasta" es inclusivo.
 * Los datos salen de la copia que arma este servicio con los eventos (no consulta a los demás).
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final VentaReporteService ventaService;
    private final VehiculoReporteService vehiculoService;
    private final CuotaReporteService cuotaService;
    private final DashboardService dashboardService;

    /** RF-39. Por defecto, el mes actual. */
    @GetMapping("/ventas")
    public ReporteVentas ventas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                @RequestParam(required = false) Long vendedorId) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        validar(inicio, fin);
        return ventaService.reporte(inicio, fin, vendedorId);
    }

    /** RF-39: venta por venta. */
    @GetMapping("/ventas/detalle")
    public List<VentaDetalle> ventasDetalle(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                            @RequestParam(required = false) Long vendedorId) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        validar(inicio, fin);
        return ventaService.detalle(inicio, fin, vendedorId);
    }

    /** RF-40 */
    @GetMapping("/inventario")
    public ReporteInventario inventario() {
        return vehiculoService.inventario();
    }

    /** RF-41 */
    @GetMapping("/cartera")
    public ReporteCartera cartera() {
        return cuotaService.cartera();
    }

    /** RF-42. Por defecto, los últimos 12 meses. */
    @GetMapping("/dashboard")
    public Dashboard dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusMonths(11).withDayOfMonth(1);
        validar(inicio, fin);
        return dashboardService.dashboard(inicio, fin);
    }

    private static void validar(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("'desde' no puede ser posterior a 'hasta'");
        }
        if (desde.plusYears(5).isBefore(hasta)) {
            throw new SolicitudInvalidaException("El período no puede ser mayor a 5 años");
        }
    }
}
