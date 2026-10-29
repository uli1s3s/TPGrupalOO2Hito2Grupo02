package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.LiquidacionDeHaberDTO;
import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.ILiquidacionService;
import com.unla.ghsicilianotfi.services.IReciboSueldoPDFService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/recibos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSABLE', 'EMPLEADO')")
public class ReciboSueldoController {

    private final ILiquidacionService liquidacionService;
    private final IReciboSueldoPDFService reciboSueldoPDFService;

    public ReciboSueldoController(ILiquidacionService liquidacionService,
                                  IReciboSueldoPDFService reciboSueldoPDFService) {
        this.liquidacionService = liquidacionService;
        this.reciboSueldoPDFService = reciboSueldoPDFService;
    }

    @GetMapping
    public String verRecibos(Model model, Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        boolean esEmpleado = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));

        List<LiquidacionDeHaberDTO> recibos;
        if (esEmpleado) {
            recibos = liquidacionService.listarRecibosPorEmpleado(authentication.getName());
        } else if (!esAdmin) {
            // Es Responsable: solo ve los recibos de los empleados de sus unidades a cargo
            recibos = liquidacionService.listarRecibosPorResponsable(authentication.getName());
        } else {
            // Es Administrador: ve todos los recibos
            recibos = liquidacionService.listarTodosLosRecibos();
        }

        model.addAttribute("recibos", recibos);
        model.addAttribute("esEmpleado", esEmpleado);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rol", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).findFirst().orElse("SIN_ROL"));

        return ViewRouteHelper.RECIBO_LISTA;
    }

    @GetMapping("/{id}/descargar-pdf")
    public ResponseEntity<byte[]> descargarReciboPDF(@PathVariable("id") Long id,
                                                     Authentication authentication) {
        LiquidacionDeHaber liquidacion = liquidacionService.buscarPorId(id);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        boolean esEmpleado = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));

        if (esEmpleado) {
            String usernameCredenciales = (liquidacion.getPersonal().getCredenciales() != null)
                    ? liquidacion.getPersonal().getCredenciales().getNombre()
                    : null;

            if (usernameCredenciales == null || !usernameCredenciales.equals(authentication.getName())) {
                throw new AccessDeniedException("No tiene permisos para ver este recibo de sueldo");
            }
        } else if (!esAdmin) {
            // Es Responsable: solo puede descargar recibos de su unidad
            String respNombre = (liquidacion.getUnidadVenta() != null && liquidacion.getUnidadVenta().getResponsable() != null)
                    ? liquidacion.getUnidadVenta().getResponsable().getNombre()
                    : null;

            if (respNombre == null || !respNombre.equals(authentication.getName())) {
                throw new AccessDeniedException("No tiene permisos para descargar recibos de otras unidades");
            }
        }

        byte[] pdfBytes = reciboSueldoPDFService.generarReciboPDF(liquidacion);

        String filename = "recibo-sueldo-" + liquidacion.getPeriodo().replaceAll("\\s+", "-")
                + "-" + liquidacion.getPersonal().getDni() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}

