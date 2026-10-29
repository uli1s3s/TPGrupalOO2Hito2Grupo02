package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Festival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFestivalRepository extends JpaRepository<Festival, Long> {

    @Override
    @EntityGraph(attributePaths = "costos")
    List<Festival> findAll();

    @EntityGraph(attributePaths = "costos")
    List<Festival> findByActivoTrue();

    @Override
    @EntityGraph(attributePaths = "costos")
    Optional<Festival> findById(Long id);
}
