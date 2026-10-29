package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Plato;
import com.unla.ghsicilianotfi.dtos.PlatoEstrellaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IPlatoRepository extends JpaRepository<Plato, Long> {

    List<Plato> findByUnidadVenta_Responsable_NombreOrderByNombreAsc(String nombreResponsable);

    @Query("""
            select plato.idPlato as idPlato,
                   plato.nombre as nombre,
                   unidad.nombreComercial as nombreUnidad,
                   plato.precioVenta as precioVenta,
                   plato.costoProduccion as costoProduccion,
                   coalesce(sum(detalle.cantidad), 0) as cantidadVendida,
                   coalesce(sum(detalle.cantidad * detalle.precioUnitario), 0.0) as facturacionTotal,
                   coalesce(sum(detalle.cantidad * detalle.costoUnitario), 0.0) as costoTotal,
                   plato.activo as activo
            from Plato plato
            join plato.unidadVenta unidad
            left join DetallePedido detalle on detalle.plato = plato
            where unidad.responsable.nombre = :nombreResponsable
            group by plato.idPlato, plato.nombre, unidad.nombreComercial,
                     plato.precioVenta, plato.costoProduccion, plato.activo
            order by coalesce(sum(detalle.cantidad * (detalle.precioUnitario - detalle.costoUnitario)), 0.0) desc,
                     (plato.precioVenta - plato.costoProduccion) desc,
                     plato.nombre asc
            """)
    List<PlatoRentabilidadProjection> buscarRentabilidadPorResponsable(
            @Param("nombreResponsable") String nombreResponsable);

    @EntityGraph(attributePaths = "unidadVenta")
    List<Plato> findByActivoTrue();

    @Query("""
            SELECT new com.unla.ghsicilianotfi.dtos.PlatoEstrellaDTO(
                plato.unidadVenta.idUnidad,
                plato.idPlato,
                SUM(detalle.cantidad)
            )
            FROM DetallePedido detalle
            JOIN detalle.plato plato
            WHERE plato.unidadVenta.idUnidad IN :idsUnidades
            GROUP BY plato.unidadVenta.idUnidad, plato.idPlato
            ORDER BY plato.unidadVenta.idUnidad ASC,
                     SUM(detalle.cantidad) DESC,
                     plato.nombre ASC
            """)
    List<PlatoEstrellaDTO> buscarPlatosMasVendidosPorUnidad(
            @Param("idsUnidades") Set<Long> idsUnidades);
}