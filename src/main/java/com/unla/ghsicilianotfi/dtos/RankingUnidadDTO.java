
package com.unla.ghsicilianotfi.dtos;

public class RankingUnidadDTO {

    private int posicion;
    private Long idUnidad;
    private String nombreComercial;
    private String codigoUnico;
    private Double recaudacion;

    public RankingUnidadDTO(Long idUnidad,
                            String nombreComercial,
                            String codigoUnico,
                            Double recaudacion) {
        this.idUnidad = idUnidad;
        this.nombreComercial = nombreComercial;
        this.codigoUnico = codigoUnico;
        this.recaudacion = recaudacion;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public Long getIdUnidad() {
        return idUnidad;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getCodigoUnico() {
        return codigoUnico;
    }

    public Double getRecaudacion() {
        return recaudacion;
    }
}
