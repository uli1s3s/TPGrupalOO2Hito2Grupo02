package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Resultado de la liquidacion de un empleado. No se persiste:
 * se calcula y se muestra en la vista.
 */
@Getter
@AllArgsConstructor
public class LiquidacionDTO {

    private Long idPersonal;
    private String empleado;          // "Apellido, Nombre"
    private String dni;
    private String tipo;              // Cocinero / Cajero
    private String unidad;            // Nombre comercial de la unidad
    private BigDecimal sueldoBase;
    private BigDecimal adicional;
    private String detalleAdicional;  // "Plus Lavaplatos" / "3 año(s) de antigüedad"
    private int diasFestival;
    private int porcentaje;           // 50, 100, 150...
    private BigDecimal totalACobrar;
}