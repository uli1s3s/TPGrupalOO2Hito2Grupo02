package com.unla.ghsicilianotfi.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import org.springframework.security.core.Authentication;

@Controller
public class UserController {

	@GetMapping("/login")
	public String login(Model model,
						@RequestParam(required=false) String error,
						@RequestParam(required=false) String logout) {
		model.addAttribute("error", error);
		model.addAttribute("logout", logout);
		return ViewRouteHelper.USER_LOGIN;
	}

	@GetMapping("/logout")
	public String logout(Model model) {
		return ViewRouteHelper.USER_LOGOUT;
	}

	// Después de iniciar sesion: el responsable va directo a su unidad, el resto al inicio
	@GetMapping("/loginsuccess")
	public String loginCheck(Authentication authentication) {

		boolean esResponsable = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));

		if (esResponsable) {
			return "redirect:/mi-unidad";
		}
		return "redirect:/index";
	}
}
