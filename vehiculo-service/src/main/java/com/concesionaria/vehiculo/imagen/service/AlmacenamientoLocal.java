package com.concesionaria.vehiculo.imagen.service;

import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.vehiculo.config.ImagenesProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Guarda las imágenes en disco: {directorio}/{vehiculoId}/{uuid}.{ext} */
@Slf4j
@Component
public class AlmacenamientoLocal implements AlmacenamientoImagenes {

    private final Path raiz;

    public AlmacenamientoLocal(ImagenesProperties props) {
        this.raiz = Path.of(props.directorio()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de imágenes " + raiz, e);
        }
        log.info("Imágenes de vehículos en {}", raiz);
    }

    @Override
    public String guardar(Long vehiculoId, byte[] contenido, String extension) {
        String clave = vehiculoId + "/" + UUID.randomUUID() + "." + extension;
        Path destino = resolver(clave);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
            return clave;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
    }

    @Override
    public Resource cargar(String clave) {
        Path archivo = resolver(clave);
        if (!Files.isReadable(archivo)) {
            throw new NoEncontradoException("Archivo de imagen no encontrado");
        }
        return new FileSystemResource(archivo);
    }

    @Override
    public void eliminar(String clave) {
        try {
            Files.deleteIfExists(resolver(clave));
        } catch (IOException e) {
            log.warn("No se pudo borrar la imagen {}", clave, e);
        }
    }

    /** Evita que una clave como "../../algo" salga de la carpeta de imágenes. */
    private Path resolver(String clave) {
        Path ruta = raiz.resolve(clave).normalize();
        if (!ruta.startsWith(raiz)) {
            throw new IllegalArgumentException("Clave de imagen inválida: " + clave);
        }
        return ruta;
    }
}
