package com.concesionaria.vehiculo.marca.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.vehiculo.marca.Marca;
import com.concesionaria.vehiculo.marca.dto.MarcaRequest;
import com.concesionaria.vehiculo.marca.dto.MarcaResponse;
import com.concesionaria.vehiculo.marca.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-07 */
@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    @Transactional(readOnly = true)
    public List<MarcaResponse> listar(boolean incluirInactivas) {
        List<Marca> marcas = incluirInactivas
                ? marcaRepository.findAllByOrderByNombreAsc()
                : marcaRepository.findAllByActivoTrueOrderByNombreAsc();
        return marcas.stream().map(MarcaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public MarcaResponse obtener(Long id) {
        return MarcaResponse.desde(buscar(id));
    }

    @Transactional
    public MarcaResponse crear(MarcaRequest request) {
        String nombre = request.nombre().trim();
        if (marcaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe la marca " + nombre);
        }
        return MarcaResponse.desde(marcaRepository.save(Marca.builder().nombre(nombre).build()));
    }

    @Transactional
    public MarcaResponse actualizar(Long id, MarcaRequest request) {
        Marca marca = buscar(id);
        String nombre = request.nombre().trim();
        if (marcaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe la marca " + nombre);
        }
        marca.setNombre(nombre);
        return MarcaResponse.desde(marca);
    }

    /** Una marca inactiva no se puede usar en modelos nuevos; los existentes no cambian. */
    @Transactional
    public MarcaResponse cambiarActivo(Long id, boolean activo) {
        Marca marca = buscar(id);
        marca.setActivo(activo);
        return MarcaResponse.desde(marca);
    }

    /** Para crear o editar modelos: la marca tiene que existir y estar activa. */
    @Transactional(readOnly = true)
    public Marca buscarActiva(Long id) {
        Marca marca = buscar(id);
        if (!marca.isActivo()) {
            throw new SolicitudInvalidaException("La marca " + marca.getNombre() + " está inactiva");
        }
        return marca;
    }

    private Marca buscar(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Marca " + id + " no encontrada"));
    }
}
