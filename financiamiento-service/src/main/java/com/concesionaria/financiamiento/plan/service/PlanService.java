package com.concesionaria.financiamiento.plan.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.financiamiento.plan.PlanFinanciamiento;
import com.concesionaria.financiamiento.plan.dto.PlanRequest;
import com.concesionaria.financiamiento.plan.dto.PlanResponse;
import com.concesionaria.financiamiento.plan.dto.SimulacionRequest;
import com.concesionaria.financiamiento.plan.dto.SimulacionResponse;
import com.concesionaria.financiamiento.plan.repository.PlanFinanciamientoRepository;
import com.concesionaria.financiamiento.plan.service.CalculadoraCuotas.CuotaCalculada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;

/** RF-28 (planes) y RF-29 (simulación). */
@Service
@RequiredArgsConstructor
public class PlanService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final PlanFinanciamientoRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> listar(boolean incluirInactivos) {
        var planes = incluirInactivos
                ? planRepository.findAllByOrderByNombreAsc()
                : planRepository.findByActivoTrueOrderByNombreAsc();
        return planes.stream().map(PlanResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse obtener(Long id, boolean verInactivos) {
        PlanFinanciamiento plan = buscar(id);
        if (!plan.isActivo() && !verInactivos) {
            throw new NoEncontradoException("Plan " + id + " no encontrado");
        }
        return PlanResponse.desde(plan);
    }

    @Transactional
    public PlanResponse crear(PlanRequest request) {
        String nombre = request.nombre().trim();
        if (planRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe un plan llamado " + nombre);
        }
        PlanFinanciamiento plan = new PlanFinanciamiento();
        aplicar(plan, request, nombre);
        return PlanResponse.desde(planRepository.save(plan));
    }

    /** Los financiamientos ya otorgados no cambian: copiaron la tasa y la mora al crearse. */
    @Transactional
    public PlanResponse actualizar(Long id, PlanRequest request) {
        PlanFinanciamiento plan = buscar(id);
        String nombre = request.nombre().trim();
        if (planRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe un plan llamado " + nombre);
        }
        aplicar(plan, request, nombre);
        return PlanResponse.desde(plan);
    }

    @Transactional
    public PlanResponse cambiarEstado(Long id, boolean activo) {
        PlanFinanciamiento plan = buscar(id);
        plan.setActivo(activo);
        return PlanResponse.desde(plan);
    }

    /**
     * RF-29. También la usa venta-service para validar una venta financiada antes de registrarla.
     * El anticipo incluye todo lo que no se financia (efectivo, seña y trade-in).
     */
    @Transactional(readOnly = true)
    public SimulacionResponse simular(SimulacionRequest request) {
        PlanFinanciamiento plan = buscar(request.planId());
        if (!plan.isActivo()) {
            throw new SolicitudInvalidaException("El plan " + plan.getNombre() + " no está disponible");
        }
        if (!plan.getPlazos().contains(request.cantidadCuotas())) {
            throw new SolicitudInvalidaException("El plan " + plan.getNombre() + " no permite " + request.cantidadCuotas()
                    + " cuotas. Opciones: " + plan.getPlazos().stream().sorted().toList());
        }
        BigDecimal precio = request.precio();
        BigDecimal anticipo = request.anticipo();
        BigDecimal anticipoMinimo = precio.multiply(plan.getPorcentajeAnticipoMinimo())
                .divide(CIEN, 2, RoundingMode.HALF_UP);
        if (anticipo.compareTo(anticipoMinimo) < 0) {
            throw new SolicitudInvalidaException("El anticipo mínimo para el plan " + plan.getNombre() + " es $"
                    + anticipoMinimo.toPlainString() + " (" + plan.getPorcentajeAnticipoMinimo().stripTrailingZeros()
                    .toPlainString() + " % del precio)");
        }
        if (anticipo.compareTo(precio) >= 0) {
            throw new SolicitudInvalidaException("El anticipo cubre todo el precio: no hay nada para financiar");
        }
        BigDecimal financiado = precio.subtract(anticipo);
        List<CuotaCalculada> cuotas = CalculadoraCuotas.cronograma(financiado, plan.getTasaAnual(), request.cantidadCuotas());
        BigDecimal total = cuotas.stream().map(CuotaCalculada::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SimulacionResponse(
                plan.getId(),
                plan.getNombre(),
                plan.getTasaAnual(),
                precio,
                anticipo,
                anticipoMinimo,
                financiado,
                request.cantidadCuotas(),
                cuotas.getFirst().monto(),
                total.subtract(financiado),
                total,
                cuotas);
    }

    /** Para generar el cronograma (RF-30): no importa si el plan se desactivó después de la venta. */
    @Transactional(readOnly = true)
    public PlanFinanciamiento buscar(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Plan " + id + " no encontrado"));
    }

    private static void aplicar(PlanFinanciamiento plan, PlanRequest r, String nombre) {
        plan.setNombre(nombre);
        plan.setDescripcion(r.descripcion());
        plan.setTasaAnual(r.tasaAnual());
        plan.setPlazos(new HashSet<>(r.plazos()));
        plan.setPorcentajeAnticipoMinimo(r.porcentajeAnticipoMinimo());
        plan.setPorcentajeMoraDiaria(r.porcentajeMoraDiaria());
    }
}
