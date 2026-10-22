package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Cocinero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICocineroRepository extends JpaRepository<Cocinero, Long> {

    Optional<Cocinero> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Cocinero> findByCategoria_IdCategoria(Long idCategoria);

    List<Cocinero> findByActivoTrue();
}
