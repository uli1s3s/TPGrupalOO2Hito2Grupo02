package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/unidades")
public class UnidadVentaController {

    private final IUnidadVentaService unidadVentaService;

    public UnidadVentaController(IUnidadVentaService unidadVentaService) {
        this.unidadVentaService = unidadVentaService;
    }
    
    // ==========================================
    // Listar todas las unidades de venta
    // ==========================================
    @GetMapping
    public String listarUnidades(Model model, Authentication authentication) {
        model.addAttribute("unidades", unidadVentaService.listarUnidades());
        agregarDatosUsuario(model, authentication);
        return ViewRouteHelper.UNIDAD_LISTA;
    }

    // ==========================================
    // Mostrar el formulario de alta
    // ==========================================
    @GetMapping("/nueva")
    public String mostrarFormulario(Model model, Authentication authentication) {
        model.addAttribute("registro", new RegistroUnidadVentaDTO());
        agregarDatosUsuario(model, authentication);
        return ViewRouteHelper.UNIDAD_NUEVA;
    }

    // ==========================================
    // Procesar el alta de la unidad y su responsable
    // ==========================================
    @PostMapping("/nueva")
    public String registrarUnidad(@Valid @ModelAttribute("registro") RegistroUnidadVentaDTO registro,
                                  BindingResult resultado,
                                  Model model,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        // Errores de formato 
        if (resultado.hasErrors()) {
            agregarDatosUsuario(model, authentication);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }

        try {
            
            unidadVentaService.registrarUnidad(registro);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Unidad de venta cargada correctamente");

            return "redirect:" + ViewRouteHelper.UNIDAD_ROOT;

        } catch (IllegalArgumentException e) {
            // Error de una regla de negocio (código repetido, email existente, etc.):
            // se vuelve al formulario con los datos cargados y el mensaje de error
            model.addAttribute("error", e.getMessage());
            agregarDatosUsuario(model, authentication);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }
    }

    // Datos que usa la barra de navegacion del layout
    private void agregarDatosUsuario(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(autoridad -> autoridad.getAuthority())
                .findFirst()
                .orElse("SIN_ROL"));
    }
}