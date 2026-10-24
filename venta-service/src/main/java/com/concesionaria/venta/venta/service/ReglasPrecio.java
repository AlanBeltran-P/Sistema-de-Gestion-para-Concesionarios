package com.concesionaria.venta.venta.service;

import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.config.VentasProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Reglas de precio compartidas por cotizaciones, reservas y ventas. */
@Component
@RequiredArgsConstructor
public class ReglasPrecio {

    public static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final VentasProperties propiedades;

    /** Un VENDEDOR puede descontar hasta el % configurado; un ADMIN no tiene límite. */
    public void validarDescuento(BigDecimal descuento, BigDecimal precio, UsuarioActual usuario) {
        if (descuento.signum() < 0) {
            throw new SolicitudInvalidaException("El descuento no puede ser negativo");
        }
        if (descuento.compareTo(precio) >= 0) {
            throw new SolicitudInvalidaException("El descuento no puede ser igual o mayor al precio");
        }
        if (usuario.esAdmin()) {
            return;
        }
        BigDecimal maximo = porcentaje(precio, propiedades.descuentoMaximoVendedor());
        if (descuento.compareTo(maximo) > 0) {
            throw new SolicitudInvalidaException("Un vendedor puede descontar hasta el "
                    + propiedades.descuentoMaximoVendedor().stripTrailingZeros().toPlainString() + " % ($"
                    + maximo.toPlainString() + "). Para más, tiene que autorizarlo un ADMIN");
        }
    }

    public static BigDecimal porcentaje(BigDecimal monto, BigDecimal porcentaje) {
        return monto.multiply(porcentaje).divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
