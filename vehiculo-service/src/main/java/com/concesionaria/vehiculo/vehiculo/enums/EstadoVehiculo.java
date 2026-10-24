package com.concesionaria.vehiculo.vehiculo.enums;

import java.util.Set;

/**
 * Ciclo de vida (RF-11):
 * DISPONIBLE -> RESERVADO -> VENDIDO, o DISPONIBLE -> VENDIDO (venta directa).
 * RESERVADO -> DISPONIBLE: la reserva vence o se anula.
 * VENDIDO -> DISPONIBLE: se anula la venta (solo ADMIN, RF-26).
 */
public enum EstadoVehiculo {
    DISPONIBLE,
    RESERVADO,
    VENDIDO;

    public boolean puedePasarA(EstadoVehiculo destino) {
        return switch (this) {
            case DISPONIBLE -> Set.of(RESERVADO, VENDIDO).contains(destino);
            case RESERVADO -> Set.of(VENDIDO, DISPONIBLE).contains(destino);
            case VENDIDO -> destino == DISPONIBLE;
        };
    }
}
