package com.concesionaria.common.transaccion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Para deshacer una llamada a otro servicio si la transacción local termina en rollback.
 * Ej: venta-service marcó el vehículo como VENDIDO y después falló al guardar la venta:
 * <pre>
 * vehiculoClient.cambiarEstado(id, VENDIDO);
 * AlHacerRollback.ejecutar("liberar vehículo " + id, () -> vehiculoClient.cambiarEstado(id, DISPONIBLE));
 * </pre>
 * Si la compensación también falla, se loguea para corregirlo a mano.
 */
@Slf4j
public final class AlHacerRollback {

    private AlHacerRollback() {
    }

    public static void ejecutar(String descripcion, Runnable compensacion) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("AlHacerRollback necesita una transacción activa");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado == STATUS_COMMITTED) {
                    return;
                }
                try {
                    compensacion.run();
                    log.info("Compensación aplicada: {}", descripcion);
                } catch (RuntimeException e) {
                    log.error("No se pudo compensar ({}); revisar a mano", descripcion, e);
                }
            }
        });
    }
}
