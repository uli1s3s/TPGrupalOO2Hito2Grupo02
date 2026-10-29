package com.unla.ghsicilianotfi.repositories;

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

    // Validación para no liquidar por duplicado el mismo período al mismo empleado
    boolean existsByPersonalAndPeriodo(Personal personal, String periodo);
}

