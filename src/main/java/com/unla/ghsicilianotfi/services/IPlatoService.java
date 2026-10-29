package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.PlatoFormDTO;
import com.unla.ghsicilianotfi.dtos.ReporteRentabilidadPlatoDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IPlatoService {

    List<PlatoFormDTO> listarPorResponsable(String nombreResponsable);

    List<ReporteRentabilidadPlatoDTO> listarRentabilidadPorResponsable(String nombreResponsable);

    Set<Long> listarIdsPlatosEstrellaPorResponsable(String nombreResponsable);

    Optional<PlatoFormDTO> buscarPorId(Long id, String nombreResponsable);

    PlatoFormDTO guardar(PlatoFormDTO datos, String nombreResponsable);

    boolean cambiarEstado(Long id, String nombreResponsable, boolean activo);
}
