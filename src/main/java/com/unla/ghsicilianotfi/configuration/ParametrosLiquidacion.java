package com.unla.ghsicilianotfi.configuration;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Montos usados en la liquidación de haberes.
 * Se encapsulan aca para no dejar valores fijos dentro de los metodos.
 * Cada valor se puede cambiar desde application.yml sin tocar codigo;
 * si no esta definido, se usa el valor por defecto (despues de los ':').
 */
@Component
@Getter
public class ParametrosLiquidacion {

    @Value("${liquidacion.sueldo-base:100000}")
    private BigDecimal sueldoBase;

    @Value("${liquidacion.monto-antiguedad-anual:5000}")
    private BigDecimal montoAntiguedadAnual;

    @Value("${liquidacion.plus-cocinero:150000}")
    private BigDecimal plusCocinero;

    @Value("${liquidacion.plus-cocinero-ayudante:100000}")
    private BigDecimal plusCocineroAyudante;

    @Value("${liquidacion.plus-lavaplatos:50000}")
    private BigDecimal plusLavaplatos;

    // Cada tramo de 15 dias equivale a un 50% del sueldo
    @Value("${liquidacion.dias-por-tramo:15}")
    private int diasPorTramo;

    @Value("${liquidacion.porcentaje-por-tramo:0.50}")
    private BigDecimal porcentajePorTramo;
}