package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Fila del reporte "Unidades con mayor canon".
 * No se persiste: se arma en el momento a partir de las unidades de venta.
 */
@Getter
@AllArgsConstructor
public class ReporteMayoresCanonDTO {

    private String nombreComercial;
    private String codigo;
    private String tipoUnidad;
    private BigDecimal canon;
}