package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("rolRepository")
public interface IRolRepository extends JpaRepository<Rol, Long> {

    // Busca un rol por su nombre (ADMINISTRADOR, RESPONSABLE, EMPLEADO)
    Optional<Rol> findByNombre(String nombre);
}