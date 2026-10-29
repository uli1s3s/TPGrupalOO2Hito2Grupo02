
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
    public Pedido registrarPedido(RegistrarPedidoDTO datos, String username) {

        // Validación de datos obligatorios
        if (datos == null
                || datos.getIdFestival() == null
                || datos.getIdUnidad() == null
                || datos.getDetalles() == null
                || datos.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "Seleccioná un festival, una unidad y al menos un plato"
            );
        }

        // Verifica que el festival exista y esté activo
        Festival festival = festivalRepository.findById(datos.getIdFestival())
                .filter(Festival::isActivo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El festival seleccionado no existe o no está activo"
                ));

        // Busca la unidad de venta seleccionada
        UnidadVenta unidad = unidadVentaRepository.findById(datos.getIdUnidad())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La unidad seleccionada no existe"
                ));

        // NUEVA VALIDACIÓN:
        // Busca la unidad asignada al empleado autenticado
        UnidadVenta unidadEmpleado = unidadVentaRepository
                .buscarUnidadDeEmpleado(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El empleado no tiene una unidad de venta asignada"
                ));

        // Comprueba que el empleado registre pedidos en su propia unidad
        if (!unidadEmpleado.getIdUnidad().equals(unidad.getIdUnidad())) {

            throw new IllegalArgumentException(
                    "No podés registrar pedidos en una unidad de venta diferente a la asignada"
            );
        }

        // Verifica que la unidad participe en el festival
        if (festival.getUnidades().stream()
                .noneMatch(unidadParticipante ->
                        unidadParticipante.getIdUnidad().equals(unidad.getIdUnidad()))) {

            throw new IllegalArgumentException(
                    "La unidad seleccionada no participa en el festival"
            );
        }

        // Crea el pedido
        Pedido pedido = new Pedido(LocalDate.now());
        pedido.setFestival(festival);
        pedido.setUnidadVenta(unidad);

        // Registra los detalles del pedido
        for (DetallePedidoDTO detalleDTO : datos.getDetalles()) {

            // Verifica que el plato y la cantidad sean válidos
            if (detalleDTO == null
                    || detalleDTO.getIdPlato() == null
                    || detalleDTO.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "El plato y la cantidad deben ser válidos"
                );
            }

            // Busca el plato seleccionado
            Plato plato = platoRepository
                    .findById(detalleDTO.getIdPlato())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El plato no existe"
                    ));

            // Verifica que el plato esté activo
            if (!plato.isActivo()) {

                throw new IllegalArgumentException(
                        "El plato no está disponible"
                );
            }

            // Verifica que el plato pertenezca a la unidad seleccionada
            if (plato.getUnidadVenta() == null
                    || !plato.getUnidadVenta().getIdUnidad()
                    .equals(unidad.getIdUnidad())) {

                throw new IllegalArgumentException(
                        "Todos los platos del pedido deben pertenecer a la unidad seleccionada"
                );
            }

            // Crea el detalle y conserva el precio y costo de ese momento
            DetallePedido detalle = new DetallePedido(
                    detalleDTO.getCantidad(),
                    pedido,
                    plato
            );

            // Agrega el detalle al pedido
            pedido.agregarDetalle(detalle);
        }

        // Guarda el pedido y sus detalles mediante CascadeType.ALL
        return pedidoRepository.save(pedido);
    }
}
