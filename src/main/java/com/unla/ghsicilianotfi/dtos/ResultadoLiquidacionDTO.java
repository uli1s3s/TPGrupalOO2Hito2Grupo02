package com.unla.ghsicilianotfi.dtos;

import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoLiquidacionDTO {

    private int totalEvaluados;
    private int cantidadGeneradas;
    private int cantidadYaLiquidados;
    private int cantidadDiasInsuficientes;
    private List<LiquidacionDeHaber> liquidacionesGeneradas = new ArrayList<>();
}

