package com.concesionaria.reporte.evento.repository;

import com.concesionaria.reporte.evento.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, String> {
}
