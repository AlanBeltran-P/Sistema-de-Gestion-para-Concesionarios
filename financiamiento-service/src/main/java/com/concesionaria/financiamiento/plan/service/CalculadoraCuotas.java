package com.concesionaria.financiamiento.plan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema francés (cuota fija): cuota = P · i · (1+i)^n / ((1+i)^n − 1), con i = tasa anual / 12.
 * Cada cuota se separa en interés (sobre el saldo) y capital. La última ajusta los centavos
 * de redondeo para que el capital sume exactamente el monto financiado.
 */
public final class CalculadoraCuotas {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal MIL_DOSCIENTOS = BigDecimal.valueOf(1200);

    public record CuotaCalculada(int numero, BigDecimal capital, BigDecimal interes, BigDecimal monto,
                                 BigDecimal saldoRestante) {
    }

    private CalculadoraCuotas() {
    }

    public static BigDecimal valorCuota(BigDecimal monto, BigDecimal tasaAnual, int cuotas) {
        BigDecimal i = tasaAnual.divide(MIL_DOSCIENTOS, MC);
        if (i.signum() == 0) {
            return monto.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);
        }
        BigDecimal factor = BigDecimal.ONE.add(i).pow(cuotas, MC);
        return monto.multiply(i, MC).multiply(factor, MC)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    public static List<CuotaCalculada> cronograma(BigDecimal monto, BigDecimal tasaAnual, int cuotas) {
        BigDecimal i = tasaAnual.divide(MIL_DOSCIENTOS, MC);
        BigDecimal cuota = valorCuota(monto, tasaAnual, cuotas);
        BigDecimal saldo = monto;
        List<CuotaCalculada> resultado = new ArrayList<>(cuotas);
        for (int n = 1; n <= cuotas; n++) {
            BigDecimal interes = saldo.multiply(i, MC).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capital = n == cuotas ? saldo : cuota.subtract(interes).min(saldo);
            saldo = saldo.subtract(capital);
            resultado.add(new CuotaCalculada(n, capital, interes, capital.add(interes), saldo));
        }
        return resultado;
    }
}
