package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import java.util.List;
import com.unla.ghsicilianotfi.dtos.ReporteMayoresCanonDTO;

public interface IUnidadVentaService {

    // Lista todas las unidades de venta registradas
    List<UnidadVenta> listarUnidades();

    // Registra una unidad (FoodTruck o PuestoDesmontable) junto con su usuario responsable
    UnidadVenta registrarUnidad(RegistroUnidadVentaDTO datos);
    
    // Unidades asignadas a un responsable (por su nombre de usuario)
    List<UnidadVenta> listarPorResponsable(String nombreUsuario);
    
    // Las cantidad unidades que más pagan de canon, de mayor a menor
    List<ReporteMayoresCanonDTO> unidadesConMayorCanon(int cantidad);
}

