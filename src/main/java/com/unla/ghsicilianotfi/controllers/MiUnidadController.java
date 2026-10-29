package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.entities.Cocinero;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.unla.ghsicilianotfi.dtos.EstadoEmpleadoDTO;

/**
 * "Mi unidad": el responsable ve su unidad de venta, el canon que paga en cada festival
 * y administra sus empleados (asignar disponibles / quitar).
 */
@Controller
@RequestMapping("/mi-unidad")
@PreAuthorize("hasRole('RESPONSABLE')")
public class MiUnidadController {

    private final IUnidadVentaService unidadVentaService;

    public MiUnidadController(IUnidadVentaService unidadVentaService) {
        this.unidadVentaService = unidadVentaService;
    }

    // ==========================================
    // Ver mis unidades, sus canones, sus empleados y todo el personal del predio
    // ==========================================
    @GetMapping
    public String verMiUnidad(@RequestParam(value = "busqueda", required = false) String busqueda,
                              Model model, Authentication authentication) {

        List<UnidadVenta> unidades = unidadVentaService.listarPorResponsable(authentication.getName());

        // Todo el personal con su estado (libre, ocupado o dado de baja), filtrado por la busqueda
        List<EstadoEmpleadoDTO> personal = unidadVentaService.listarPersonalConEstado(busqueda);

        // Canon de cada unidad en cada uno de sus festivales
        Map<Long, Map<String, BigDecimal>> canones = new HashMap<>();
        for (UnidadVenta unidad : unidades) {
            canones.put(unidad.getIdUnidad(), unidadVentaService.calcularCanonPorFestival(unidad.getIdUnidad()));
        }

        // Tipo de cada empleado de mis unidades (Cocinero o Cajero) para mostrarlo en la vista
        Map<Long, String> tipos = new HashMap<>();
        for (UnidadVenta unidad : unidades) {
            for (Personal empleado : unidad.getEmpleados()) {
                tipos.put(empleado.getIdPersonal(), tipoDe(empleado));
            }
        }

        model.addAttribute("unidades", unidades);
        model.addAttribute("personal", personal);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("canones", canones);
        model.addAttribute("tipos", tipos);

        // Datos para la barra de navegacion
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).findFirst().orElse("SIN_ROL"));

        return ViewRouteHelper.MI_UNIDAD;
    }
    // ==========================================
    // Asignar un empleado disponible a mi unidad
    // ==========================================
    @PostMapping("/{idUnidad}/asignar")
    public String asignarEmpleado(@PathVariable("idUnidad") Long idUnidad,
                                  @RequestParam("idPersonal") Long idPersonal,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            unidadVentaService.asignarEmpleado(idUnidad, idPersonal, authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Empleado asignado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + ViewRouteHelper.MI_UNIDAD_ROOT;
    }

    // ==========================================
    // Quitar un empleado de mi unidad
    // ==========================================
    @PostMapping("/{idUnidad}/quitar/{idPersonal}")
    public String quitarEmpleado(@PathVariable("idUnidad") Long idUnidad,
                                 @PathVariable("idPersonal") Long idPersonal,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            unidadVentaService.quitarEmpleado(idUnidad, idPersonal, authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Empleado quitado de la unidad. Vuelve a estar disponible.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + ViewRouteHelper.MI_UNIDAD_ROOT;
    }
    
    // ==========================================
    // Reactivar un empleado dado de baja y asignarlo a mi unidad
    // ==========================================
    @PostMapping("/{idUnidad}/reactivar")
    public String reactivarEmpleado(@PathVariable("idUnidad") Long idUnidad,
                                    @RequestParam("idPersonal") Long idPersonal,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            unidadVentaService.reactivarYAsignarEmpleado(idUnidad, idPersonal, authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje",
                    "Empleado reactivado y asignado. Le enviamos un mail con su nueva contraseña.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + ViewRouteHelper.MI_UNIDAD_ROOT;
    }

    private String tipoDe(Personal empleado) {
        return (empleado instanceof Cocinero) ? "Cocinero" : "Cajero";
    }
    
    // ==========================================
    // Ver la rentabilidad neta de mi unidad en un festival
    // ==========================================
    @GetMapping("/{idUnidad}/rentabilidad")
    public String verRentabilidad(@PathVariable("idUnidad") Long idUnidad,
                                  @RequestParam(name = "festival", required = false) Long idFestival,
                                  Model model,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("rentabilidad",
                    unidadVentaService.calcularRentabilidadNeta(idUnidad, idFestival, authentication.getName()));
        } catch (IllegalArgumentException e) {
            // Unidad ajena, festival en el que no participa, etc
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + ViewRouteHelper.MI_UNIDAD_ROOT;
        }

        // Datos para la barra de navegacion
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).findFirst().orElse("SIN_ROL"));

        return ViewRouteHelper.RENTABILIDAD_NETA;
    }
}