package com.concesionaria.reporte.venta.service;

import com.concesionaria.reporte.evento.Eventos;
import com.concesionaria.reporte.evento.service.RegistroEventos;
import com.concesionaria.reporte.venta.VentaReporte;
import com.concesionaria.reporte.venta.dto.ReporteVentas;
import com.concesionaria.reporte.venta.dto.ReporteVentas.PorGrupo;
import com.concesionaria.reporte.venta.dto.ReporteVentas.PorVendedor;
import com.concesionaria.reporte.venta.dto.VentaDetalle;
import com.concesionaria.reporte.venta.repository.VentaReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Copia de las ventas (con los eventos de venta-service) y reporte RF-39. */
@Service
@RequiredArgsConstructor
public class VentaReporteService {

    public static final String CONFIRMADA = "CONFIRMADA";

    private final VentaReporteRepository repository;
    private final RegistroEventos registroEventos;

    /** venta.confirmada y venta.anulada traen la foto completa: se pisa la copia. */
    @Transactional
    public void aplicar(Eventos.Venta e) {
        if (!registroEventos.marcarNuevo(e.eventoId(), "venta." + e.tipo())) {
            return;
        }
        VentaReporte v = repository.findByVentaId(e.ventaId())
                .orElseGet(() -> VentaReporte.builder().ventaId(e.ventaId()).build());
        if (v.getUltimaActualizacion() != null && e.fecha().isBefore(v.getUltimaActualizacion())) {
            return;
        }
        v.setNumeroComprobante(e.numeroComprobante());
        v.setVendedorId(e.vendedorId());
        v.setVendedorNombre(e.vendedorNombre());
        v.setClienteId(e.clienteId());
        v.setVehiculoId(e.vehiculoId());
        v.setModeloId(e.modeloId());
        v.setMarca(e.marca());
        v.setModelo(e.modelo());
        v.setCategoria(e.categoria());
        v.setAnio(e.anio());
        v.setCondicion(e.condicion());
        v.setFormaPago(e.formaPago());
        v.setConTradeIn(e.conTradeIn());
        v.setPrecioFinal(e.precioFinal());
        v.setMontoComision(e.montoComision() != null ? e.montoComision() : BigDecimal.ZERO);
        v.setEstado(e.estado());
        v.setFechaVenta(e.fechaVenta());
        v.setFechaAnulacion(e.fechaAnulacion());
        v.setUltimaActualizacion(e.fecha());
        repository.save(v);
    }

    /** RF-39. El período incluye los dos días (hasta es inclusivo). */
    @Transactional(readOnly = true)
    public ReporteVentas reporte(LocalDate desde, LocalDate hasta, Long vendedorId) {
        List<VentaReporte> todas = delPeriodo(desde, hasta, vendedorId);
        List<VentaReporte> confirmadas = confirmadas(todas);

        List<PorVendedor> porVendedor = confirmadas.stream()
                .collect(Collectors.groupingBy(VentaReporte::getVendedorId))
                .values().stream()
                .map(ventas -> new PorVendedor(ventas.getFirst().getVendedorId(), ventas.getFirst().getVendedorNombre(),
                        ventas.size(), sumar(ventas, VentaReporte::getPrecioFinal),
                        sumar(ventas, VentaReporte::getMontoComision)))
                .sorted(Comparator.comparing(PorVendedor::monto).reversed())
                .toList();

        return new ReporteVentas(
                desde, hasta,
                confirmadas.size(),
                todas.size() - confirmadas.size(),
                sumar(confirmadas, VentaReporte::getPrecioFinal),
                promedio(confirmadas),
                sumar(confirmadas, VentaReporte::getMontoComision),
                confirmadas.stream().filter(VentaReporte::isConTradeIn).count(),
                porVendedor,
                agrupar(confirmadas, VentaReporte::getFormaPago),
                agrupar(confirmadas, VentaReporte::getCondicion));
    }

    @Transactional(readOnly = true)
    public List<VentaDetalle> detalle(LocalDate desde, LocalDate hasta, Long vendedorId) {
        return delPeriodo(desde, hasta, vendedorId).stream().map(VentaDetalle::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<VentaReporte> confirmadasDelPeriodo(LocalDate desde, LocalDate hasta) {
        return confirmadas(delPeriodo(desde, hasta, null));
    }

    private List<VentaReporte> delPeriodo(LocalDate desde, LocalDate hasta, Long vendedorId) {
        return repository.delPeriodo(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), vendedorId);
    }

    private static List<VentaReporte> confirmadas(List<VentaReporte> ventas) {
        return ventas.stream().filter(v -> CONFIRMADA.equals(v.getEstado())).toList();
    }

    private static List<PorGrupo> agrupar(List<VentaReporte> ventas, Function<VentaReporte, String> clave) {
        Map<String, List<VentaReporte>> grupos = ventas.stream()
                .collect(Collectors.groupingBy(v -> clave.apply(v) != null ? clave.apply(v) : "-"));
        return grupos.entrySet().stream()
                .map(g -> new PorGrupo(g.getKey(), g.getValue().size(), sumar(g.getValue(), VentaReporte::getPrecioFinal)))
                .sorted(Comparator.comparing(PorGrupo::grupo))
                .toList();
    }

    public static BigDecimal sumar(List<VentaReporte> ventas, Function<VentaReporte, BigDecimal> campo) {
        return ventas.stream().map(campo).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal promedio(List<VentaReporte> ventas) {
        return ventas.isEmpty() ? BigDecimal.ZERO
                : sumar(ventas, VentaReporte::getPrecioFinal).divide(BigDecimal.valueOf(ventas.size()), 2, RoundingMode.HALF_UP);
    }
}
