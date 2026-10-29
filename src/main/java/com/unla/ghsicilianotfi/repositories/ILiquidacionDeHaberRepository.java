package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;
import com.unla.ghsicilianotfi.entities.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("liquidacionDeHaberRepository")
public interface ILiquidacionDeHaberRepository extends JpaRepository<LiquidacionDeHaber, Long> {

    // Historial de recibos de un empleado ordenados del más reciente al más antiguo
    List<LiquidacionDeHaber> findByPersonalOrderByFechaLiquidacionDesc(Personal personal);

    // Historial buscando por el username del empleado autenticado
    List<LiquidacionDeHaber> findByPersonal_Credenciales_NombreOrderByFechaLiquidacionDesc(String nombreUsuario);

    // Historial para el responsable: recibos de los empleados pertenecientes a sus unidades de venta
    List<LiquidacionDeHaber> findByUnidadVenta_Responsable_NombreOrderByFechaLiquidacionDesc(String nombreUsuario);

    // Validación para no liquidar por duplicado el mismo período y festival al mismo empleado
    boolean existsByPersonalAndFestivalAndPeriodo(Personal personal, Festival festival, String periodo);

    // Validación general por período
    boolean existsByPersonalAndPeriodo(Personal personal, String periodo);
}

