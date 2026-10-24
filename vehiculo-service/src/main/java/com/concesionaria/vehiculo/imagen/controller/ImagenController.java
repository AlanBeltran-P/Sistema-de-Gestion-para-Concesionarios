package com.concesionaria.vehiculo.imagen.controller;

import com.concesionaria.vehiculo.imagen.dto.ImagenResponse;
import com.concesionaria.vehiculo.imagen.service.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Duration;

/** RF-10. Ver la imagen es público; subir, borrar y elegir principal: ADMIN o VENDEDOR. */
@Tag(name = "Imágenes de vehículos", description = "Subir, ver y eliminar fotos (RF-10)")
@RestController
@RequestMapping("/api/vehiculos/{vehiculoId}/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    private final ImagenService imagenService;

    /** multipart/form-data con el campo "archivo" (JPG, PNG o WEBP, máx. 5 MB). */
    @Operation(summary = "Subir una foto", description = "multipart/form-data con el campo \"archivo\" (JPG, PNG o WEBP, máx. 5 MB).\n\n**Roles:** ADMIN, VENDEDOR.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ImagenResponse> subir(@PathVariable Long vehiculoId,
                                                @RequestPart("archivo") MultipartFile archivo,
                                                @RequestParam(defaultValue = "false") boolean principal) {
        ImagenResponse imagen = imagenService.subir(vehiculoId, archivo, principal);
        return ResponseEntity.created(URI.create(imagen.url())).body(imagen);
    }

    @Operation(summary = "Eliminar una foto", description = "**Roles:** ADMIN, VENDEDOR.")
    @DeleteMapping("/{imagenId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long vehiculoId, @PathVariable Long imagenId) {
        imagenService.eliminar(vehiculoId, imagenId);
    }

    @Operation(summary = "Elegir la foto principal", description = "**Roles:** ADMIN, VENDEDOR.")
    @PatchMapping("/{imagenId}/principal")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ImagenResponse marcarPrincipal(@PathVariable Long vehiculoId, @PathVariable Long imagenId) {
        return imagenService.marcarPrincipal(vehiculoId, imagenId);
    }

    /** Devuelve el archivo. El navegador lo puede guardar en caché un día (el archivo nunca cambia). */
    @Operation(summary = "Descargar una foto", description = "Devuelve el archivo. El navegador lo puede guardar en caché un día (el archivo nunca cambia).\n\n**Público** (no requiere login).")
    @SecurityRequirements
    @GetMapping("/{imagenId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Long vehiculoId, @PathVariable Long imagenId) {
        ImagenService.ArchivoImagen archivo = imagenService.obtenerArchivo(vehiculoId, imagenId);
        return ResponseEntity.ok()
                .contentType(archivo.tipo())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .body(archivo.contenido());
    }
}
