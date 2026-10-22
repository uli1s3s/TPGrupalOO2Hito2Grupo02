
package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Pedido;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = {"detalles", "detalles.plato"})
    List<Pedido> findAll();
}
