package com.concesionaria.vehiculo.imagen.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.vehiculo.config.ImagenesProperties;
import com.concesionaria.vehiculo.imagen.ImagenVehiculo;
import com.concesionaria.vehiculo.imagen.dto.ImagenResponse;
import com.concesionaria.vehiculo.imagen.enums.TipoImagen;
import com.concesionaria.vehiculo.imagen.repository.ImagenVehiculoRepository;
import com.concesionaria.vehiculo.vehiculo.Vehiculo;
import com.concesionaria.vehiculo.vehiculo.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;

/** RF-10: subir, eliminar y elegir la imagen principal de un vehículo. */
@Service
@RequiredArgsConstructor
public class ImagenService {

    private final VehiculoService vehiculoService;
    private final ImagenVehiculoRepository imagenRepository;
    private final AlmacenamientoImagenes almacenamiento;
    private final ImagenesProperties props;

    public record ArchivoImagen(Resource contenido, MediaType tipo) {
    }

    /** La primera imagen del vehículo queda como principal aunque no se pida. */
    @Transactional
    public ImagenResponse subir(Long vehiculoId, MultipartFile archivo, boolean principal) {
        Vehiculo vehiculo = vehiculoService.buscarEntidad(vehiculoId);
        if (vehiculo.getImagenes().size() >= props.maxPorVehiculo()) {
            throw new ConflictoException("El vehículo ya tiene el máximo de " + props.maxPorVehiculo() + " imágenes");
        }
        byte[] contenido = leer(archivo);
        TipoImagen tipo = TipoImagen.detectar(contenido)
                .orElseThrow(() -> new SolicitudInvalidaException("Formato no soportado. Usá JPG, PNG o WEBP"));

        String clave = almacenamiento.guardar(vehiculoId, contenido, tipo.extension());
        // Si la transacción no se confirma, el archivo no debe quedar huérfano en disco
        alTerminar(clave, false);

        boolean esPrincipal = principal || vehiculo.getImagenes().isEmpty();
        if (esPrincipal) {
            vehiculo.getImagenes().forEach(i -> i.setPrincipal(false));
        }
        int orden = vehiculo.getImagenes().stream()
                .map(ImagenVehiculo::getOrden)
                .max(Comparator.naturalOrder())
                .orElse(-1) + 1;

        ImagenVehiculo imagen = ImagenVehiculo.builder()
                .url(clave)
                .nombreArchivo(nombreOriginal(archivo))
                .orden(orden)
                .principal(esPrincipal)
                .build();
        vehiculo.agregarImagen(imagen);
        // save inserta en el momento y asigna el id que necesita la respuesta
        imagenRepository.save(imagen);
        return ImagenResponse.desde(vehiculoId, imagen);
    }

    /** Si se borra la principal, pasa a serlo la siguiente según el orden. */
    @Transactional
    public void eliminar(Long vehiculoId, Long imagenId) {
        Vehiculo vehiculo = vehiculoService.buscarEntidad(vehiculoId);
        ImagenVehiculo imagen = buscarImagen(vehiculo, imagenId);
        vehiculo.quitarImagen(imagen);
        if (imagen.isPrincipal()) {
            vehiculo.getImagenes().stream()
                    .min(Comparator.comparing(ImagenVehiculo::getOrden))
                    .ifPresent(i -> i.setPrincipal(true));
        }
        // El archivo se borra recién cuando la base confirma el cambio
        alTerminar(imagen.getUrl(), true);
    }

    @Transactional
    public ImagenResponse marcarPrincipal(Long vehiculoId, Long imagenId) {
        Vehiculo vehiculo = vehiculoService.buscarEntidad(vehiculoId);
        ImagenVehiculo elegida = buscarImagen(vehiculo, imagenId);
        vehiculo.getImagenes().forEach(i -> i.setPrincipal(i == elegida));
        return ImagenResponse.desde(vehiculoId, elegida);
    }

    @Transactional(readOnly = true)
    public ArchivoImagen obtenerArchivo(Long vehiculoId, Long imagenId) {
        ImagenVehiculo imagen = buscarImagen(vehiculoService.buscarEntidad(vehiculoId), imagenId);
        MediaType tipo = TipoImagen.porExtension(imagen.getUrl())
                .map(TipoImagen::mediaType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return new ArchivoImagen(almacenamiento.cargar(imagen.getUrl()), tipo);
    }

    private ImagenVehiculo buscarImagen(Vehiculo vehiculo, Long imagenId) {
        return vehiculo.getImagenes().stream()
                .filter(i -> i.getId().equals(imagenId))
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException(
                        "Imagen " + imagenId + " no encontrada en el vehículo " + vehiculo.getId()));
    }

    /**
     * @param siConfirma true: borrar el archivo si la transacción se confirma (eliminar imagen).
     *                   false: borrarlo si la transacción falla (archivo recién subido).
     */
    private void alTerminar(String clave, boolean siConfirma) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                boolean confirmada = status == STATUS_COMMITTED;
                if (confirmada == siConfirma) {
                    almacenamiento.eliminar(clave);
                }
            }
        });
    }

    private static byte[] leer(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("No se envió ningún archivo");
        }
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo", e);
        }
    }

    private static String nombreOriginal(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }
}
