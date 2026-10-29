package com.unla.ghsicilianotfi.dtos;

import java.time.LocalDate;

public record AsignacionActualDTO(
        String empleado,
        String funcion,
        String unidad,
        String festival,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}