package com.concesionaria.reporte.vehiculo.service;

import com.concesionaria.reporte.evento.Eventos;
import com.concesionaria.reporte.evento.service.RegistroEventos;
import com.concesionaria.reporte.vehiculo.VehiculoReporte;
import com.concesionaria.reporte.vehiculo.dto.ReporteInventario;
import com.concesionaria.reporte.vehiculo.dto.ReporteInventario.RangoAntiguedad;
import com.concesionaria.reporte.vehiculo.dto.ReporteInventario.VehiculoEnStock;
import com.concesionaria.reporte.vehiculo.repository.VehiculoReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Copia del inventario (con los eventos de vehiculo-service) y reporte RF-40. */
@Service
@RequiredArgsConstructor
public class VehiculoReporteService {

    private static final String VENDIDO = "VENDIDO";
    /** Rangos de antigüedad en stock: [desde, hasta] en días. */
    private static final int[][] RANGOS = {{0, 30}, {31, 60}, {61, 90}, {91, 180}, {181, Integer.MAX_VALUE}};

    private final VehiculoReporteRepository repository;
    private final RegistroEventos registroEventos;

    @Transactional
    public void aplicar(Eventos.Vehiculo e) {
        if (!registroEventos.marcarNuevo(e.eventoId(), "vehiculo." + e.tipo())) {
            return;
        }
        VehiculoReporte v = repository.findByVehiculoId(e.vehiculoId())
                .orElseGet(() -> VehiculoReporte.builder().vehiculoId(e.vehiculoId()).build());
        if (v.getUltimaActualizacion() != null && e.fecha().isBefore(v.getUltimaActualizacion())) {
            return;
        }
        v.setVin(e.vin());
        v.setMarca(e.marca());
        v.setModelo(e.modelo());
        v.setCategoria(e.categoria());
        v.setAnio(e.anio());
        v.setCondicion(e.condicion());
        v.setPrecio(e.precio());
        v.setActivo(e.activo());
        v.setFechaIngreso(e.fechaIngreso());
        if (VENDIDO.equals(e.estado())) {
            if (v.getFechaVenta() == null || !VENDIDO.equals(v.getEstado())) {
                v.setFechaVenta(e.fecha());
            }
        } else {
            v.setFechaVenta(null);
        }
        v.setEstado(e.estado());
        v.setUltimaActualizacion(e.fecha());
        repository.save(v);
    }

    /** RF-40 */
    @Transactional(readOnly = true)
    public ReporteInventario inventario() {
        LocalDateTime ahora = LocalDateTime.now();
        List<VehiculoReporte> activos = repository.findByActivoTrue();
        List<VehiculoReporte> enStock = activos.stream().filter(v -> !VENDIDO.equals(v.getEstado())).toList();

        Map<String, Long> porEstado = new TreeMap<>(activos.stream()
                .collect(Collectors.groupingBy(VehiculoReporte::getEstado, Collectors.counting())));
        Map<String, Long> porCondicion = new TreeMap<>(enStock.stream()
                .collect(Collectors.groupingBy(v -> v.getCondicion() != null ? v.getCondicion() : "-", Collectors.counting())));

        List<RangoAntiguedad> antiguedad = new ArrayList<>();
        for (int[] rango : RANGOS) {
            List<VehiculoReporte> delRango = enStock.stream()
                    .filter(v -> dias(v, ahora) >= rango[0] && dias(v, ahora) <= rango[1]).toList();
            String nombre = rango[1] == Integer.MAX_VALUE ? "más de " + (rango[0] - 1) + " días"
                    : rango[0] + " a " + rango[1] + " días";
            antiguedad.add(new RangoAntiguedad(nombre, delRango.size(), sumarPrecios(delRango)));
        }

        List<VehiculoEnStock> masAntiguos = enStock.stream()
                .sorted(Comparator.comparing(VehiculoReporte::getFechaIngreso))
                .limit(10)
                .map(v -> new VehiculoEnStock(v.getVehiculoId(), v.getVin(), v.getMarca(), v.getModelo(), v.getAnio(),
                        v.getCondicion(), v.getEstado(), v.getPrecio(), v.getFechaIngreso(), dias(v, ahora)))
                .toList();
        double promedio = enStock.stream().mapToLong(v -> dias(v, ahora)).average().orElse(0);

        return new ReporteInventario(LocalDate.now(), enStock.size(), sumarPrecios(enStock),
                Math.round(promedio * 10) / 10.0, porEstado, porCondicion, antiguedad, masAntiguos);
    }

    @Transactional(readOnly = true)
    public long disponibles() {
        return repository.countByActivoTrueAndEstado("DISPONIBLE");
    }

    private static long dias(VehiculoReporte v, LocalDateTime ahora) {
        return ChronoUnit.DAYS.between(v.getFechaIngreso(), ahora);
    }

    private static BigDecimal sumarPrecios(List<VehiculoReporte> vehiculos) {
        return vehiculos.stream().map(v -> v.getPrecio() != null ? v.getPrecio() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
