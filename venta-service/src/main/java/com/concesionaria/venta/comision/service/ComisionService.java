package com.concesionaria.venta.comision.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.venta.comision.Comision;
import com.concesionaria.venta.comision.dto.ComisionResponse;
import com.concesionaria.venta.comision.dto.ResumenComisionesResponse;
import com.concesionaria.venta.comision.dto.TotalesComision;
import com.concesionaria.venta.comision.enums.EstadoComision;
import com.concesionaria.venta.comision.repository.ComisionRepository;
import com.concesionaria.venta.config.VentasProperties;
import com.concesionaria.venta.venta.Venta;
import com.concesionaria.venta.venta.service.ReglasPrecio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * RF-25: la comisión es un % del precio final (después del descuento), distinto para autos
 * nuevos y usados. Se guarda el % usado por si la configuración cambia después.
 */
@Service
@RequiredArgsConstructor
public class ComisionService {

    private final ComisionRepository comisionRepository;
    private final VentasProperties propiedades;

    /** Se llama al registrar la venta; queda PENDIENTE hasta que el ADMIN la marca como pagada. */
    public Comision calcular(Venta venta, String condicionVehiculo) {
        BigDecimal porcentaje = "USADO".equals(condicionVehiculo)
                ? propiedades.comision().porcentajeUsado()
                : propiedades.comision().porcentajeNuevo();
        return Comision.builder()
                .vendedorId(venta.getVendedorId())
                .baseCalculo(venta.getPrecioFinal())
                .porcentaje(porcentaje)
                .monto(ReglasPrecio.porcentaje(venta.getPrecioFinal(), porcentaje))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ComisionResponse> buscar(Long vendedorId, EstadoComision estado, LocalDate desde, LocalDate hasta,
                                         Pageable pageable) {
        validarPeriodo(desde, hasta);
        return comisionRepository.buscar(vendedorId, estado, inicio(desde), fin(hasta), pageable)
                .map(ComisionResponse::desde);
    }

    @Transactional(readOnly = true)
    public ResumenComisionesResponse resumen(Long vendedorId, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        TotalesComision t = comisionRepository.totales(vendedorId, inicio(desde), fin(hasta));
        return new ResumenComisionesResponse(vendedorId, t.cantidadVentas(), t.totalPendiente(), t.totalPagado(),
                t.totalPendiente().add(t.totalPagado()));
    }

    @Transactional
    public ComisionResponse marcarPagada(Long id) {
        Comision comision = comisionRepository.findWithVentaById(id)
                .orElseThrow(() -> new NoEncontradoException("Comisión " + id + " no encontrada"));
        if (comision.getEstado() != EstadoComision.PENDIENTE) {
            throw new ConflictoException("La comisión está " + comision.getEstado());
        }
        comision.setEstado(EstadoComision.PAGADA);
        comision.setFechaPago(LocalDateTime.now());
        return ComisionResponse.desde(comision);
    }

    private static void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("'desde' no puede ser posterior a 'hasta'");
        }
    }

    private static LocalDateTime inicio(LocalDate desde) {
        return desde != null ? desde.atStartOfDay() : null;
    }

    /** hasta es inclusivo: se toma hasta el comienzo del día siguiente. */
    private static LocalDateTime fin(LocalDate hasta) {
        return hasta != null ? hasta.plusDays(1).atStartOfDay() : null;
    }
}
