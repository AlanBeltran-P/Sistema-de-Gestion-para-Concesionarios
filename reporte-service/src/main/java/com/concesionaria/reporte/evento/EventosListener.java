package com.concesionaria.reporte.evento;

import com.concesionaria.reporte.config.RabbitConfig;
import com.concesionaria.reporte.cuota.service.CuotaReporteService;
import com.concesionaria.reporte.vehiculo.service.VehiculoReporteService;
import com.concesionaria.reporte.venta.service.VentaReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventosListener {

    private final VehiculoReporteService vehiculoService;
    private final VentaReporteService ventaService;
    private final CuotaReporteService cuotaService;

    @RabbitListener(queues = RabbitConfig.COLA_VEHICULOS)
    public void vehiculo(Eventos.Vehiculo evento) {
        vehiculoService.aplicar(evento);
    }

    @RabbitListener(queues = RabbitConfig.COLA_VENTAS)
    public void venta(Eventos.Venta evento) {
        ventaService.aplicar(evento);
    }

    @RabbitListener(queues = RabbitConfig.COLA_CUOTAS)
    public void cuota(Eventos.Cuota evento) {
        cuotaService.aplicar(evento);
    }
}
