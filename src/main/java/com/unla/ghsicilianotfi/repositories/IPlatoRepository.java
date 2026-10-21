package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPlatoRepository extends JpaRepository<Plato, Long> {

}