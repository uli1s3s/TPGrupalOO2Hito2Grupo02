package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Festival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IFestivalRepository extends JpaRepository<Festival, Long> {

    @Override
    @EntityGraph(attributePaths = {"costos", "unidades", "unidades.responsable"})
    List<Festival> findAll();

    @EntityGraph(attributePaths = {"costos", "unidades", "unidades.responsable"})
    List<Festival> findByActivoTrue();

    @Override
    @EntityGraph(attributePaths = {"costos", "unidades", "unidades.responsable"})
    Optional<Festival> findById(Long id);

    @Query("""
            select f.idFestival as idFestival,
                   f.nombre as nombre,
                   coalesce(sum(detalle.cantidad * plato.precioVenta), 0.0) as facturacion
            from Festival f
            left join f.pedidos pedido
            left join pedido.detalles detalle
            left join detalle.plato plato
            group by f.idFestival, f.nombre
            order by coalesce(sum(detalle.cantidad * plato.precioVenta), 0.0) desc,
                     f.nombre asc,
                     f.idFestival asc
            """)
    List<FestivalFacturacionProjection> findFacturacionPorFestival();
}
