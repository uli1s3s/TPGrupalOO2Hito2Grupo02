package com.unla.ghsicilianotfi.entities;

public enum TipoUnidadAplicable {
    TODAS("Todas las unidades"),
    FOOD_TRUCK("Food truck"),
    PUESTO_DESMONTABLE("Puesto desmontable");

    private final String descripcion;

    TipoUnidadAplicable(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
