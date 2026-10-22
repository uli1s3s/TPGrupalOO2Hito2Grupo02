package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.CategoriaCocineroDTO;

import java.util.List;
import java.util.Optional;

public interface ICategoriaCocineroService {

    List<CategoriaCocineroDTO> getAll();

    Optional<CategoriaCocineroDTO> findById(Long id);

    Optional<CategoriaCocineroDTO> findByNombre(String nombre);

    CategoriaCocineroDTO insertOrUpdate(CategoriaCocineroDTO categoriaCocineroDTO);

    boolean remove(Long id);
}

