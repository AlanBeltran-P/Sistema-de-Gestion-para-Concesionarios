package com.concesionaria.reporte.evento.service;

import com.concesionaria.reporte.evento.EventoProcesado;
import com.concesionaria.reporte.evento.repository.EventoProcesadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Evita aplicar dos veces el mismo evento (RabbitMQ puede entregarlo repetido).
 * Se llama dentro de la transacción que aplica el evento: si algo falla, tampoco queda marcado.
 */
@Component
@RequiredArgsConstructor
public class RegistroEventos {

    private final EventoProcesadoRepository repository;

    /** true si es la primera vez que llega; false si ya se había aplicado. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean marcarNuevo(String eventoId, String tipo) {
        if (repository.existsById(eventoId)) {
            return false;
        }
        repository.save(EventoProcesado.builder().eventoId(eventoId).tipo(tipo).build());
        return true;
    }
}
