package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.FestivalFormDTO;
import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IFestivalService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Registro publico: una persona sin usuario se registra como responsable
 * y da de alta su unidad de venta en un festival activo, en el mismo paso.
 * Si ya hay una sesión iniciada (por ejemplo un administrador), no se permite.
 */
@Controller
@RequestMapping("/registro-responsable")
public class RegistroResponsableController {

    private final IUnidadVentaService unidadVentaService;
    private final IFestivalService festivalService;

    public RegistroResponsableController(IUnidadVentaService unidadVentaService,
                                         IFestivalService festivalService) {
        this.unidadVentaService = unidadVentaService;
        this.festivalService = festivalService;
    }

    // ==========================================
    // Mostrar el formulario de registro
    // Si se llega desde un festival (?festival=ID), queda preseleccionado
    // ==========================================
    @GetMapping
    public String mostrarFormulario(@RequestParam(name = "festival", required = false) Long idFestival,
                                    Model model,
                                    Authentication authentication) {

        RegistroUnidadVentaDTO registro = new RegistroUnidadVentaDTO();
        registro.setIdFestival(idFestival);
        model.addAttribute("registro", registro);
        cargarFestivales(model);

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
            cargarFestivales(model);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }

        // Errores de formato (campos vacios, email invalido, festival sin elegir, etc.)
        if (resultado.hasErrors()) {
            cargarFestivales(model);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }

        try {
            UnidadVenta unidad = unidadVentaService.registrarUnidad(registro);
            redirectAttributes.addFlashAttribute("codigoGenerado", unidad.getCodigoUnico());
            redirectAttributes.addFlashAttribute("emailRegistrado",
                    registro.getEmailResponsable().trim().toLowerCase());
            return "redirect:" + ViewRouteHelper.REGISTRO_RESPONSABLE_ROOT + "/exito";

        } catch (IllegalArgumentException e) {
            // Errores de negocio: festival inactivo, email ya registrado, etc.
            model.addAttribute("error", e.getMessage());
            cargarFestivales(model);
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

    // Solo se ofrecen festivales activos que todavia no terminaron
    private void cargarFestivales(Model model) {
        List<FestivalFormDTO> festivales = festivalService.listarActivos()
                .stream()
                .filter(festival -> !festival.getFechaFin().isBefore(LocalDate.now()))
                .toList();
        model.addAttribute("festivales", festivales);
    }
}