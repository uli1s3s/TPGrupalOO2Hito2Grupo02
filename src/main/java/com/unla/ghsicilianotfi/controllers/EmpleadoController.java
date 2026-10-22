package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.AltaEmpleadoDTO;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.ICategoriaCocineroService;
import com.unla.ghsicilianotfi.services.IEmpleadoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    private final IEmpleadoService empleadoService;
    private final ICategoriaCocineroService categoriaCocineroService;

    public EmpleadoController(IEmpleadoService empleadoService,
                              ICategoriaCocineroService categoriaCocineroService) {
        this.empleadoService = empleadoService;
        this.categoriaCocineroService = categoriaCocineroService;
    }

    @GetMapping("")
    public ModelAndView index(
            @RequestParam(name = "busqueda", required = false) String busqueda,
            @RequestParam(name = "tipo", required = false, defaultValue = "TODOS") String tipo) {

        ModelAndView mAV = new ModelAndView(ViewRouteHelper.EMPLEADO_INDEX);
        mAV.addObject("empleados", empleadoService.filtrarActivos(busqueda, tipo));
        mAV.addObject("busqueda", busqueda != null ? busqueda : "");
        mAV.addObject("tipoSeleccionado", tipo);
        return mAV;
    }

    @GetMapping("/nuevo")
    public ModelAndView nuevo() {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);
        mAV.addObject("empleado", new AltaEmpleadoDTO());
        mAV.addObject("categorias", categoriaCocineroService.getAll());
        mAV.addObject("esEdicion", false);
        return mAV;
    }

    @GetMapping("/editar/{id}")
    public ModelAndView editar(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        return empleadoService.buscarPorId(id)
                .map(dto -> {
                    ModelAndView mAV = new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);
                    mAV.addObject("empleado", dto);
                    mAV.addObject("categorias", categoriaCocineroService.getAll());
                    mAV.addObject("esEdicion", true);
                    return mAV;
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el empleado.");
                    return new ModelAndView(new RedirectView(ViewRouteHelper.EMPLEADO_ROOT, true));
                });
    }

    @PostMapping("/guardar")
    public ModelAndView guardar(
            @Valid @ModelAttribute("empleado") AltaEmpleadoDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        boolean esEdicion = dto.getIdPersonal() != null;

        if (bindingResult.hasErrors()) {
            ModelAndView mAV = new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);
            mAV.addObject("empleado", dto);
            mAV.addObject("categorias", categoriaCocineroService.getAll());
            mAV.addObject("esEdicion", esEdicion);
            return mAV;
        }

        try {
            if (esEdicion) {
                empleadoService.actualizarEmpleado(dto);
                redirectAttributes.addFlashAttribute("mensajeExito", "Empleado actualizado correctamente.");
            } else {
                empleadoService.darDeAltaEmpleado(dto);
                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "¡Empleado dado de alta con éxito! Se ha enviado un correo a " + dto.getEmail() + " con sus credenciales de acceso."
                );
            }
            return new ModelAndView(new RedirectView(ViewRouteHelper.EMPLEADO_ROOT, true));
        } catch (IllegalArgumentException e) {
            ModelAndView mAV = new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);
            mAV.addObject("empleado", dto);
            mAV.addObject("categorias", categoriaCocineroService.getAll());
            mAV.addObject("esEdicion", esEdicion);
            mAV.addObject("mensajeError", e.getMessage());
            return mAV;
        }
    }

    @PostMapping("/{id}/baja")
    public RedirectView darDeBaja(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        if (empleadoService.darDeBajaLogica(id)) {
            redirectAttributes.addFlashAttribute("mensajeInfo", "Empleado dado de baja lógicamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el empleado para dar de baja.");
        }

        return new RedirectView(ViewRouteHelper.EMPLEADO_ROOT, true);
    }

    @PostMapping("/{id}/alta")
    public RedirectView darDeAlta(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        if (empleadoService.darDeAltaLogica(id)) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Empleado reactivado con éxito.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el empleado.");
        }

        return new RedirectView(ViewRouteHelper.EMPLEADO_ROOT, true);
    }
}

