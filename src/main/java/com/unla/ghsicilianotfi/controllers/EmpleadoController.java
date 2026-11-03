package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.AltaEmpleadoDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.repositories.IUnidadVentaRepository;
import com.unla.ghsicilianotfi.services.ICategoriaCocineroService;
import com.unla.ghsicilianotfi.services.IEmpleadoService;

import jakarta.validation.Valid;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/empleados")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSABLE')")
public class EmpleadoController {

    private final IEmpleadoService empleadoService;
    private final ICategoriaCocineroService categoriaCocineroService;
    private final IUnidadVentaRepository unidadVentaRepository;

    public EmpleadoController(
            IEmpleadoService empleadoService,
            ICategoriaCocineroService categoriaCocineroService,
            IUnidadVentaRepository unidadVentaRepository) {

        this.empleadoService = empleadoService;
        this.categoriaCocineroService = categoriaCocineroService;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    // ============================================
    // LISTADO DE EMPLEADOS
    // ============================================

    @GetMapping("")
    public ModelAndView index(
            @RequestParam(name = "busqueda", required = false)
            String busqueda,

            @RequestParam(name = "tipo", required = false,
                    defaultValue = "TODOS")
            String tipo,

            Authentication authentication) {

        ModelAndView mAV =
                new ModelAndView(ViewRouteHelper.EMPLEADO_INDEX);

        List<AltaEmpleadoDTO> empleados =
                empleadoService.filtrarActivos(busqueda, tipo);

        if (!esAdministrador(authentication)) {

            Set<Long> idsPermitidos =
                    obtenerIdsEmpleadosAutorizados(authentication);

            empleados = empleados.stream()
                    .filter(emp ->
                            idsPermitidos.contains(emp.getIdPersonal()))
                    .toList();
        }

        mAV.addObject("empleados", empleados);
        mAV.addObject(
                "busqueda",
                busqueda != null ? busqueda : ""
        );
        mAV.addObject("tipoSeleccionado", tipo);

        return mAV;
    }

    // ============================================
    // NUEVO EMPLEADO
    // ============================================

    @GetMapping("/nuevo")
    public ModelAndView nuevo() {

        ModelAndView mAV =
                new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);

        mAV.addObject("empleado", new AltaEmpleadoDTO());
        mAV.addObject(
                "categorias",
                categoriaCocineroService.getAll()
        );
        mAV.addObject("esEdicion", false);

        return mAV;
    }

    // ============================================
    // EDITAR EMPLEADO
    // ============================================

    @GetMapping("/editar/{id}")
    public ModelAndView editar(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        verificarAccesoEmpleado(id, authentication);

        return empleadoService.buscarPorId(id)
                .map(dto -> {

                    ModelAndView mAV =
                            new ModelAndView(
                                    ViewRouteHelper.EMPLEADO_FORM
                            );

                    mAV.addObject("empleado", dto);
                    mAV.addObject(
                            "categorias",
                            categoriaCocineroService.getAll()
                    );
                    mAV.addObject("esEdicion", true);

                    return mAV;
                })
                .orElseGet(() -> {

                    redirectAttributes.addFlashAttribute(
                            "mensajeError",
                            "No se encontró el empleado."
                    );

                    return new ModelAndView(
                            new RedirectView(
                                    ViewRouteHelper.EMPLEADO_ROOT,
                                    true
                            )
                    );
                });
    }

    // ============================================
    // GUARDAR EMPLEADO
    // ============================================

    @PostMapping("/guardar")
    public ModelAndView guardar(
            @Valid @ModelAttribute("empleado")
            AltaEmpleadoDTO dto,

            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        boolean esEdicion = dto.getIdPersonal() != null;

        // Validar antes de modificar datos.
        if (esEdicion) {
            verificarAccesoEmpleado(
                    dto.getIdPersonal(),
                    authentication
            );
        }

        if (bindingResult.hasErrors()) {

            ModelAndView mAV =
                    new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);

            mAV.addObject("empleado", dto);
            mAV.addObject(
                    "categorias",
                    categoriaCocineroService.getAll()
            );
            mAV.addObject("esEdicion", esEdicion);

            return mAV;
        }

        try {

            if (esEdicion) {

                empleadoService.actualizarEmpleado(dto);

                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "Empleado actualizado correctamente."
                );

            } else {

                empleadoService.darDeAltaEmpleado(dto);

                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "¡Empleado dado de alta con éxito! "
                                + "Se ha enviado un correo a "
                                + dto.getEmail()
                                + " con sus credenciales de acceso."
                );
            }

            return new ModelAndView(
                    new RedirectView(
                            ViewRouteHelper.EMPLEADO_ROOT,
                            true
                    )
            );

        } catch (IllegalArgumentException e) {

            ModelAndView mAV =
                    new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);

            mAV.addObject("empleado", dto);
            mAV.addObject(
                    "categorias",
                    categoriaCocineroService.getAll()
            );
            mAV.addObject("esEdicion", esEdicion);
            mAV.addObject("mensajeError", e.getMessage());

            return mAV;
        }
    }

    // ============================================
    // BAJA LÓGICA
    // ============================================

    @PostMapping("/{id}/baja")
    public RedirectView darDeBaja(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        verificarAccesoEmpleado(id, authentication);

        if (empleadoService.darDeBajaLogica(id)) {

            redirectAttributes.addFlashAttribute(
                    "mensajeInfo",
                    "Empleado dado de baja lógicamente."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "No se encontró el empleado para dar de baja."
            );
        }

        return new RedirectView(
                ViewRouteHelper.EMPLEADO_ROOT,
                true
        );
    }

    // ============================================
    // ALTA LÓGICA
    // ============================================

    @PostMapping("/{id}/alta")
    public RedirectView darDeAlta(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        verificarAccesoEmpleado(id, authentication);

        if (empleadoService.darDeAltaLogica(id)) {

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Empleado reactivado con éxito."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "No se encontró el empleado."
            );
        }

        return new RedirectView(
                ViewRouteHelper.EMPLEADO_ROOT,
                true
        );
    }

    // ============================================
    // EMPLEADOS AUTORIZADOS DEL RESPONSABLE
    // ============================================

    private Set<Long> obtenerIdsEmpleadosAutorizados(
            Authentication authentication) {

        List<UnidadVenta> unidades =
                unidadVentaRepository.findByResponsable_Nombre(
                        authentication.getName()
                );

        // El repositorio existente obtiene las unidades
        // con sus empleados para evitar depender del DTO.
        Set<Long> idsUnidades = unidades.stream()
                .map(UnidadVenta::getIdUnidad)
                .collect(Collectors.toSet());

        return unidadVentaRepository
                .buscarUnidadesConEmpleados()
                .stream()
                .filter(unidad ->
                        idsUnidades.contains(unidad.getIdUnidad()))
                .flatMap(unidad -> unidad.getEmpleados().stream())
                .map(empleado -> empleado.getIdPersonal())
                .collect(Collectors.toSet());
    }

    // ============================================
    // CONTROL DE ACCESO A EMPLEADOS
    // ============================================

    private void verificarAccesoEmpleado(
            Long idPersonal,
            Authentication authentication) {

        if (esAdministrador(authentication)) {
            return;
        }

        Set<Long> idsPermitidos =
                obtenerIdsEmpleadosAutorizados(authentication);

        if (!idsPermitidos.contains(idPersonal)) {
            throw new AccessDeniedException(
                    "No tenés permiso para administrar este empleado."
            );
        }
    }

    // ============================================
    // VERIFICAR ROL
    // ============================================

    private boolean esAdministrador(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(autoridad ->
                        autoridad.getAuthority()
                                .equals("ROLE_ADMINISTRADOR"));
    }
}