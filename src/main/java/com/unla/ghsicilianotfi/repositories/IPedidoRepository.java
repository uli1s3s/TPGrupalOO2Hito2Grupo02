
package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

}