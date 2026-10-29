
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.CambioContrasenaDTO;
import com.unla.ghsicilianotfi.services.IContrasenaService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContrasenaController {

    private final IContrasenaService contrasenaService;

    public ContrasenaController(IContrasenaService contrasenaService) {
        this.contrasenaService = contrasenaService;
    }

    @GetMapping("/cambiar-contrasena")
    public String mostrarFormulario(Model model) {

        model.addAttribute(
                "cambioContrasena",
                new CambioContrasenaDTO()
        );

        return "user/cambiar-contrasena";
    }

    @PostMapping("/cambiar-contrasena")
    public String cambiarContrasena(
            @Valid @ModelAttribute("cambioContrasena")
            CambioContrasenaDTO datos,
            BindingResult resultado,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return "user/cambiar-contrasena";
        }

        try {

            contrasenaService.cambiarContrasena(
                    authentication.getName(),
                    datos
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Contraseña actualizada correctamente"
            );

            return "redirect:/cambiar-contrasena";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            return "user/cambiar-contrasena";
        }
    }
}
