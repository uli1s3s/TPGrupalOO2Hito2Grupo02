package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.LiquidacionDTO;
import com.unla.ghsicilianotfi.dtos.ResultadoLiquidacionDTO;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.repositories.IFestivalRepository;
import com.unla.ghsicilianotfi.repositories.ILiquidacionDeHaberRepository;
import com.unla.ghsicilianotfi.services.ILiquidacionService;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/liquidaciones")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSABLE')")
public class LiquidacionController {

    private final IUnidadVentaService unidadVentaService;
    private final ILiquidacionService liquidacionService;
    private final IFestivalRepository festivalRepository;
    private final ILiquidacionDeHaberRepository liquidacionDeHaberRepository;

    public LiquidacionController(IUnidadVentaService unidadVentaService,
                                 ILiquidacionService liquidacionService,
                                 IFestivalRepository festivalRepository,
                                 ILiquidacionDeHaberRepository liquidacionDeHaberRepository) {
        this.unidadVentaService = unidadVentaService;
        this.liquidacionService = liquidacionService;
        this.festivalRepository = festivalRepository;
        this.liquidacionDeHaberRepository = liquidacionDeHaberRepository;
    }

    @GetMapping
    public String verLiquidaciones(@RequestParam(name = "idFestival", required = false) Long idFestival,
                                   @RequestParam(name = "periodo", required = false, defaultValue = "TODOS") String periodo,
                                   Model model,
                                   Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        List<UnidadVenta> unidades = esAdmin
                ? unidadVentaService.listarUnidades()
                : unidadVentaService.listarPorResponsable(authentication.getName());

        List<Festival> festivales = festivalRepository.findByActivoTrueOrderByFechaInicioDesc();

        Festival festivalSeleccionado = null;
        if (idFestival != null) {
            festivalSeleccionado = festivalRepository.findById(idFestival).orElse(null);
        } else if (!festivales.isEmpty()) {
            festivalSeleccionado = festivales.get(0);
        }

        List<LiquidacionDTO> liquidaciones = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;
        String advertenciaFestival = null;
        boolean sePuedeLiquidar = true;
        int totalPendientes = 0;

        List<YearMonth> periodosFestival = (festivalSeleccionado != null)
                ? liquidacionService.obtenerPeriodosFestival(festivalSeleccionado)
                : Collections.emptyList();

        List<YearMonth> periodosAMostrar = new ArrayList<>(periodosFestival);
        if (periodo != null && !periodo.trim().isEmpty() && !periodo.equalsIgnoreCase("TODOS")) {
            try {
                YearMonth ymFiltro = YearMonth.parse(periodo.trim());
                if (periodosFestival.contains(ymFiltro)) {
                    periodosAMostrar = List.of(ymFiltro);
                }
            } catch (Exception ignored) {
            }
        }

        if (festivalSeleccionado != null) {
            int duracionTotal = festivalSeleccionado.getDuracionDias();

            if (duracionTotal < 15) {
                advertenciaFestival = "El festival '" + festivalSeleccionado.getNombre()
                        + "' tiene una duración de solo " + duracionTotal + " días. "
                        + "La regla de negocio prohíbe liquidar festivales con menos de 15 días computables.";
                sePuedeLiquidar = false;
            }

            for (YearMonth ym : periodosAMostrar) {
                String strPeriodo = ym.toString();
                for (UnidadVenta unidad : unidades) {
                    if (unidad.getEmpleados() != null) {
                        for (Personal empleado : unidad.getEmpleados()) {
                            if (empleado.isActivo()) {
                                // Si el empleado ingresó después de que finalizó el festival, nunca perteneció a él
                                if (empleado.getFechaIngreso() != null
                                        && empleado.getFechaIngreso().isAfter(festivalSeleccionado.getFechaFin())) {
                                    continue;
                                }

                                int diasComputados = liquidacionService.calcularDiasTrabajadosEnPeriodo(
                                        empleado, festivalSeleccionado, ym, LocalDate.now());

                                // Si para este período específico no computó días, no mostrar en la lista
                                if (diasComputados == 0) {
                                    continue;
                                }

                                int diasCalculo = Math.min(diasComputados, 30);
                                LiquidacionDTO l = liquidacionService.calcular(empleado, unidad, diasCalculo);
                                l.setDiasFestival(diasComputados);
                                l.setPeriodo(strPeriodo);
                                boolean yaLiquidado = liquidacionDeHaberRepository.existsByPersonalAndFestivalAndPeriodo(
                                        empleado, festivalSeleccionado, strPeriodo);
                                l.setYaLiquidado(yaLiquidado);
                                liquidaciones.add(l);

                                if (diasComputados >= 15) {
                                    totalGeneral = totalGeneral.add(l.getTotalACobrar());
                                    if (!yaLiquidado) {
                                        totalPendientes++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (duracionTotal >= 15 && totalPendientes == 0 && !liquidaciones.isEmpty()) {
                sePuedeLiquidar = false;
            }
        }

        model.addAttribute("festivales", festivales);
        model.addAttribute("festivalSeleccionado", festivalSeleccionado);
        model.addAttribute("periodosFestival", periodosFestival);
        model.addAttribute("liquidaciones", liquidaciones);
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("periodo", periodo);
        model.addAttribute("advertenciaFestival", advertenciaFestival);
        model.addAttribute("sePuedeLiquidar", sePuedeLiquidar);
        model.addAttribute("totalPendientes", totalPendientes);

        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).findFirst().orElse("SIN_ROL"));

        return ViewRouteHelper.LIQUIDACION_LISTA;
    }

    @PostMapping("/generar")
    public String generarLiquidaciones(@RequestParam("idFestival") Long idFestival,
                                       @RequestParam(name = "periodo", defaultValue = "TODOS") String periodo,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {

        Festival festival = festivalRepository.findById(idFestival)
                .orElseThrow(() -> new IllegalArgumentException("No existe el festival indicado"));

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        List<UnidadVenta> unidades = esAdmin
                ? unidadVentaService.listarUnidades()
                : unidadVentaService.listarPorResponsable(authentication.getName());

        try {
            ResultadoLiquidacionDTO res = liquidacionService.procesarLiquidacionFestival(
                    festival, unidades, periodo.trim(), LocalDate.now());

            if (res.getCantidadGeneradas() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("¡Se generaron con éxito ").append(res.getCantidadGeneradas())
                  .append(" liquidaciones mensuales para el festival '").append(festival.getNombre()).append("'!");

                if (res.getCantidadDiasInsuficientes() > 0) {
                    sb.append(" (").append(res.getCantidadDiasInsuficientes())
                      .append(" empleado(s) con menos de 15 días trabajados no calificaron)");
                }
                if (res.getCantidadYaLiquidados() > 0) {
                    sb.append(" (").append(res.getCantidadYaLiquidados())
                      .append(" período(s) ya contaban con recibo registrado)");
                }
                redirectAttributes.addFlashAttribute("success", sb.toString());
            } else {
                if (res.getCantidadYaLiquidados() > 0 && res.getCantidadDiasInsuficientes() > 0) {
                    redirectAttributes.addFlashAttribute("warning",
                            "No se registraron nuevas liquidaciones: "
                                    + res.getCantidadYaLiquidados() + " período(s) ya tenían recibo registrado y "
                                    + res.getCantidadDiasInsuficientes() + " no alcanzan el mínimo de 15 días trabajados.");
                } else if (res.getCantidadYaLiquidados() > 0) {
                    redirectAttributes.addFlashAttribute("warning",
                            "Todos los períodos evaluados ya tienen su recibo registrado para este festival.");
                } else {
                    redirectAttributes.addFlashAttribute("warning",
                            "No se registraron liquidaciones: ninguno de los empleados cumple con el mínimo de 15 días trabajados requerido.");
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/liquidaciones?idFestival=" + idFestival + "&periodo=" + periodo;
    }
}
