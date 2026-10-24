package com.concesionaria.reporte.reporte.service;

import com.concesionaria.reporte.cuota.dto.ReporteCartera;
import com.concesionaria.reporte.cuota.service.CuotaReporteService;
import com.concesionaria.reporte.reporte.dto.Dashboard;
import com.concesionaria.reporte.reporte.dto.Dashboard.PorMes;
import com.concesionaria.reporte.reporte.dto.Dashboard.TopModelo;
import com.concesionaria.reporte.vehiculo.service.VehiculoReporteService;
import com.concesionaria.reporte.venta.VentaReporte;
import com.concesionaria.reporte.venta.dto.ReporteVentas;
import com.concesionaria.reporte.venta.service.VentaReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** RF-42: junta ventas, inventario y cartera en un solo tablero. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VentaReporteService ventaService;
    private final VehiculoReporteService vehiculoService;
    private final CuotaReporteService cuotaService;

    public Dashboard dashboard(LocalDate desde, LocalDate hasta) {
        List<VentaReporte> ventas = ventaService.confirmadasDelPeriodo(desde, hasta);
        ReporteVentas reporte = ventaService.reporte(desde, hasta, null);
        ReporteCartera cartera = cuotaService.cartera();

        List<TopModelo> topModelos = ventas.stream()
                .collect(Collectors.groupingBy(v -> v.getMarca() + "|" + v.getModelo()))
                .values().stream()
                .map(g -> new TopModelo(g.getFirst().getModeloId(), g.getFirst().getMarca(), g.getFirst().getModelo(),
                        g.size(), VentaReporteService.sumar(g, VentaReporte::getPrecioFinal)))
                .sorted(Comparator.comparing(TopModelo::unidades).thenComparing(TopModelo::ingresos).reversed())
                .limit(5)
                .toList();

        // Todos los meses del período, aunque no haya ventas
        Map<YearMonth, List<VentaReporte>> porMes = ventas.stream()
                .collect(Collectors.groupingBy(v -> YearMonth.from(v.getFechaVenta()), TreeMap::new, Collectors.toList()));
        List<PorMes> meses = YearMonth.from(desde).atDay(1).datesUntil(hasta.plusDays(1), java.time.Period.ofMonths(1))
                .map(YearMonth::from)
                .map(mes -> {
                    List<VentaReporte> delMes = porMes.getOrDefault(mes, List.of());
                    return new PorMes(mes.toString(), delMes.size(), VentaReporteService.sumar(delMes, VentaReporte::getPrecioFinal));
                })
                .toList();

        return new Dashboard(
                desde, hasta,
                reporte.montoTotal(),
                reporte.cantidadVentas(),
                reporte.ticketPromedio(),
                reporte.comisiones(),
                topModelos,
                reporte.porVendedor().stream().limit(5).toList(),
                meses,
                vehiculoService.disponibles(),
                cartera.vencido(),
                cartera.morosos().size());
    }
}
