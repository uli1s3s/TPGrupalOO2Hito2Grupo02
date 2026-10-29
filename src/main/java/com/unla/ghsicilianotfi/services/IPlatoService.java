package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.PlatoFormDTO;

import java.util.List;
import java.util.Optional;

public interface IPlatoService {

    List<PlatoFormDTO> listarPorResponsable(String nombreResponsable);

    Optional<PlatoFormDTO> buscarPorId(Long id, String nombreResponsable);

    PlatoFormDTO guardar(PlatoFormDTO datos, String nombreResponsable);

    boolean cambiarEstado(Long id, String nombreResponsable, boolean activo);
}
