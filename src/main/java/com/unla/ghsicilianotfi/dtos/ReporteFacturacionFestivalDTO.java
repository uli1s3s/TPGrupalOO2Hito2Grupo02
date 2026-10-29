package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fila del ranking de facturación por festival.
 */
@Getter
@AllArgsConstructor
public class ReporteFacturacionFestivalDTO {

    private Long idFestival;
    private String nombre;
    private Double facturacion;
}
