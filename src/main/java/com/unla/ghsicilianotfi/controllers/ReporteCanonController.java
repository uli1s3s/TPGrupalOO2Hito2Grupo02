package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

/**
 * Reporte del administrador: las "x" unidades de venta que más pagan de canon.
 */
@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReporteCanonController {

    private final IUnidadVentaService unidadVentaService;

    public ReporteCanonController(IUnidadVentaService unidadVentaService) {
        this.unidadVentaService = unidadVentaService;
    }

    // ==========================================
    // Unidades con mayor canon (por defecto, las 3 primeras)
    // ==========================================
    @GetMapping("/mayor-canon")
    public String unidadesConMayorCanon(@RequestParam(name = "cantidad", defaultValue = "3") int cantidad,
                                        Model model,
                                        Authentication authentication) {

        try {
            model.addAttribute("reporte", unidadVentaService.unidadesConMayorCanon(cantidad));
        } catch (IllegalArgumentException e) {
            // Por ejemplo, si se pide una cantidad menor a 1
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reporte", new ArrayList<>());
        }

        model.addAttribute("cantidad", cantidad);

        // Datos para la barra de navegación
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).findFirst().orElse("SIN_ROL"));

        return ViewRouteHelper.REPORTE_MAYOR_CANON;
    }
}