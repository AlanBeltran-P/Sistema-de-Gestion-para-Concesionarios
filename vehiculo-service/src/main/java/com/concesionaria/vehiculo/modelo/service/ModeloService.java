package com.concesionaria.vehiculo.modelo.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.vehiculo.categoria.Categoria;
import com.concesionaria.vehiculo.categoria.service.CategoriaService;
import com.concesionaria.vehiculo.marca.Marca;
import com.concesionaria.vehiculo.marca.service.MarcaService;
import com.concesionaria.vehiculo.modelo.Modelo;
import com.concesionaria.vehiculo.modelo.dto.ModeloRequest;
import com.concesionaria.vehiculo.modelo.dto.ModeloResponse;
import com.concesionaria.vehiculo.modelo.repository.ModeloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-07 */
@Service
@RequiredArgsConstructor
public class ModeloService {

    private final ModeloRepository modeloRepository;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;

    @Transactional(readOnly = true)
    public List<ModeloResponse> buscar(Long marcaId, Long categoriaId, boolean incluirInactivos) {
        return modeloRepository.buscar(marcaId, categoriaId, incluirInactivos).stream()
                .map(ModeloResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ModeloResponse obtener(Long id) {
        return ModeloResponse.desde(buscarPorId(id));
    }

    @Transactional
    public ModeloResponse crear(ModeloRequest request) {
        Marca marca = marcaService.buscarActiva(request.marcaId());
        Categoria categoria = categoriaService.buscarActiva(request.categoriaId());
        String nombre = request.nombre().trim();
        if (modeloRepository.existsByMarcaIdAndNombreIgnoreCase(marca.getId(), nombre)) {
            throw new ConflictoException("La marca " + marca.getNombre() + " ya tiene el modelo " + nombre);
        }
        Modelo modelo = modeloRepository.save(Modelo.builder()
                .nombre(nombre)
                .marca(marca)
                .categoria(categoria)
                .build());
        return ModeloResponse.desde(modelo);
    }

    @Transactional
    public ModeloResponse actualizar(Long id, ModeloRequest request) {
        Modelo modelo = buscarPorId(id);
        Marca marca = marcaService.buscarActiva(request.marcaId());
        Categoria categoria = categoriaService.buscarActiva(request.categoriaId());
        String nombre = request.nombre().trim();
        if (modeloRepository.existsByMarcaIdAndNombreIgnoreCaseAndIdNot(marca.getId(), nombre, id)) {
            throw new ConflictoException("La marca " + marca.getNombre() + " ya tiene el modelo " + nombre);
        }
        modelo.setNombre(nombre);
        modelo.setMarca(marca);
        modelo.setCategoria(categoria);
        return ModeloResponse.desde(modelo);
    }

    @Transactional
    public ModeloResponse cambiarActivo(Long id, boolean activo) {
        Modelo modelo = buscarPorId(id);
        modelo.setActivo(activo);
        return ModeloResponse.desde(modelo);
    }

    /** Para registrar o editar vehículos: el modelo y su marca tienen que estar activos. */
    @Transactional(readOnly = true)
    public Modelo buscarActivo(Long id) {
        Modelo modelo = buscarPorId(id);
        if (!modelo.isActivo() || !modelo.getMarca().isActivo()) {
            throw new SolicitudInvalidaException("El modelo " + modelo.getNombre() + " o su marca están inactivos");
        }
        return modelo;
    }

    private Modelo buscarPorId(Long id) {
        return modeloRepository.findWithMarcaAndCategoriaById(id)
                .orElseThrow(() -> new NoEncontradoException("Modelo " + id + " no encontrado"));
    }
}
