
package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.DetallePedidoDTO;
import com.unla.ghsicilianotfi.dtos.RegistrarPedidoDTO;
import com.unla.ghsicilianotfi.entities.DetallePedido;
import com.unla.ghsicilianotfi.entities.Pedido;
import com.unla.ghsicilianotfi.entities.Plato;
import com.unla.ghsicilianotfi.repositories.IPedidoRepository;
import com.unla.ghsicilianotfi.repositories.IPlatoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class PedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IPlatoRepository platoRepository;

    public PedidoService(IPedidoRepository pedidoRepository,
                         IPlatoRepository platoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
    }

    @Transactional
    public Pedido registrarPedido(RegistrarPedidoDTO datos) {

        if (datos == null || datos.getDetalles() == null
                || datos.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un detalle"
            );
        }

        // TODO: Asociar con UnidadVenta cuando esté implementada.
        Pedido pedido = new Pedido(LocalDate.now());

        for (DetallePedidoDTO detalleDTO : datos.getDetalles()) {

            if (detalleDTO == null
                    || detalleDTO.getIdPlato() == null
                    || detalleDTO.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "El plato y la cantidad deben ser válidos"
                );
            }

            Plato plato = platoRepository
                    .findById(detalleDTO.getIdPlato())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "El plato no existe"
                            )
                    );

            if (!plato.isActivo()) {
                throw new IllegalArgumentException(
                        "El plato no está disponible"
                );
            }

            DetallePedido detalle = new DetallePedido(
                    detalleDTO.getCantidad(),
                    pedido,
                    plato
            );

            pedido.getDetalles().add(detalle);
        }

        return pedidoRepository.save(pedido);
    }
}
