package com.concesionaria.venta.reserva.service;

import com.concesionaria.venta.cotizacion.service.CotizacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** RF-22: cada pocos minutos libera las reservas vencidas y vence las cotizaciones viejas. */
@Slf4j
@Component
@RequiredArgsConstructor
public class VencimientosJob {

    public record Resultado(LocalDateTime fecha, int reservasVencidas, int cotizacionesVencidas) {
    }

    private final ReservaService reservaService;
    private final CotizacionService cotizacionService;

    @Scheduled(cron = "${ventas.cron-vencimientos:0 */5 * * * *}")
    public void programado() {
        Resultado r = procesar(LocalDateTime.now());
        if (r.reservasVencidas() > 0 || r.cotizacionesVencidas() > 0) {
            log.info("Vencimientos: {}", r);
        }
    }

    public Resultado procesar(LocalDateTime ahora) {
        int reservas = reservaService.vencer(ahora);
        int cotizaciones = cotizacionService.vencer(ahora.toLocalDate());
        return new Resultado(ahora, reservas, cotizaciones);
    }
}
