package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.LiquidacionDTO;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import java.math.BigDecimal;

public interface ILiquidacionService {

    // Calcula lo que cobra un empleado segun su tipo y la duracion del festival
    LiquidacionDTO calcular(Personal empleado, UnidadVenta unidad, int diasFestival);

    // Porcentaje del sueldo segun los dias del festival (0.50, 1.00, 1.50...)
    BigDecimal calcularPorcentaje(int diasFestival);
}