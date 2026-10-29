
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.AsignacionActualDTO;
import com.unla.ghsicilianotfi.dtos.HistorialLaboralDTO;
import com.unla.ghsicilianotfi.entities.Cocinero;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.repositories.ILiquidacionDeHaberRepository;
import com.unla.ghsicilianotfi.repositories.IUnidadVentaRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasRole('EMPLEADO')")
public class AsignacionActualController {

    private final IUnidadVentaRepository unidadRepository;
    private final ILiquidacionDeHaberRepository liquidacionRepository;

    public AsignacionActualController(
            IUnidadVentaRepository unidadRepository,
            ILiquidacionDeHaberRepository liquidacionRepository) {

        this.unidadRepository = unidadRepository;
        this.liquidacionRepository = liquidacionRepository;
    }

    @GetMapping("/mi-asignacion")
    @Transactional(readOnly = true)
    public String verAsignacion(Authentication authentication, Model model) {

        String username = authentication.getName();

        // 1. HISTORIAL LABORAL
        // Se obtiene de las liquidaciones registradas.
        // Se consulta primero para mostrarlo incluso si el empleado
        // actualmente no tiene unidad o festival asignado.

        List<HistorialLaboralDTO> historial = liquidacionRepository
                .findByPersonal_Credenciales_NombreOrderByFechaLiquidacionDesc(username)
                .stream()
                .filter(l -> l.getFestival() != null
                        && l.getUnidadVenta() != null)
                .map(l -> new HistorialLaboralDTO(
                        l.getFestival().getNombre(),
                        l.getUnidadVenta().getNombreComercial(),
                        l.getPeriodo()
                ))
                .toList();

        model.addAttribute("historial", historial);

        // 2. ASIGNACION ACTUAL
        // Buscar la unidad correspondiente al empleado autenticado.

        Optional<UnidadVenta> unidadOpt =
                unidadRepository.buscarUnidadDeEmpleado(username);

        if (unidadOpt.isEmpty()) {
            model.addAttribute("mensaje",
                    "Actualmente no tenés una unidad de venta asignada.");
            return "empleados/mi-asignacion";
        }

        UnidadVenta unidad = unidadOpt.get();

        // Identificar al empleado dentro de la unidad.

        Personal empleado = unidad.getEmpleados().stream()
                .filter(p -> p.isActivo()
                        && p.getCredenciales() != null
                        && username.equals(p.getCredenciales().getNombre()))
                .findFirst()
                .orElse(null);

        if (empleado == null) {
            model.addAttribute("mensaje",
                    "No se encontró una asignación activa.");
            return "empleados/mi-asignacion";
        }

        // 3. BUSCAR FESTIVALES VIGENTES
        // Deben estar activos y dentro de sus fechas.

        LocalDate hoy = LocalDate.now();

        List<Festival> festivalesVigentes = unidad.getFestivales().stream()
                .filter(Festival::isActivo)
                .filter(f -> f.getFechaInicio() != null
                        && f.getFechaFin() != null)
                .filter(f -> !hoy.isBefore(f.getFechaInicio()))
                .filter(f -> !hoy.isAfter(f.getFechaFin()))
                .sorted(Comparator.comparing(Festival::getFechaInicio))
                .toList();

        if (festivalesVigentes.isEmpty()) {
            model.addAttribute("mensaje",
                    "Tu unidad no participa actualmente en un festival vigente.");
            return "empleados/mi-asignacion";
        }

        // 4. DETERMINAR FUNCION DEL EMPLEADO

        String funcion = empleado instanceof Cocinero
                ? "Cocinero"
                : "Cajero";

        // 5. CONSTRUIR EL DTO DE ASIGNACION ACTUAL

        List<AsignacionActualDTO> asignaciones = festivalesVigentes.stream()
                .map(f -> new AsignacionActualDTO(
                        empleado.getNombre() + " " + empleado.getApellido(),
                        funcion,
                        unidad.getNombreComercial(),
                        f.getNombre(),
                        f.getFechaInicio(),
                        f.getFechaFin()
                ))
                .toList();

        model.addAttribute("asignaciones", asignaciones);

        return "empleados/mi-asignacion";
    }
}
