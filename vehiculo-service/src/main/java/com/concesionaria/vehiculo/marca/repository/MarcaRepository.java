package com.concesionaria.vehiculo.marca.repository;

import com.concesionaria.vehiculo.marca.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Long> {

    List<Marca> findAllByOrderByNombreAsc();

    List<Marca> findAllByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
