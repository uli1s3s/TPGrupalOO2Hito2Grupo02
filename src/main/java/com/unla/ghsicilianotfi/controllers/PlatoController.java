package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.PlatoFormDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IPlatoService;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/platos")
@PreAuthorize("hasRole('RESPONSABLE')")
public class PlatoController {

    private final IPlatoService platoService;
    private final IUnidadVentaService unidadVentaService;

    public PlatoController(IPlatoService platoService,
                           IUnidadVentaService unidadVentaService) {
        this.platoService = platoService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping
    public String listar(Model model, Authentication authentication) {
        model.addAttribute("platos", platoService.listarPorResponsable(authentication.getName()));
        return ViewRouteHelper.PLATO_INDEX;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model, Authentication authentication) {
        PlatoFormDTO plato = new PlatoFormDTO();
        List<UnidadVenta> unidades = cargarUnidades(model, authentication.getName());
        if (unidades.size() == 1) {
            plato.setIdUnidad(unidades.get(0).getIdUnidad());
        }
        model.addAttribute("plato", plato);
        model.addAttribute("esEdicion", false);
        return ViewRouteHelper.PLATO_FORM;
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        Optional<PlatoFormDTO> encontrado = platoService.buscarPorId(id, authentication.getName());
        if (encontrado.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el plato en tus unidades.");
            return "redirect:" + ViewRouteHelper.PLATO_ROOT;
        }
        cargarUnidades(model, authentication.getName());
        model.addAttribute("plato", encontrado.get());
        model.addAttribute("esEdicion", true);
        return ViewRouteHelper.PLATO_FORM;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("plato") PlatoFormDTO plato,
                          BindingResult bindingResult,
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        boolean esEdicion = plato.getIdPlato() != null;
        if (bindingResult.hasErrors()) {
            cargarUnidades(model, authentication.getName());
            model.addAttribute("esEdicion", esEdicion);
            return ViewRouteHelper.PLATO_FORM;
        }
        try {
            platoService.guardar(plato, authentication.getName());
            redirectAttributes.addFlashAttribute("mensajeExito",
                    esEdicion ? "Plato actualizado correctamente." : "Plato dado de alta correctamente.");
            return "redirect:" + ViewRouteHelper.PLATO_ROOT;
        } catch (IllegalArgumentException e) {
            cargarUnidades(model, authentication.getName());
            model.addAttribute("esEdicion", esEdicion);
            model.addAttribute("mensajeError", e.getMessage());
            return ViewRouteHelper.PLATO_FORM;
        }
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        actualizarEstado(id, authentication, false, redirectAttributes);
        return "redirect:" + ViewRouteHelper.PLATO_ROOT;
    }

    @PostMapping("/{id}/alta")
    public String darDeAlta(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        actualizarEstado(id, authentication, true, redirectAttributes);
        return "redirect:" + ViewRouteHelper.PLATO_ROOT;
    }

    private List<UnidadVenta> cargarUnidades(Model model, String nombreResponsable) {
        List<UnidadVenta> unidades = unidadVentaService.listarPorResponsable(nombreResponsable);
        model.addAttribute("unidades", unidades);
        return unidades;
    }

    private void actualizarEstado(Long id,
                                  Authentication authentication,
                                  boolean activo,
                                  RedirectAttributes redirectAttributes) {
        boolean actualizado = platoService.cambiarEstado(id, authentication.getName(), activo);
        if (!actualizado) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el plato en tus unidades.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeExito",
                    activo ? "Plato reactivado correctamente." : "Plato dado de baja correctamente.");
        }
    }
}
