
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

    // Última unidad cargada con ese prefijo
    Optional<UnidadVenta> findTopByCodigoUnicoStartingWithOrderByCodigoUnicoDesc(String prefijo);

    // Empleados activos que no están asignados a ninguna unidad
    @Query("SELECT p FROM Personal p WHERE p.activo = true "
            + "AND p.idPersonal NOT IN (SELECT e.idPersonal FROM UnidadVenta u JOIN u.empleados e)")
    List<Personal> buscarEmpleadosDisponibles();

    // Buscar un empleado (cocinero o cajero) por su ID
    @Query("SELECT p FROM Personal p WHERE p.idPersonal = :idPersonal")
    Optional<Personal> buscarEmpleadoPorId(@Param("idPersonal") Long idPersonal);

    // Verifica si el empleado ya está asignado a alguna unidad
    boolean existsByEmpleados_IdPersonal(Long idPersonal);

    // Unidades que participan de un festival
    @EntityGraph(attributePaths = {"responsable", "festivales"})
    List<UnidadVenta> findDistinctByFestivales_IdFestival(Long idFestival);

    // Total vendido por una unidad en un festival
    // Utiliza el precio registrado al momento de realizar cada pedido
    @Query("SELECT COALESCE(SUM(d.cantidad * d.precioUnitario), 0.0) FROM DetallePedido d "
            + "WHERE d.pedido.unidadVenta.idUnidad = :idUnidad "
            + "AND d.pedido.festival.idFestival = :idFestival")
    Double sumarVentas(@Param("idUnidad") Long idUnidad,
                       @Param("idFestival") Long idFestival);

    // Costo de los platos vendidos por una unidad en un festival
    // Utiliza el costo registrado al momento de realizar cada pedido
    @Query("SELECT COALESCE(SUM(d.cantidad * d.costoUnitario), 0.0) FROM DetallePedido d "
            + "WHERE d.pedido.unidadVenta.idUnidad = :idUnidad "
            + "AND d.pedido.festival.idFestival = :idFestival")
    Double sumarCostoPlatos(@Param("idUnidad") Long idUnidad,
                            @Param("idFestival") Long idFestival);

    // Sueldos liquidados a los empleados de una unidad en un festival
    @Query("SELECT COALESCE(SUM(l.totalACobrar), 0.0) FROM LiquidacionDeHaber l "
            + "WHERE l.unidadVenta.idUnidad = :idUnidad "
            + "AND l.festival.idFestival = :idFestival")
    Double sumarSueldos(@Param("idUnidad") Long idUnidad,
                        @Param("idFestival") Long idFestival);

    @Query("""
    SELECT DISTINCT u
    FROM UnidadVenta u
    JOIN u.empleados e
    JOIN e.credenciales c
    WHERE c.nombre = :username
      AND e.activo = true
    """)
    Optional<UnidadVenta> buscarUnidadDeEmpleado(
            @Param("username") String username);
}
