
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.repositories.IFestivalRepository;
import com.unla.ghsicilianotfi.services.implementation.RankingUnidadesService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class RankingUnidadesController {

    private final RankingUnidadesService rankingService;
    private final IFestivalRepository festivalRepository;

    public RankingUnidadesController(
            RankingUnidadesService rankingService,
            IFestivalRepository festivalRepository) {
        this.rankingService = rankingService;
        this.festivalRepository = festivalRepository;
    }

    @GetMapping("/ranking-unidades")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String verRanking(
            @RequestParam(required = false) Long idFestival,
            Model model) {

        List<Festival> festivales = festivalRepository.findAll();

        model.addAttribute("festivales", festivales);
        model.addAttribute("idFestivalSeleccionado", idFestival);

        if (festivales.isEmpty()) {
            model.addAttribute("mensaje",
                    "No hay festivales registrados. No es posible consultar el ranking de ventas.");
        } else if (idFestival != null) {

            if (!festivalRepository.existsById(idFestival)) {
                model.addAttribute("mensaje",
                        "El festival seleccionado no existe.");
            } else {
                model.addAttribute("ranking",
                        rankingService.obtenerRanking(idFestival));
            }
        }

        return "unidad/ranking-unidades";
    }
}
