package com.concesionaria.venta.cliente.dto;

/** Lo que se usa de la respuesta de cliente-service. */
public record ClienteRemoto(
        Long id,
        Long usuarioId,
        String nombre,
        String apellido,
        String tipoDocumento,
        String numeroDocumento,
        String email,
        String telefono,
        Direccion direccion
) {

    public record Direccion(String calle, String numero, String pisoDepto,
                            String ciudad, String provincia, String codigoPostal) {

        /** Ej: "Av. Rivadavia 1234 3B, CABA, Buenos Aires (CP 1406)". */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(calle + " " + numero);
            if (pisoDepto != null && !pisoDepto.isBlank()) {
                sb.append(' ').append(pisoDepto.trim());
            }
            sb.append(", ").append(ciudad).append(", ").append(provincia);
            if (codigoPostal != null && !codigoPostal.isBlank()) {
                sb.append(" (CP ").append(codigoPostal.trim()).append(')');
            }
            return sb.toString();
        }
    }

    public String nombreCompleto() {
        return nombre + " " + apellido;
    }
}
