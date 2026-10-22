package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.CocineroDTO;

import java.util.List;
import java.util.Optional;

public interface ICocineroService {

    List<CocineroDTO> getAll();

    List<CocineroDTO> getAllActivos();

    Optional<CocineroDTO> findById(Long id);

    Optional<CocineroDTO> findByDni(String dni);

    List<CocineroDTO> findByCategoria(Long idCategoria);

    CocineroDTO insertOrUpdate(CocineroDTO cocineroDTO);

    boolean darDeBajaLogica(Long id);

    boolean darDeAltaLogica(Long id);

    boolean remove(Long id);
}

