
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.RegistrarPedidoDTO;
import com.unla.ghsicilianotfi.entities.Pedido;
import com.unla.ghsicilianotfi.entities.Plato;
import com.unla.ghsicilianotfi.repositories.IPedidoRepository;
import com.unla.ghsicilianotfi.repositories.IPlatoRepository;
import com.unla.ghsicilianotfi.services.implementation.PedidoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PedidoController {

    private final IPedidoRepository pedidoRepository;
    private final IPlatoRepository platoRepository;
    private final PedidoService pedidoService;

    public PedidoController(IPedidoRepository pedidoRepository,
                            IPlatoRepository platoRepository,
                            PedidoService pedidoService) {

        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
        this.pedidoService = pedidoService;
    }


    // Permite consultar pedidos a los tres roles.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSABLE', 'EMPLEADO')")
    @GetMapping("/pedidos")
    public String listarPedidos(Model model, Authentication authentication) {

        // Carga los pedidos y platos desde la base de datos.
        cargarDatos(model);

        // Verifica si el usuario autenticado es un empleado.
        boolean esEmpleado = authentication.getAuthorities()
                .stream()
                .anyMatch(autoridad ->
                        autoridad.getAuthority().equals("ROLE_EMPLEADO"));

        // Envía el resultado al HTML para mostrar u ocultar el formulario.
        model.addAttribute("esEmpleado", esEmpleado);

        return "pedido/lista";
    }

    // Solo los empleados pueden registrar pedidos.
    @PreAuthorize("hasRole('EMPLEADO')")
    @PostMapping("/pedidos/nuevo")
    public String registrarPedido(
            @ModelAttribute RegistrarPedidoDTO datos,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            // Valida y guarda el pedido mediante el servicio.
            Pedido pedido = pedidoService.registrarPedido(datos);

            // Envía un mensaje de confirmación.
            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Pedido N.º " + pedido.getIdPedido()
                            + " registrado correctamente."
            );

            // Regresa al listado actualizado.
            return "redirect:/pedidos";

        } catch (IllegalArgumentException ex) {

            // Muestra el error y vuelve a cargar la página.
            model.addAttribute("error", ex.getMessage());

            model.addAttribute("esEmpleado", true);

            cargarDatos(model);

            return "pedido/lista";
        }
    }


    private void cargarDatos(Model model) {

        List<Pedido> pedidos = pedidoRepository.findAll();

        List<Plato> platosActivos = new ArrayList<>();

        for (Plato plato : platoRepository.findAll()) {

            if (plato.isActivo()) {
                platosActivos.add(plato);
            }
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("platos", platosActivos);
    }
}
