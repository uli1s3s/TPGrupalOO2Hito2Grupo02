
package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.DetallePedidoDTO;
import com.unla.ghsicilianotfi.dtos.RegistrarPedidoDTO;
import com.unla.ghsicilianotfi.entities.DetallePedido;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.Pedido;
import com.unla.ghsicilianotfi.entities.Plato;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.repositories.IFestivalRepository;
import com.unla.ghsicilianotfi.repositories.IPedidoRepository;
import com.unla.ghsicilianotfi.repositories.IPlatoRepository;
import com.unla.ghsicilianotfi.repositories.IUnidadVentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class PedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IPlatoRepository platoRepository;
    private final IFestivalRepository festivalRepository;
    private final IUnidadVentaRepository unidadVentaRepository;

    public PedidoService(IPedidoRepository pedidoRepository,
                         IPlatoRepository platoRepository,
                         IFestivalRepository festivalRepository,
                         IUnidadVentaRepository unidadVentaRepository) {

        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Transactional
    public Pedido registrarPedido(RegistrarPedidoDTO datos) {

        if (datos == null || datos.getIdFestival() == null || datos.getIdUnidad() == null
                || datos.getDetalles() == null
                || datos.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "Seleccioná un festival, una unidad y al menos un plato"
            );
        }

        Festival festival = festivalRepository.findById(datos.getIdFestival())
                .filter(Festival::isActivo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El festival seleccionado no existe o no está activo"));
        UnidadVenta unidad = unidadVentaRepository.findById(datos.getIdUnidad())
                .orElseThrow(() -> new IllegalArgumentException("La unidad seleccionada no existe"));
        if (festival.getUnidades().stream()
                .noneMatch(unidadParticipante -> unidadParticipante.getIdUnidad().equals(unidad.getIdUnidad()))) {
            throw new IllegalArgumentException("La unidad seleccionada no participa en el festival");
        }

        Pedido pedido = new Pedido(LocalDate.now());
        pedido.setFestival(festival);
        pedido.setUnidadVenta(unidad);

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
            if (plato.getUnidadVenta() == null
                    || !plato.getUnidadVenta().getIdUnidad().equals(unidad.getIdUnidad())) {
                throw new IllegalArgumentException(
                        "Todos los platos del pedido deben pertenecer a la unidad seleccionada");
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
