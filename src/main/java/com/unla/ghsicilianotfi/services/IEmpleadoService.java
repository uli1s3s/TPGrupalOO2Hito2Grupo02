package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.AltaEmpleadoDTO;

import java.util.List;
import java.util.Optional;

public interface IEmpleadoService {

    List<AltaEmpleadoDTO> traerTodosActivos();

    List<AltaEmpleadoDTO> filtrarActivos(String busqueda, String tipo);

    AltaEmpleadoDTO darDeAltaEmpleado(AltaEmpleadoDTO dto);

    AltaEmpleadoDTO actualizarEmpleado(AltaEmpleadoDTO dto);

    boolean darDeBajaLogica(Long idPersonal);

    boolean darDeAltaLogica(Long idPersonal);

    Optional<AltaEmpleadoDTO> buscarPorId(Long idPersonal);
}

