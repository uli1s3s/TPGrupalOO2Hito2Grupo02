package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import java.util.List;
import com.unla.ghsicilianotfi.dtos.ReporteMayoresCanonDTO;
import com.unla.ghsicilianotfi.entities.Personal;

import java.math.BigDecimal;
import java.util.Map;

public interface IUnidadVentaService {

    // Lista todas las unidades de venta registradas
    List<UnidadVenta> listarUnidades();

    // Registra una unidad (FoodTruck o PuestoDesmontable) junto con su usuario responsable
    UnidadVenta registrarUnidad(RegistroUnidadVentaDTO datos);
    
    // Unidades asignadas a un responsable (por su nombre de usuario)
    List<UnidadVenta> listarPorResponsable(String nombreUsuario);
    
    // Las "cantidad" unidades de un festival que más pagan de canon, de mayor a menor
    List<ReporteMayoresCanonDTO> unidadesConMayorCanon(Long idFestival, int cantidad);
    
    // Empleados activos que no estAn asignados a ninguna unidad
    List<Personal> listarEmpleadosDisponibles();

    // El responsable asigna un empleado disponible a una de sus unidades
    void asignarEmpleado(Long idUnidad, Long idPersonal, String nombreResponsable);

    // El responsable quita un empleado de una de sus unidades
    void quitarEmpleado(Long idUnidad, Long idPersonal, String nombreResponsable);

    // Canon que paga la unidad en cada uno de sus festivales (nombre del festival -> canon)
    Map<String, BigDecimal> calcularCanonPorFestival(Long idUnidad);
    
    // Unidades de venta que participan de un festival
    List<UnidadVenta> listarPorFestival(Long idFestival);
}

