package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.CategoriaCocinero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICategoriaCocineroRepository extends JpaRepository<CategoriaCocinero, Long> {

    Optional<CategoriaCocinero> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}

