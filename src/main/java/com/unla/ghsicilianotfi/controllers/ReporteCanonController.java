package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.FestivalFormDTO;
import com.unla.ghsicilianotfi.dtos.ReporteMayoresCanonDTO;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Reporte del administrador: dado un festival, las "x" unidades de venta
 * que mas pagan de canon (calculado con los costos de ese festival).
 */
@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReporteCanonController {

    private final IUnidadVentaService unidadVentaService;
    private final IFestivalService festivalService;

    public ReporteCanonController(IUnidadVentaService unidadVentaService,
                                  IFestivalService festivalService) {
        this.unidadVentaService = unidadVentaService;
        this.festivalService = festivalService;
    }

    // ==========================================
    // Unidades con mayor canon de un festival (por defecto, las 3 primeras)
    // ==========================================
    @GetMapping("/mayor-canon")
    public String unidadesConMayorCanon(@RequestParam(name = "festival", required = false) Long idFestival,
                                        @RequestParam(name = "cantidad", defaultValue = "3") int cantidad,
                                        Model model,
                                        Authentication authentication) {

        // El admin puede consultar cualquier festival (activo o no)
        List<FestivalFormDTO> festivales = festivalService.listar();

        // Si no eligio festival, se muestra el primero de la lista
        if (idFestival == null && !festivales.isEmpty()) {
            idFestival = festivales.get(0).getIdFestival();
        }

        List<ReporteMayoresCanonDTO> reporte = new ArrayList<>();

        if (idFestival != null) {
            try {
                reporte = unidadVentaService.unidadesConMayorCanon(idFestival, cantidad);
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        model.addAttribute("reporte", reporte);
        model.addAttribute("festivales", festivales);
        model.addAttribute("idFestival", idFestival);
        model.addAttribute("cantidad", cantidad);

        // Datos para la barra de navegacion
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).findFirst().orElse("SIN_ROL"));

        return ViewRouteHelper.REPORTE_MAYOR_CANON;
    }
}