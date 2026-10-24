package com.concesionaria.vehiculo.imagen.enums;

import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Optional;

/**
 * Formatos aceptados. El tipo se detecta por los primeros bytes del archivo,
 * no por la extensión ni el Content-Type, que los puede cambiar cualquiera.
 */
public enum TipoImagen {
    JPEG("jpg", MediaType.IMAGE_JPEG),
    PNG("png", MediaType.IMAGE_PNG),
    WEBP("webp", MediaType.parseMediaType("image/webp"));

    private final String extension;
    private final MediaType mediaType;

    TipoImagen(String extension, MediaType mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String extension() {
        return extension;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public static Optional<TipoImagen> detectar(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return Optional.of(JPEG);
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return Optional.of(PNG);
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    public static Optional<TipoImagen> porExtension(String nombreArchivo) {
        String ext = nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
        return Arrays.stream(values()).filter(t -> t.extension.equals(ext)).findFirst();
    }
}
