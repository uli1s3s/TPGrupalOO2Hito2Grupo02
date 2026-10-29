
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
              AND (:sinRestriccion = true
                   OR p.unidadVenta.idUnidad IN :idsUnidades)
            ORDER BY p.fechaPedido DESC, p.idPedido DESC
            """)
    List<Pedido> buscarPedidos(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("sinRestriccion") boolean sinRestriccion,
            @Param("idsUnidades") List<Long> idsUnidades
    );
}
