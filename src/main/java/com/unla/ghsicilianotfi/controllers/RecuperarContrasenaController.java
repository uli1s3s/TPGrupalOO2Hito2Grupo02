
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.services.implementation.RecuperarContrasenaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecuperarContrasenaController {

    private final RecuperarContrasenaService recuperacionService;

    public RecuperarContrasenaController(
            RecuperarContrasenaService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    // Mostrar formulario para solicitar recuperación
    @GetMapping("/recuperar-contrasena")
    public String mostrarRecuperacion() {
        return "contrasena/recuperar-contrasena";
    }

    // Procesar solicitud de recuperación

    @PostMapping("/recuperar-contrasena")
    public String solicitarRecuperacion(
            @RequestParam String email,
            Model model) {

        try {
            recuperacionService.solicitarRecuperacion(email);

            model.addAttribute("mensaje",
                    "Si el correo está registrado, recibirás un enlace "
                            + "para restablecer tu contraseña.");

        } catch (IllegalStateException e) {

            model.addAttribute("error", e.getMessage());

        } catch (Exception e) {

            model.addAttribute("error",
                    "No fue posible procesar la solicitud. "
                            + "Intentá nuevamente más tarde.");
        }

        return "contrasena/recuperar-contrasena";
    }


    // Mostrar formulario para establecer contraseña nueva
    @GetMapping("/restablecer-contrasena")
    public String mostrarRestablecimiento(
            @RequestParam(required = false) String token,
            Model model) {

        if (!recuperacionService.tokenValido(token)) {
            model.addAttribute("error",
                    "El enlace de recuperación no es válido o ha vencido.");

            return "contrasena/restablecer-contrasena";
        }

        model.addAttribute("token", token);

        return "contrasena/restablecer-contrasena";
    }

    // Guardar contraseña nueva
    @PostMapping("/restablecer-contrasena")
    public String restablecerContrasena(
            @RequestParam String token,
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmacion,
            Model model) {

        try {
            recuperacionService.restablecerContrasena(
                    token, nuevaContrasena, confirmacion);

            model.addAttribute("mensaje",
                    "Tu contraseña fue restablecida correctamente. "
                            + "Ya podés iniciar sesión.");

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());

            if (recuperacionService.tokenValido(token)) {
                model.addAttribute("token", token);
            }
        }

        return "contrasena/restablecer-contrasena";
    }
}
