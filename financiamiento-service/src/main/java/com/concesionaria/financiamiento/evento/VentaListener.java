package com.concesionaria.financiamiento.evento;

import com.concesionaria.financiamiento.config.RabbitConfig;
import com.concesionaria.financiamiento.financiamiento.service.FinanciamientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RF-30: el cronograma se genera cuando venta-service confirma una venta financiada.
 * Si la venta se anula (RF-26), se anula el financiamiento y sus cuotas impagas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VentaListener {

    private final FinanciamientoService financiamientoService;

    @RabbitListener(queues = RabbitConfig.COLA_VENTAS)
    public void alCambiarVenta(VentaEvento evento) {
        if (!evento.financiada()) {
            return;
        }
        switch (evento.tipo()) {
            case "confirmada" -> {
                log.info("Venta financiada {} confirmada ({})", evento.ventaId(), evento.eventoId());
                financiamientoService.crearDesdeVenta(evento);
            }
            case "anulada" -> {
                log.info("Venta financiada {} anulada ({})", evento.ventaId(), evento.eventoId());
                financiamientoService.anularPorVenta(evento.ventaId());
            }
            default -> log.debug("Evento de venta {} ignorado", evento.tipo());
        }
    }
}
