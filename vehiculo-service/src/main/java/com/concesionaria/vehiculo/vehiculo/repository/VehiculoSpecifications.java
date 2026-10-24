package com.concesionaria.vehiculo.vehiculo.repository;

import com.concesionaria.vehiculo.vehiculo.Vehiculo;
import com.concesionaria.vehiculo.vehiculo.dto.FiltroVehiculos;
import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** Arma el WHERE del catálogo según los filtros que vengan (RF-12). */
public final class VehiculoSpecifications {

    private VehiculoSpecifications() {
    }

    /**
     * @param esEmpleado si es false (público) se fuerza: activo = true y estado = DISPONIBLE
     */
    public static Specification<Vehiculo> conFiltros(FiltroVehiculos f, boolean esEmpleado) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            Path<Object> modelo = root.get("modelo");

            if (f.marcaId() != null) {
                condiciones.add(cb.equal(modelo.get("marca").get("id"), f.marcaId()));
            }
            if (f.modeloId() != null) {
                condiciones.add(cb.equal(modelo.get("id"), f.modeloId()));
            }
            if (f.categoriaId() != null) {
                condiciones.add(cb.equal(modelo.get("categoria").get("id"), f.categoriaId()));
            }
            if (f.condicion() != null) {
                condiciones.add(cb.equal(root.get("condicion"), f.condicion()));
            }
            if (f.precioMin() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("precio"), f.precioMin()));
            }
            if (f.precioMax() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("precio"), f.precioMax()));
            }
            if (f.anioMin() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("anio"), f.anioMin()));
            }
            if (f.anioMax() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("anio"), f.anioMax()));
            }
            if (f.kmMax() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("kilometraje"), f.kmMax()));
            }
            if (StringUtils.hasText(f.texto())) {
                String patron = "%" + f.texto().trim().toLowerCase() + "%";
                condiciones.add(cb.or(
                        cb.like(cb.lower(modelo.get("nombre")), patron),
                        cb.like(cb.lower(modelo.get("marca").get("nombre")), patron),
                        cb.like(cb.lower(root.get("vin")), patron)));
            }

            if (esEmpleado) {
                if (f.estado() != null) {
                    condiciones.add(cb.equal(root.get("estado"), f.estado()));
                }
                if (!Boolean.TRUE.equals(f.incluirInactivos())) {
                    condiciones.add(cb.isTrue(root.get("activo")));
                }
            } else {
                condiciones.add(cb.isTrue(root.get("activo")));
                condiciones.add(cb.equal(root.get("estado"), EstadoVehiculo.DISPONIBLE));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }
}
