package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.FestivalFormDTO;
import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IFestivalService;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;
import com.unla.ghsicilianotfi.services.IVerificacionEmailService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Registro publico: una persona sin usuario se registra como responsable
 * y da de alta su unidad de venta en un festival activo, en el mismo paso.
 * Despues tiene que verificar su email con el codigo que se le envía.
 * Si ya hay una sesion iniciada (por ejemplo un administrador), no se permite.
 */
@Controller
@RequestMapping("/registro-responsable")
public class RegistroResponsableController {

    private final IUnidadVentaService unidadVentaService;
    private final IFestivalService festivalService;
    private final IVerificacionEmailService verificacionEmailService;

    public RegistroResponsableController(IUnidadVentaService unidadVentaService,
                                         IFestivalService festivalService,
                                         IVerificacionEmailService verificacionEmailService) {
        this.unidadVentaService = unidadVentaService;
        this.festivalService = festivalService;
        this.verificacionEmailService = verificacionEmailService;
    }

    // ==========================================
    // Mostrar el formulario de registro
    // Si se llega desde un festival (?festival=ID), queda preseleccionado
    // ==========================================
    @GetMapping
    public String mostrarFormulario(@RequestParam(name = "festival", required = false) Long idFestival,
                                    Model model,
                                    Authentication authentication) {

        RegistroUnidadVentaDTO registro = new RegistroUnidadVentaDTO();
        registro.setIdFestival(idFestival);
        model.addAttribute("registro", registro);
        cargarFestivales(model);

        // authentication es null cuando nadie inicio sesión
        if (authentication != null) {
            model.addAttribute("bloqueado", true);
        }
        return ViewRouteHelper.UNIDAD_NUEVA;
    }

    // ==========================================
    // Procesar el registro del responsable y su unidad
    // ==========================================
    @PostMapping
    public String registrar(@Valid @ModelAttribute("registro") RegistroUnidadVentaDTO registro,
                            BindingResult resultado,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        // Un usuario logueado (admin, responsable o empleado) no puede registrarse de nuevo
        if (authentication != null) {
            model.addAttribute("bloqueado", true);
            cargarFestivales(model);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }

        // Errores de formato (campos vacios, email invalido, festival sin elegir, etc.)
        if (resultado.hasErrors()) {
            cargarFestivales(model);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }

        String email = registro.getEmailResponsable().trim().toLowerCase(Locale.ROOT);

        try {
            unidadVentaService.registrarUnidad(registro);

            // Pasa a la pantalla para ingresar el codigo que se envió por mail
            redirectAttributes.addAttribute("email", email);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Te enviamos un código de verificación a " + email + ".");
            return "redirect:" + ViewRouteHelper.REGISTRO_RESPONSABLE_ROOT + "/verificar";

        } catch (IllegalArgumentException e) {
            // Errores de negocio: festival inactivo, email ya registrado, etc.
            model.addAttribute("error", e.getMessage());

            // Si el email ya se registro pero nunca se verifico, se ofrece verificarlo
            if (verificacionEmailService.estaPendienteDeVerificacion(email)) {
                model.addAttribute("emailPendiente", email);
            }

            cargarFestivales(model);
            return ViewRouteHelper.UNIDAD_NUEVA;

        } catch (DataIntegrityViolationException e) {
            // El formulario se envio dos veces al mismo tiempo (doble clic):
            // el primer envío ya registro la cuenta, este choca con el email repetido
            model.addAttribute("error", "Ese email ya está registrado.");

            if (verificacionEmailService.estaPendienteDeVerificacion(email)) {
                model.addAttribute("emailPendiente", email);
            }

            cargarFestivales(model);
            return ViewRouteHelper.UNIDAD_NUEVA;
        }
    }
    // ==========================================
    // Pantalla para ingresar el codigo de verificación
    // ==========================================
    @GetMapping("/verificar")
    public String mostrarVerificacion(@RequestParam(name = "email", required = false) String email,
                                      @RequestParam(name = "pendiente", required = false) Boolean pendiente,
                                      Model model) {
        model.addAttribute("email", email);

        // Llego desde el login con la cuenta sin verificar
        if (Boolean.TRUE.equals(pendiente)) {
            model.addAttribute("mensaje",
                    "Tu cuenta todavía no está verificada. Ingresá el código que te enviamos por mail o pedí uno nuevo.");
        }

        return ViewRouteHelper.REGISTRO_RESPONSABLE_VERIFICAR;
    }

    // ==========================================
    // Verificar el codigo ingresado
    // ==========================================
    @PostMapping("/verificar")
    public String verificar(@RequestParam("email") String email,
                            @RequestParam("codigo") String codigo,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            verificacionEmailService.verificarCodigo(email, codigo);

            // Datos para la pantalla de exito: email y codigo de la unidad registrada
            String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
            redirectAttributes.addFlashAttribute("emailRegistrado", emailNormalizado);

            List<UnidadVenta> unidades = unidadVentaService.listarPorResponsable(emailNormalizado);
            if (!unidades.isEmpty()) {
                redirectAttributes.addFlashAttribute("codigoGenerado", unidades.get(0).getCodigoUnico());
            }

            return "redirect:" + ViewRouteHelper.REGISTRO_RESPONSABLE_ROOT + "/exito";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return ViewRouteHelper.REGISTRO_RESPONSABLE_VERIFICAR;
        }
    }

    // ==========================================
    // Reenviar un codigo nuevo
    // ==========================================
    @PostMapping("/reenviar")
    public String reenviar(@RequestParam("email") String email,
                           RedirectAttributes redirectAttributes) {
        try {
            verificacionEmailService.reenviarCodigo(email);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Te enviamos un código nuevo. El anterior ya no es válido.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addAttribute("email", email);
        return "redirect:" + ViewRouteHelper.REGISTRO_RESPONSABLE_ROOT + "/verificar";
    }

    // ==========================================
    // Pantalla de registro exitoso (despues de verificar)
    // ==========================================
    @GetMapping("/exito")
    public String exito() {
        return ViewRouteHelper.REGISTRO_RESPONSABLE_EXITO;
    }

    // Solo se ofrecen festivales activos que todavia no terminaron
    private void cargarFestivales(Model model) {
        List<FestivalFormDTO> festivales = festivalService.listarActivos()
                .stream()
                .filter(festival -> !festival.getFechaFin().isBefore(LocalDate.now()))
                .toList();
        model.addAttribute("festivales", festivales);
    }
}