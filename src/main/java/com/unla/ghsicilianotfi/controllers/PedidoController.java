
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.RegistrarPedidoDTO;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.Pedido;
import com.unla.ghsicilianotfi.entities.Plato;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import com.unla.ghsicilianotfi.repositories.ICierreCajaRepository;
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
    private final ICierreCajaRepository cierreCajaRepository;

    public PedidoController(
            IPedidoRepository pedidoRepository,
            IPlatoRepository platoRepository,
            IFestivalRepository festivalRepository,
            IUnidadVentaRepository unidadVentaRepository,
            PedidoService pedidoService,
            ICierreCajaRepository cierreCajaRepository) {

        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.pedidoService = pedidoService;
        this.cierreCajaRepository = cierreCajaRepository;
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

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );

            return "redirect:/pedidos";
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
        model.addAttribute("cajaCerrada", false);

        Long festivalAplicado = esAdministrador
                ? idFestival
                : null;

        Long unidadAplicada = esAdministrador
                ? idUnidad
                : null;

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

            unidadesAutorizadas = unidadVentaRepository.findAll();

        } else if (esResponsable) {

            unidadesAutorizadas = unidadVentaRepository
                    .findByResponsable_Nombre(username);

        } else if (esEmpleado) {

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
        // FILTROS PARA ADMINISTRADOR
        // ============================================

        if (esAdministrador) {

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

            model.addAttribute("festivalesFiltro", List.of());
            model.addAttribute("unidadesFiltro", List.of());
        }

        // ============================================
        // CONSULTAR PEDIDOS
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

            model.addAttribute("pedidos", List.of());

        } else {

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
        // FORMULARIO SOLO PARA EMPLEADOS
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

        // ============================================
        // VERIFICAR SI LA CAJA ESTÁ CERRADA
        // ============================================

        boolean cajaCerrada = cierreCajaRepository
                .existsByUnidadVenta_IdUnidadAndFechaAndCerradaTrue(
                        unidad.getIdUnidad(),
                        LocalDate.now()
                );

        model.addAttribute("cajaCerrada", cajaCerrada);

        if (cajaCerrada) {

            model.addAttribute(
                    "avisoPedido",
                    "No es posible registrar nuevos pedidos porque "
                            + "la caja de esta unidad está cerrada. "
                            + "Para continuar, un cajero debe reabrirla."
            );

            model.addAttribute("platos", List.of());
            model.addAttribute("festivales", List.of());

            return;
        }

        // ============================================
        // FESTIVALES ACTIVOS
        // ============================================

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

        // ============================================
        // PLATOS ACTIVOS DE LA UNIDAD
        // ============================================

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
