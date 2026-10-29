
package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Pedido;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "detalles",
            "detalles.plato",
            "unidadVenta",
            "festival"
    })
    List<Pedido> findAll();

    // Filtra por fechas, festival y unidad de venta.
    // Todos los filtros son opcionales.
    // Siempre respeta las unidades autorizadas para el usuario.
    @EntityGraph(attributePaths = {
            "detalles",
            "detalles.plato",
            "unidadVenta",
            "festival"
    })
    @Query("""
            SELECT DISTINCT p
            FROM Pedido p
            WHERE (:desde IS NULL OR p.fechaPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaPedido <= :hasta)
              AND (:idFestival IS NULL
                   OR p.festival.idFestival = :idFestival)
              AND (:idUnidad IS NULL
                   OR p.unidadVenta.idUnidad = :idUnidad)
              AND (:sinRestriccion = true
                   OR p.unidadVenta.idUnidad IN :idsUnidades)
            ORDER BY p.fechaPedido DESC, p.idPedido DESC
            """)
    List<Pedido> buscarPedidos(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("idFestival") Long idFestival,
            @Param("idUnidad") Long idUnidad,
            @Param("sinRestriccion") boolean sinRestriccion,
            @Param("idsUnidades") List<Long> idsUnidades
    );

    // Se conserva para el cierre diario de caja.
    @EntityGraph(attributePaths = {
            "detalles",
            "detalles.plato"
    })
    List<Pedido> findByUnidadVenta_IdUnidadAndFechaPedido(
            Long idUnidad,
            LocalDate fechaPedido
    );
}
