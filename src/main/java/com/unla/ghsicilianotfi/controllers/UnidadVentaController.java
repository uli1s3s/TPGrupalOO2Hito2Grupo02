package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Consulta de unidades de venta para el administrador.
 * El alta de unidades la hace el responsable desde el registro público
 * (ver RegistroResponsableController): el administrador no puede registrarlas.
 */
@Controller
@RequestMapping("/unidades")
@PreAuthorize("hasRole('ADMINISTRADOR')")
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

    // Datos que usa la barra de navegación del layout
    private void agregarDatosUsuario(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(autoridad -> autoridad.getAuthority())
                .findFirst()
                .orElse("SIN_ROL"));
    }
}