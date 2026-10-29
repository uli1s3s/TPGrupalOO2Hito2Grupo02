package com.unla.ghsicilianotfi.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Montos que se aplican para calcular el canon de una unidad en particular.
 * Se arma a partir de los costos del festival de la unidad
 * (o de los valores por defecto si el festival no los configuro).
 */
@Getter
@AllArgsConstructor
public class TarifaCanon {

    private BigDecimal costoPorM2;
    private BigDecimal plusElectricidad;
    private BigDecimal descuentoPorMinutoMontaje;
}