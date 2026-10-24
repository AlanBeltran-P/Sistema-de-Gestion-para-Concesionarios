package com.concesionaria.financiamiento.plan.dto;

import com.concesionaria.financiamiento.plan.PlanFinanciamiento;

import java.math.BigDecimal;
import java.util.List;

public record PlanResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal tasaAnual,
        List<Integer> plazos,
        BigDecimal porcentajeAnticipoMinimo,
        BigDecimal porcentajeMoraDiaria,
        boolean activo
) {

    public static PlanResponse desde(PlanFinanciamiento p) {
        return new PlanResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getTasaAnual(),
                p.getPlazos().stream().sorted().toList(),
                p.getPorcentajeAnticipoMinimo(),
                p.getPorcentajeMoraDiaria(),
                p.isActivo());
    }
}
