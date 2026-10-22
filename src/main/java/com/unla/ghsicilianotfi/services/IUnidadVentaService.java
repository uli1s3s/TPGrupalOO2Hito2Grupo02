package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import java.util.List;

public interface IUnidadVentaService {

    // Lista todas las unidades de venta registradas
    List<UnidadVenta> listarUnidades();

    // Registra una unidad (FoodTruck o PuestoDesmontable) junto con su usuario responsable
    UnidadVenta registrarUnidad(RegistroUnidadVentaDTO datos);
}
