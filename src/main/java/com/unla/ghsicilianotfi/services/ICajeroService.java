package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.CajeroDTO;

import java.util.List;
import java.util.Optional;

public interface ICajeroService {

    List<CajeroDTO> getAll();

    List<CajeroDTO> getAllActivos();

    List<CajeroDTO> findConFiltros(String texto, String turno, Boolean activo, Double sueldoMinimo);

    Optional<CajeroDTO> findById(Long id);

    Optional<CajeroDTO> findByDni(String dni);

    List<CajeroDTO> findByTurno(String turno);

    CajeroDTO insertOrUpdate(CajeroDTO cajeroDTO);

    boolean darDeBajaLogica(Long id);

    boolean darDeAltaLogica(Long id);

    boolean remove(Long id);
}

