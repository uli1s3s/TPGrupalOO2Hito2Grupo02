package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {

    // Verifica si ya existe una unidad con ese código
    boolean existsByCodigoUnico(String codigoUnico);

    // Unidades asignadas a un responsable (por su nombre de usuario)
    @EntityGraph(attributePaths = {"responsable", "festivales"})
    List<UnidadVenta> findByResponsable_Nombre(String nombreUsuario);

    // Última unidad cargada con ese prefijo (para generar el siguiente código)
    Optional<UnidadVenta> findTopByCodigoUnicoStartingWithOrderByCodigoUnicoDesc(String prefijo);

    // Empleados activos que no están asignados a ninguna unidad (disponibles)
    @Query("SELECT p FROM Personal p WHERE p.activo = true "
         + "AND p.idPersonal NOT IN (SELECT e.idPersonal FROM UnidadVenta u JOIN u.empleados e)")
    List<Personal> buscarEmpleadosDisponibles();

    // Buscar un empleado (cocinero o cajero) por su id
    @Query("SELECT p FROM Personal p WHERE p.idPersonal = :idPersonal")
    Optional<Personal> buscarEmpleadoPorId(@Param("idPersonal") Long idPersonal);

    // Verifica si el empleado ya está asignado a alguna unidad
    boolean existsByEmpleados_IdPersonal(Long idPersonal);
    
    // Unidades que participan de un festival (relación muchos a muchos)
    @EntityGraph(attributePaths = {"responsable", "festivales"})
    List<UnidadVenta> findDistinctByFestivales_IdFestival(Long idFestival);
    
}