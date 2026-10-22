package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CategoriaCocineroDTO;
import com.unla.ghsicilianotfi.entities.CategoriaCocinero;
import com.unla.ghsicilianotfi.repositories.ICategoriaCocineroRepository;
import com.unla.ghsicilianotfi.services.ICategoriaCocineroService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("categoriaCocineroService")
public class CategoriaCocineroService implements ICategoriaCocineroService {

    private final ICategoriaCocineroRepository categoriaCocineroRepository;
    private final ModelMapper modelMapper;

    public CategoriaCocineroService(ICategoriaCocineroRepository categoriaCocineroRepository,
                                    ModelMapper modelMapper) {
        this.categoriaCocineroRepository = categoriaCocineroRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCocineroDTO> getAll() {
        return categoriaCocineroRepository.findAll()
                .stream()
                .map(cat -> modelMapper.map(cat, CategoriaCocineroDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaCocineroDTO> findById(Long id) {
        return categoriaCocineroRepository.findById(id)
                .map(cat -> modelMapper.map(cat, CategoriaCocineroDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaCocineroDTO> findByNombre(String nombre) {
        return categoriaCocineroRepository.findByNombreIgnoreCase(nombre)
                .map(cat -> modelMapper.map(cat, CategoriaCocineroDTO.class));
    }

    @Override
    @Transactional
    public CategoriaCocineroDTO insertOrUpdate(CategoriaCocineroDTO categoriaCocineroDTO) {
        if (categoriaCocineroDTO == null || categoriaCocineroDTO.getNombre() == null || categoriaCocineroDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }

        // Si es una inserción nueva, verificar unicidad de nombre
        if (categoriaCocineroDTO.getIdCategoria() == null &&
                categoriaCocineroRepository.existsByNombreIgnoreCase(categoriaCocineroDTO.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + categoriaCocineroDTO.getNombre());
        }

        CategoriaCocinero categoria;
        if (categoriaCocineroDTO.getIdCategoria() != null) {
            categoria = categoriaCocineroRepository.findById(categoriaCocineroDTO.getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("No existe categoría con id: " + categoriaCocineroDTO.getIdCategoria()));
            categoria.setNombre(categoriaCocineroDTO.getNombre().trim());
        } else {
            categoria = new CategoriaCocinero(categoriaCocineroDTO.getNombre().trim());
        }

        CategoriaCocinero guardada = categoriaCocineroRepository.save(categoria);
        return modelMapper.map(guardada, CategoriaCocineroDTO.class);
    }

    @Override
    @Transactional
    public boolean remove(Long id) {
        if (categoriaCocineroRepository.existsById(id)) {
            categoriaCocineroRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

