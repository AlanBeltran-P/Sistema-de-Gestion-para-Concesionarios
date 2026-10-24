package com.concesionaria.vehiculo.imagen.service;

import org.springframework.core.io.Resource;

/**
 * Dónde se guardan los archivos. Hoy en disco (AlmacenamientoLocal); para usar S3 u otro
 * servicio alcanza con otra implementación de esta interfaz.
 * La "clave" identifica el archivo (ej: "12/3f2a....jpg") y es lo que se guarda en la base.
 */
public interface AlmacenamientoImagenes {

    String guardar(Long vehiculoId, byte[] contenido, String extension);

    Resource cargar(String clave);

    void eliminar(String clave);
}
