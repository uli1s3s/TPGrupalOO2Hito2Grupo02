package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.CostoFestivalFormDTO;
import com.unla.ghsicilianotfi.dtos.FestivalFormDTO;
import com.unla.ghsicilianotfi.entities.TipoCostoFestival;
import com.unla.ghsicilianotfi.entities.TipoUnidadAplicable;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IFestivalService;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.EnumMap;
import java.util.Map;

@Controller
@RequestMapping("/festivales")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class FestivalController {

    private final IFestivalService festivalService;
    private final IUnidadVentaService unidadVentaService;

    public FestivalController(IFestivalService festivalService,
                              IUnidadVentaService unidadVentaService) {
        this.festivalService = festivalService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("festivales", festivalService.listar());
        return ViewRouteHelper.FESTIVAL_INDEX;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("festival", nuevoFestival());
        agregarOpcionesFormulario(model);
        return ViewRouteHelper.FESTIVAL_FORM;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("festival") FestivalFormDTO festival,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            agregarOpcionesFormulario(model);
            return ViewRouteHelper.FESTIVAL_FORM;
        }

        try {
            boolean esEdicion = festival.getIdFestival() != null;
            festivalService.guardar(festival);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    esEdicion ? "Festival actualizado correctamente."
                            : "Festival creado correctamente.");
            return "redirect:/festivales";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("festival.invalido", e.getMessage());
            agregarOpcionesFormulario(model);
            return ViewRouteHelper.FESTIVAL_FORM;
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        return festivalService.buscarPorId(id)
                .map(festival -> {
                    prepararCostos(festival);
                    model.addAttribute("festival", festival);
                    agregarOpcionesFormulario(model);
                    return ViewRouteHelper.FESTIVAL_FORM;
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el festival.");
                    return "redirect:/festivales";
                });
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (festivalService.eliminar(id)) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Festival eliminado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se encontró el festival o tiene pedidos asociados y no se puede eliminar.");
        }
        return "redirect:/festivales";
    }

    private FestivalFormDTO nuevoFestival() {
        FestivalFormDTO festival = new FestivalFormDTO();
        prepararCostos(festival);
        return festival;
    }

    private void prepararCostos(FestivalFormDTO festival) {
        Map<TipoCostoFestival, CostoFestivalFormDTO> costosExistentes = new EnumMap<>(TipoCostoFestival.class);
        if (festival.getCostos() != null) {
            festival.getCostos().forEach(costo -> costosExistentes.put(costo.getTipoCosto(), costo));
        }

        festival.setCostos(TipoCostoFestival.configurables().stream()
                .map(tipo -> {
                    CostoFestivalFormDTO existente = costosExistentes.get(tipo);
                    if (existente != null) {
                        existente.setAplicar(true);
                        return existente;
                    }
                    CostoFestivalFormDTO nuevo = new CostoFestivalFormDTO();
                    nuevo.setTipoCosto(tipo);
                    nuevo.setTipoUnidadAplicable(TipoUnidadAplicable.TODAS);
                    return nuevo;
                })
                .toList());
    }

    private void agregarOpcionesFormulario(Model model) {
        model.addAttribute("tiposUnidadAplicable", TipoUnidadAplicable.values());
        model.addAttribute("unidadesDisponibles", unidadVentaService.listarUnidades());
    }
}
