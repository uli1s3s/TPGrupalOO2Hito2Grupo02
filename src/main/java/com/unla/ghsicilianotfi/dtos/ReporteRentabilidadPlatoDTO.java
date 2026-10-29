package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ReporteRentabilidadPlatoDTO {

    private Long idPlato;
    private String nombre;
    private String nombreUnidad;
    private BigDecimal precioVenta;
    private BigDecimal costoProduccion;
    private BigDecimal rentabilidadUnitaria;
    private BigDecimal margenPorcentaje;
    private Long cantidadVendida;
    private BigDecimal facturacionTotal;
    private BigDecimal rentabilidadTotal;
    private boolean activo;
}
