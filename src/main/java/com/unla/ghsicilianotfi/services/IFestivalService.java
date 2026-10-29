package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.FestivalFormDTO;
import com.unla.ghsicilianotfi.dtos.ReporteFacturacionFestivalDTO;

import java.util.List;
import java.util.Optional;

public interface IFestivalService {

    List<FestivalFormDTO> listar();

    List<ReporteFacturacionFestivalDTO> listarFacturacion();

    List<FestivalFormDTO> listarActivos();

    Optional<FestivalFormDTO> buscarPorId(Long id);

    FestivalFormDTO guardar(FestivalFormDTO datos);

    boolean eliminar(Long id);
}
