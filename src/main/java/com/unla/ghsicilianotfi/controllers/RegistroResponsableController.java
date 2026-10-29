package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
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
import com.unla.ghsicilianotfi.entities.UnidadVenta;

/**
 * Registro publico: una persona sin usuario se registra como responsable
 * y da de alta su unidad de venta en el mismo paso.
 * Si ya hay una sesion iniciada (por ejemplo un administrador), no se permite.
 */
@Controller
@RequestMapping("/registro-responsable")
public class RegistroResponsableController {

    private final IUnidadVentaService unidadVentaService;

    public RegistroResponsableController(IUnidadVentaService unidadVentaService) {
        this.unidadVentaService = unidadVentaService;
    }

    // ==========================================
    // Mostrar el formulario de registro
    // ==========================================
    @GetMapping
    public String mostrarFormulario(Model model, Authentication authentication) {
        model.addAttribute("registro", new RegistroUnidadVentaDTO());

        // authentication es null cuando nadie inicio sesión
        if (authentication != null) {
            model.addAttribute("bloqueado", true);
        }
        return ViewRouteHelper.UNIDAD_NUEVA;
    }

    // ==========================================
    // Procesar el registro del responsable y su unidad
    // ==========================================
    @PostMapping
    public String registrar(@Valid @ModelAttribute("registro") RegistroUnidadVentaDTO registro,
                            BindingResult resultado,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        // Un usuario logueado (admin, responsable o empleado) no puede registrarse de nuevo
        if (authentication != null) {
            model.addAttribute("bloqueado", true);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }

        // Errores de formato (campos vacíos, email invalido, etc.)
        if (resultado.hasErrors()) {
            return ViewRouteHelper.UNIDAD_NUEVA;
        }

        try {
            UnidadVenta unidad = unidadVentaService.registrarUnidad(registro);
            redirectAttributes.addFlashAttribute("codigoGenerado", unidad.getCodigoUnico());
            redirectAttributes.addFlashAttribute("emailRegistrado",
                    registro.getEmailResponsable().trim().toLowerCase());
            return "redirect:" + ViewRouteHelper.REGISTRO_RESPONSABLE_ROOT + "/exito";

        } catch (IllegalArgumentException e) {
            // Errores de negocio: código repetido, email ya registrado, etc.
            model.addAttribute("error", e.getMessage());
            return ViewRouteHelper.UNIDAD_NUEVA;
        }
    }

    // ==========================================
    // Pantalla de registro exitoso
    // ==========================================
    @GetMapping("/exito")
    public String exito() {
        return ViewRouteHelper.REGISTRO_RESPONSABLE_EXITO;
    }
}