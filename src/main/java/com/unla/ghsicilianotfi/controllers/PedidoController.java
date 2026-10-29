
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.RegistrarPedidoDTO;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.Pedido;
import com.unla.ghsicilianotfi.entities.Plato;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import com.unla.ghsicilianotfi.repositories.IFestivalRepository;
import com.unla.ghsicilianotfi.repositories.IPedidoRepository;
import com.unla.ghsicilianotfi.repositories.IPlatoRepository;
import com.unla.ghsicilianotfi.repositories.IUnidadVentaRepository;

import com.unla.ghsicilianotfi.services.implementation.PedidoService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class PedidoController {

    private final IPedidoRepository pedidoRepository;
    private final IPlatoRepository platoRepository;
    private final IFestivalRepository festivalRepository;
    private final IUnidadVentaRepository unidadVentaRepository;
    private final PedidoService pedidoService;

    public PedidoController(
            IPedidoRepository pedidoRepository,
            IPlatoRepository platoRepository,
            IFestivalRepository festivalRepository,
            IUnidadVentaRepository unidadVentaRepository,
            PedidoService pedidoService) {

        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.pedidoService = pedidoService;
    }

    // Consulta de pedidos según el rol y las fechas seleccionadas.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSABLE', 'EMPLEADO')")
    @GetMapping("/pedidos")
    public String listarPedidos(
            Model model,
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta) {

        cargarDatos(model, authentication, desde, hasta);

        return "pedido/lista";
    }

    // Solo los empleados pueden registrar pedidos.
    @PreAuthorize("hasRole('EMPLEADO')")
    @PostMapping("/pedidos/nuevo")
    public String registrarPedido(
            @ModelAttribute RegistrarPedidoDTO datos,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        try {

            Pedido pedido = pedidoService.registrarPedido(
                    datos,
                    authentication.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Pedido N.º " + pedido.getIdPedido()
                            + " registrado correctamente."
            );

            return "redirect:/pedidos";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("error", ex.getMessage());

            cargarDatos(model, authentication, null, null);

            return "pedido/lista";
        }
    }

    private void cargarDatos(
            Model model,
            Authentication authentication,
            LocalDate desde,
            LocalDate hasta) {

        String username = authentication.getName();

        boolean esAdministrador = tieneRol(
                authentication, "ROLE_ADMINISTRADOR");

        boolean esResponsable = tieneRol(
                authentication, "ROLE_RESPONSABLE");

        boolean esEmpleado = tieneRol(
                authentication, "ROLE_EMPLEADO");

        model.addAttribute("esEmpleado", esEmpleado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        // Identifica las unidades autorizadas según el rol.
        List<Long> idsUnidades;

        Optional<UnidadVenta> unidadEmpleado = Optional.empty();

        if (esAdministrador) {

            // El administrador puede consultar todos los pedidos.
            idsUnidades = List.of();

        } else if (esResponsable) {

            // El responsable consulta sus unidades.
            idsUnidades = unidadVentaRepository
                    .findByResponsable_Nombre(username)
                    .stream()
                    .map(UnidadVenta::getIdUnidad)
                    .toList();

        } else if (esEmpleado) {

            // El empleado consulta solamente su unidad asignada.
            unidadEmpleado =
                    unidadVentaRepository.buscarUnidadDeEmpleado(username);

            idsUnidades = unidadEmpleado
                    .map(unidad -> List.of(unidad.getIdUnidad()))
                    .orElseGet(List::of);

        } else {

            idsUnidades = List.of();
        }

        // Valida el rango de fechas.
        if (desde != null && hasta != null && desde.isAfter(hasta)) {

            model.addAttribute(
                    "error",
                    "La fecha inicial no puede ser posterior a la fecha final."
            );

            model.addAttribute("pedidos", List.of());

        } else if (!esAdministrador && idsUnidades.isEmpty()) {

            // Un usuario sin unidades autorizadas no ve pedidos.
            model.addAttribute("pedidos", List.of());

        } else {

            // Consulta los pedidos aplicando fechas y permisos.
            // El ID -1 evita pasar una lista vacía a la consulta.
            List<Long> idsConsulta = esAdministrador
                    ? List.of(-1L)
                    : idsUnidades;

            List<Pedido> pedidos = pedidoRepository.buscarPedidos(
                    desde,
                    hasta,
                    esAdministrador,
                    idsConsulta
            );

            model.addAttribute("pedidos", pedidos);
        }

        // El formulario de registro solamente corresponde a empleados.
        if (!esEmpleado) {

            model.addAttribute("platos", List.of());
            model.addAttribute("festivales", List.of());

            return;
        }

        // Si no tiene unidad, no puede registrar pedidos.
        if (unidadEmpleado.isEmpty()) {

            model.addAttribute("platos", List.of());
            model.addAttribute("festivales", List.of());

            model.addAttribute(
                    "avisoPedido",
                    "No tenés una unidad de venta activa asignada."
            );

            return;
        }

        UnidadVenta unidad = unidadEmpleado.get();

        // Festivales activos donde participa la unidad del empleado.
        List<Festival> festivalesDisponibles =
                festivalRepository.findByActivoTrue()
                        .stream()
                        .filter(festival ->
                                festival.getUnidades()
                                        .stream()
                                        .anyMatch(unidadParticipante ->
                                                unidadParticipante.getIdUnidad()
                                                        .equals(unidad.getIdUnidad())
                                        )
                        )
                        .toList();

        // Platos activos de la unidad asignada.
        List<Plato> platosDisponibles =
                platoRepository.findByActivoTrue()
                        .stream()
                        .filter(plato ->
                                plato.getUnidadVenta() != null
                                        && plato.getUnidadVenta()
                                        .getIdUnidad()
                                        .equals(unidad.getIdUnidad())
                        )
                        .toList();

        model.addAttribute("festivales", festivalesDisponibles);
        model.addAttribute("platos", platosDisponibles);

        // Enviamos la unidad al HTML para restringir el selector.
        model.addAttribute("idUnidadEmpleado", unidad.getIdUnidad());

        if (festivalesDisponibles.isEmpty()) {

            model.addAttribute(
                    "avisoPedido",
                    "Tu unidad de venta no participa en ningún festival activo."
            );
        }
    }

    private boolean tieneRol(
            Authentication authentication,
            String rol) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(autoridad ->
                        autoridad.getAuthority().equals(rol));
    }
}
