package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPlatoRepository extends JpaRepository<Plato, Long> {

    List<Plato> findByUnidadVenta_Responsable_NombreOrderByNombreAsc(String nombreResponsable);
}