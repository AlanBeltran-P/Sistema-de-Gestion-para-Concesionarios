package com.concesionaria.financiamiento.cuota.service;

import com.concesionaria.financiamiento.config.FinanciamientoProperties;
import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.evento.CuotaEvento;
import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import com.concesionaria.financiamiento.financiamiento.enums.EstadoFinanciamiento;
import com.concesionaria.financiamiento.cuota.repository.CuotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Job diario (RF-33 y RF-37):
 * 1. Las cuotas impagas cuya fecha pasó quedan VENCIDAS y se avisa al cliente.
 * 2. La mora se recalcula: monto de la cuota × % diario del plan × días de atraso.
 * 3. Se recuerdan las cuotas que vencen en los próximos días (una sola vez por cuota).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VencimientoCuotasService {

    private static final List<EstadoCuota> IMPAGAS =
            List.of(EstadoCuota.PENDIENTE, EstadoCuota.PAGADA_PARCIAL, EstadoCuota.VENCIDA);
    private static final List<EstadoCuota> SIN_VENCER = List.of(EstadoCuota.PENDIENTE, EstadoCuota.PAGADA_PARCIAL);
    private static final List<EstadoFinanciamiento> VIGENTES =
            List.of(EstadoFinanciamiento.ACTIVO, EstadoFinanciamiento.EN_MORA);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public record Resultado(LocalDate fecha, int cuotasVencidas, int morasActualizadas, int recordatorios) {
    }

    private final CuotaRepository cuotaRepository;
    private final FinanciamientoProperties propiedades;
    private final ApplicationEventPublisher eventos;

    @Scheduled(cron = "${financiamiento.cron-vencimientos:0 0 1 * * *}")
    public void procesarDiario() {
        Resultado r = procesar(LocalDate.now());
        log.info("Vencimientos procesados: {}", r);
    }

    /** @param hoy fecha de referencia (el ADMIN puede correrlo a mano con otra fecha para probar). */
    @Transactional
    public Resultado procesar(LocalDate hoy) {
        int vencidas = 0;
        int moras = 0;
        Set<Financiamiento> afectados = new HashSet<>();

        for (Cuota cuota : cuotaRepository.findVencidasAl(IMPAGAS, hoy, VIGENTES)) {
            boolean recienVencida = cuota.getEstado() != EstadoCuota.VENCIDA;
            if (recienVencida) {
                cuota.setEstado(EstadoCuota.VENCIDA);
                vencidas++;
            }
            BigDecimal mora = calcularMora(cuota, hoy);
            boolean cambioMora = mora.compareTo(cuota.getMora()) > 0;
            if (cambioMora) {
                cuota.setMora(mora);
                moras++;
            }
            if (recienVencida) {
                eventos.publishEvent(CuotaEvento.vencida(cuota));
            } else if (cambioMora) {
                eventos.publishEvent(CuotaEvento.actualizada(cuota));
            }
            afectados.add(cuota.getFinanciamiento());
        }
        afectados.forEach(Financiamiento::recalcularEstado);

        int recordatorios = 0;
        LocalDate hasta = hoy.plusDays(propiedades.diasAvisoVencimiento());
        for (Cuota cuota : cuotaRepository.findPorVencer(SIN_VENCER, hoy, hasta, VIGENTES)) {
            cuota.setRecordatorioEnviado(true);
            eventos.publishEvent(CuotaEvento.porVencer(cuota));
            recordatorios++;
        }
        return new Resultado(hoy, vencidas, moras, recordatorios);
    }

    private static BigDecimal calcularMora(Cuota cuota, LocalDate hoy) {
        long dias = ChronoUnit.DAYS.between(cuota.getFechaVencimiento(), hoy);
        BigDecimal porcentaje = cuota.getFinanciamiento().getPorcentajeMoraDiariaAplicado();
        return cuota.getMonto().multiply(porcentaje).multiply(BigDecimal.valueOf(dias))
                .divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
