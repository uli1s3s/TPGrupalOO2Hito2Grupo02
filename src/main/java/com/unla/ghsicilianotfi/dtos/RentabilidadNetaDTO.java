package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RentabilidadNetaDTO {

    private Long idUnidad;
    private String nombreComercial;
    private String codigoUnico;

    private Long idFestival;
    private String nombreFestival;

    private BigDecimal ventas;
    private BigDecimal costoPlatos;
    private BigDecimal gananciaBruta;
    private BigDecimal sueldos;
    private BigDecimal canon;
    private BigDecimal rentabilidadNeta;
}