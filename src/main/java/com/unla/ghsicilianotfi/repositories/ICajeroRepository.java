package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Cajero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICajeroRepository extends JpaRepository<Cajero, Long> {

    Optional<Cajero> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Cajero> findByTurnoIgnoreCase(String turno);

    List<Cajero> findByActivoTrue();
}

