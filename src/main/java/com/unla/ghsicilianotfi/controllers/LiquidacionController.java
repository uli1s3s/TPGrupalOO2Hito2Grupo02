package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.LiquidacionDTO;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.ILiquidacionService;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/liquidaciones")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSABLE')")
public class LiquidacionController {

    private final IUnidadVentaService unidadVentaService;
    private final ILiquidacionService liquidacionService;

    public LiquidacionController(IUnidadVentaService unidadVentaService,
                                 ILiquidacionService liquidacionService) {
        this.unidadVentaService = unidadVentaService;
        this.liquidacionService = liquidacionService;
    }

    // ==========================================
    // Liquidación de los empleados según los días del festival
    // ==========================================
    @GetMapping
    public String verLiquidaciones(@RequestParam(name = "dias", defaultValue = "15") int dias,
                                   Model model,
                                   Authentication authentication) {

        // El admin ve todas las unidades; el responsable solo las suyas
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        List<UnidadVenta> unidades = esAdmin
                ? unidadVentaService.listarUnidades()
                : unidadVentaService.listarPorResponsable(authentication.getName());

        List<LiquidacionDTO> liquidaciones = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        try {
            for (UnidadVenta unidad : unidades) {
                for (Personal empleado : unidad.getEmpleados()) {
                    if (empleado.isActivo()) {
                        LiquidacionDTO liquidacion = liquidacionService.calcular(empleado, unidad, dias);
                        liquidaciones.add(liquidacion);
                        totalGeneral = totalGeneral.add(liquidacion.getTotalACobrar());
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("liquidaciones", liquidaciones);
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("dias", dias);
        model.addAttribute("porcentaje",
                liquidacionService.calcularPorcentaje(Math.max(dias, 1))
                        .multiply(BigDecimal.valueOf(100)).intValue());

        // Datos para la barra de navegacion
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).findFirst().orElse("SIN_ROL"));

        return ViewRouteHelper.LIQUIDACION_LISTA;
    }
}