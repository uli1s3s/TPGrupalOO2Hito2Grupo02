package com.unla.ghsicilianotfi.entities;

import java.util.List;

public enum TipoCostoFestival {
    POR_SUPERFICIE("Costo por superficie"),
    POR_MONTAJE("Costo por montaje"),
    USO_ELECTRICIDAD("Plus por uso de electricidad"),
    // Kept so existing database rows can still be read; it is no longer configurable.
    @Deprecated
    SUELDO_BASE("Sueldo base");

    private final String descripcion;

    TipoCostoFestival(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static List<TipoCostoFestival> configurables() {
        return List.of(POR_SUPERFICIE, POR_MONTAJE, USO_ELECTRICIDAD);
    }
}
