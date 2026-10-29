
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
import org.springframework.transaction.annotation.Transactional;
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

    // ============================================
    // LISTADO Y FILTROS DE PEDIDOS
    // ============================================

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSABLE', 'EMPLEADO')")
    @GetMapping("/pedidos")
    @Transactional(readOnly = true)
    public String listarPedidos(
            Model model,
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,
            @RequestParam(required = false)
            Long idFestival,
            @RequestParam(required = false)
            Long idUnidad) {

        cargarDatos(
                model,
                authentication,
                desde,
                hasta,
                idFestival,
                idUnidad
        );

        return "pedido/lista";
    }

    // ============================================
    // REGISTRAR PEDIDO
    // ============================================

    @PreAuthorize("hasRole('EMPLEADO')")
    @PostMapping("/pedidos/nuevo")
    @Transactional
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

            cargarDatos(
                    model,
                    authentication,
                    null,
                    null,
                    null,
                    null
            );

            return "pedido/lista";
        }
    }

    // ============================================
    // CARGAR DATOS PARA LA VISTA
    // ============================================

    private void cargarDatos(
            Model model,
            Authentication authentication,
            LocalDate desde,
            LocalDate hasta,
            Long idFestival,
            Long idUnidad) {

        String username = authentication.getName();

        boolean esAdministrador = tieneRol(
                authentication, "ROLE_ADMINISTRADOR");

        boolean esResponsable = tieneRol(
                authentication, "ROLE_RESPONSABLE");

        boolean esEmpleado = tieneRol(
                authentication, "ROLE_EMPLEADO");

        model.addAttribute("esEmpleado", esEmpleado);
        model.addAttribute("esAdministrador", esAdministrador);

        // Los filtros de festival y unidad
        // solamente corresponden al administrador.
        Long festivalAplicado = esAdministrador
                ? idFestival
                : null;

        Long unidadAplicada = esAdministrador
                ? idUnidad
                : null;

        // Conserva los filtros seleccionados.
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("idFestivalFiltro", festivalAplicado);
        model.addAttribute("idUnidadFiltro", unidadAplicada);

        // ============================================
        // UNIDADES AUTORIZADAS SEGÚN EL ROL
        // ============================================

        List<UnidadVenta> unidadesAutorizadas;
        Optional<UnidadVenta> unidadEmpleado = Optional.empty();

        if (esAdministrador) {

            // El administrador puede consultar todas las unidades.
            unidadesAutorizadas = unidadVentaRepository.findAll();

        } else if (esResponsable) {

            // El responsable consulta sus unidades.
            unidadesAutorizadas = unidadVentaRepository
                    .findByResponsable_Nombre(username);

        } else if (esEmpleado) {

            // El empleado consulta su unidad asignada.
            unidadEmpleado = unidadVentaRepository
                    .buscarUnidadDeEmpleado(username);

            unidadesAutorizadas = unidadEmpleado
                    .map(List::of)
                    .orElseGet(List::of);

        } else {

            unidadesAutorizadas = List.of();
        }

        List<Long> idsUnidades = unidadesAutorizadas
                .stream()
                .map(UnidadVenta::getIdUnidad)
                .toList();

        // ============================================
        // OPCIONES DE LOS FILTROS
        // SOLO ADMINISTRADOR
        // ============================================

        if (esAdministrador) {

            // Incluye festivales activos e inactivos
            // para permitir consultas históricas.
            List<Festival> festivalesFiltro =
                    festivalRepository.findAll();

            model.addAttribute(
                    "festivalesFiltro",
                    festivalesFiltro
            );

            model.addAttribute(
                    "unidadesFiltro",
                    unidadesAutorizadas
            );

        } else {

            // No se cargan filtros adicionales
            // para responsables ni empleados.
            model.addAttribute("festivalesFiltro", List.of());
            model.addAttribute("unidadesFiltro", List.of());
        }

        // ============================================
        // VALIDACIONES DE LA BÚSQUEDA
        // ============================================

        if (desde != null
                && hasta != null
                && desde.isAfter(hasta)) {

            model.addAttribute(
                    "error",
                    "La fecha inicial no puede ser posterior a la fecha final."
            );

            model.addAttribute("pedidos", List.of());

        } else if (!esAdministrador && idsUnidades.isEmpty()) {

            // Sin unidades autorizadas no se muestran pedidos.
            model.addAttribute("pedidos", List.of());

        } else {

            // El repositorio aplica los filtros.
            // Para responsables y empleados, los filtros
            // de festival y unidad siempre serán null.
            List<Long> idsConsulta = esAdministrador
                    ? List.of(-1L)
                    : idsUnidades;

            List<Pedido> pedidos = pedidoRepository.buscarPedidos(
                    desde,
                    hasta,
                    festivalAplicado,
                    unidadAplicada,
                    esAdministrador,
                    idsConsulta
            );

            model.addAttribute("pedidos", pedidos);
        }

        // ============================================
        // FORMULARIO DE REGISTRO
        // SOLO PARA EMPLEADOS
        // ============================================

        if (!esEmpleado) {

            model.addAttribute("platos", List.of());
            model.addAttribute("festivales", List.of());

            return;
        }

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

        // Solo festivales activos para registrar pedidos.
        List<Festival> festivalesDisponibles =
                festivalRepository.findByActivoTrue()
                        .stream()
                        .filter(festival ->
                                festival.getUnidades()
                                        .stream()
                                        .anyMatch(unidadParticipante ->
                                                unidadParticipante.getIdUnidad()
                                                        .equals(
                                                                unidad.getIdUnidad()
                                                        )
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

        model.addAttribute(
                "idUnidadEmpleado",
                unidad.getIdUnidad()
        );

        if (festivalesDisponibles.isEmpty()) {

            model.addAttribute(
                    "avisoPedido",
                    "Tu unidad de venta no participa en ningún festival activo."
            );
        }
    }

    // ============================================
    // VERIFICAR ROL DEL USUARIO
    // ============================================

    private boolean tieneRol(
            Authentication authentication,
            String rol) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(autoridad ->
                        autoridad.getAuthority().equals(rol));
    }
}
