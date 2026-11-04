
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IFestivalService;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
public class HomeController {

	private final IFestivalService festivalService;

	public HomeController(IFestivalService festivalService) {
		this.festivalService = festivalService;
	}

	// ============================================
	// PÁGINA PRINCIPAL
	// ============================================

	@GetMapping("/index")
	public ModelAndView index() {

		ModelAndView modelAndView =
				new ModelAndView(ViewRouteHelper.INDEX);

		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		boolean usuarioAutenticado =
				authentication != null
						&& authentication.isAuthenticated()
						&& !(authentication instanceof AnonymousAuthenticationToken);

		if (usuarioAutenticado) {

			modelAndView.addObject(
					"username",
					authentication.getName()
			);

			String rol = authentication.getAuthorities()
					.stream()
					.map(authority -> authority.getAuthority())
					.findFirst()
					.orElse("SIN_ROL");

			modelAndView.addObject("rol", rol);

		} else {

			// VISITANTE SIN INICIAR SESIÓN
			modelAndView.addObject("username", null);
			modelAndView.addObject("rol", null);
		}

		// FESTIVALES ACTIVOS DE EPICENTRO GOURMET
		modelAndView.addObject(
				"festivales",
				festivalService.listarActivos()
		);

		return modelAndView;
	}

	// ============================================
	// REDIRECCIÓN A LA PÁGINA PRINCIPAL
	// ============================================

	@GetMapping("/")
	public RedirectView redirectToHomeIndex() {

		return new RedirectView(ViewRouteHelper.ROUTE);
	}
}
