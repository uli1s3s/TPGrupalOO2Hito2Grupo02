package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IFestivalService;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Consulta de unidades de venta para el administrador.
 * Puede ver todas o solo las de un festival.
 * El alta de unidades la hace el responsable desde el registro público
 * (ver RegistroResponsableController): el administrador no puede registrarlas.
 */
@Controller
@RequestMapping("/unidades")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UnidadVentaController {

    private final IUnidadVentaService unidadVentaService;
    private final IFestivalService festivalService;

    public UnidadVentaController(IUnidadVentaService unidadVentaService,
                                 IFestivalService festivalService) {
        this.unidadVentaService = unidadVentaService;
        this.festivalService = festivalService;
    }

    // ==========================================
    // Listar unidades de venta (todas o filtradas por festival)
    // ==========================================
    @GetMapping
    public String listarUnidades(@RequestParam(name = "festival", required = false) Long idFestival,
                                 Model model,
                                 Authentication authentication) {

        List<UnidadVenta> unidades = (idFestival == null)
                ? unidadVentaService.listarUnidades()
                : unidadVentaService.listarPorFestival(idFestival);

        model.addAttribute("unidades", unidades);
        model.addAttribute("festivales", festivalService.listar());
        model.addAttribute("idFestival", idFestival);
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