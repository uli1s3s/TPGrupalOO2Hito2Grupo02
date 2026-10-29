package com.unla.ghsicilianotfi.dtos;

import lombok.Getter;

@Getter
public class PlatoEstrellaDTO {

    private final Long idUnidad;
    private final Long idPlato;
    private final Long cantidadVendida;

    public PlatoEstrellaDTO(Long idUnidad, Long idPlato, Long cantidadVendida) {
        this.idUnidad = idUnidad;
        this.idPlato = idPlato;
        this.cantidadVendida = cantidadVendida;
    }
}
