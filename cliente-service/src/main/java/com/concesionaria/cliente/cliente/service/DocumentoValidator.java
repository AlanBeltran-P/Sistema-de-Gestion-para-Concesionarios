package com.concesionaria.cliente.cliente.service;

import com.concesionaria.cliente.cliente.enums.TipoDocumento;
import com.concesionaria.common.exception.SolicitudInvalidaException;

/**
 * Normaliza y valida documentos argentinos:
 * - DNI: 7 u 8 dígitos (se aceptan puntos: 30.123.456).
 * - CUIT/CUIL: 11 dígitos con dígito verificador válido (se aceptan guiones: 20-30123456-7).
 * - PASAPORTE: 6 a 9 letras o números.
 * Devuelve el número sin puntos ni guiones, que es como se guarda.
 */
public final class DocumentoValidator {

    private static final int[] PESOS_CUIT = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private DocumentoValidator() {
    }

    public static String normalizar(TipoDocumento tipo, String numero) {
        String limpio = numero.replaceAll("[.\\-\\s]", "").toUpperCase();
        return switch (tipo) {
            case DNI -> {
                if (!limpio.matches("\\d{7,8}")) {
                    throw new SolicitudInvalidaException("El DNI debe tener 7 u 8 dígitos");
                }
                yield limpio;
            }
            case CUIT, CUIL -> {
                if (!limpio.matches("\\d{11}") || !digitoVerificadorValido(limpio)) {
                    throw new SolicitudInvalidaException("El " + tipo + " no es válido");
                }
                yield limpio;
            }
            case PASAPORTE -> {
                if (!limpio.matches("[A-Z0-9]{6,9}")) {
                    throw new SolicitudInvalidaException("El pasaporte debe tener entre 6 y 9 letras o números");
                }
                yield limpio;
            }
        };
    }

    private static boolean digitoVerificadorValido(String cuit) {
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(cuit.charAt(i)) * PESOS_CUIT[i];
        }
        int resto = 11 - (suma % 11);
        int esperado = resto == 11 ? 0 : resto;
        return esperado != 10 && esperado == Character.getNumericValue(cuit.charAt(10));
    }
}
