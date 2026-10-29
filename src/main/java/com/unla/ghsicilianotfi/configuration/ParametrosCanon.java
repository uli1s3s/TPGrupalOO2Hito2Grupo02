package com.unla.ghsicilianotfi.configuration;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Montos usados para calcular el canon de las unidades de venta.
 * Se encapsulan aca para no dejar valores fijos dentro de los metodos.
 * Cuando exista la entidad Costo del festival, estos valores van a salir de ahí.
 */
@Component
@Getter
public class ParametrosCanon {

    // Costo por cada m2 de superficie ocupada
    @Value("${canon.costo-por-m2:500}")
    private BigDecimal costoPorM2;

    // Adicional para los food trucks que usan conexion eléctrica
    @Value("${canon.plus-electricidad:2000}")
    private BigDecimal plusElectricidad;

    // Descuento por cada minuto de montaje de un puesto desmontable
    @Value("${canon.descuento-por-minuto-montaje:10}")
    private BigDecimal descuentoPorMinutoMontaje;
}