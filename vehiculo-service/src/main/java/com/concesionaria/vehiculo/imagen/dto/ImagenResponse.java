package com.concesionaria.vehiculo.imagen.dto;

import com.concesionaria.vehiculo.imagen.ImagenVehiculo;

/**
 * @param url ruta pública para mostrar la imagen (pasando por el gateway)
 */
public record ImagenResponse(Long id, String url, String nombreArchivo, Integer orden, boolean principal) {

    public static ImagenResponse desde(Long vehiculoId, ImagenVehiculo imagen) {
        return new ImagenResponse(imagen.getId(), urlPublica(vehiculoId, imagen.getId()),
                imagen.getNombreArchivo(), imagen.getOrden(), imagen.isPrincipal());
    }

    public static String urlPublica(Long vehiculoId, Long imagenId) {
        return "/api/vehiculos/" + vehiculoId + "/imagenes/" + imagenId + "/archivo";
    }
}
