package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Festival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFestivalRepository extends JpaRepository<Festival, Long> {

    @Override
    @EntityGraph(attributePaths = {"costos", "unidades", "unidades.responsable"})
    List<Festival> findAll();

    @EntityGraph(attributePaths = {"costos", "unidades", "unidades.responsable"})
    List<Festival> findByActivoTrue();

    @EntityGraph(attributePaths = {"costos", "unidades", "unidades.responsable"})
    List<Festival> findByActivoTrueOrderByFechaInicioDesc();

    @Override
    @EntityGraph(attributePaths = {"costos", "unidades", "unidades.responsable"})
    Optional<Festival> findById(Long id);
}
