package com.unla.ghsicilianotfi.repositories;

public interface PlatoRentabilidadProjection {

    Long getIdPlato();

    String getNombre();

    String getNombreUnidad();

    Double getPrecioVenta();

    Double getCostoProduccion();

    Long getCantidadVendida();

    Double getFacturacionTotal();

    Double getCostoTotal();

    Boolean getActivo();
}
