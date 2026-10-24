package com.concesionaria.vehiculo.categoria.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.vehiculo.categoria.Categoria;
import com.concesionaria.vehiculo.categoria.dto.CategoriaRequest;
import com.concesionaria.vehiculo.categoria.dto.CategoriaResponse;
import com.concesionaria.vehiculo.categoria.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-07 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(boolean incluirInactivas) {
        List<Categoria> categorias = incluirInactivas
                ? categoriaRepository.findAllByOrderByNombreAsc()
                : categoriaRepository.findAllByActivoTrueOrderByNombreAsc();
        return categorias.stream().map(CategoriaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.desde(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe la categoría " + nombre);
        }
        return CategoriaResponse.desde(categoriaRepository.save(Categoria.builder()
                .nombre(nombre)
                .descripcion(request.descripcion())
                .build()));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe la categoría " + nombre);
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(request.descripcion());
        return CategoriaResponse.desde(categoria);
    }

    @Transactional
    public CategoriaResponse cambiarActivo(Long id, boolean activo) {
        Categoria categoria = buscar(id);
        categoria.setActivo(activo);
        return CategoriaResponse.desde(categoria);
    }

    /** Para crear o editar modelos: la categoría tiene que existir y estar activa. */
    @Transactional(readOnly = true)
    public Categoria buscarActiva(Long id) {
        Categoria categoria = buscar(id);
        if (!categoria.isActivo()) {
            throw new SolicitudInvalidaException("La categoría " + categoria.getNombre() + " está inactiva");
        }
        return categoria;
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Categoría " + id + " no encontrada"));
    }
}
