
package com.unla.ghsicilianotfi.controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.unla.ghsicilianotfi.helpers.ViewRouteHelper;
import com.unla.ghsicilianotfi.services.IPersonService;

@Controller
@RequestMapping("/")
public class HomeController {

	private final IPersonService personService;

	public HomeController(IPersonService personService) {
		this.personService = personService;
	}

	// PÁGINA PRINCIPAL
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

		modelAndView.addObject(
				"persons",
				personService.getAll()
		);

		return modelAndView;
	}

	// EJEMPLO: /hello?name=Juan
	@GetMapping("/hello")
	public ModelAndView helloParams1(
			@RequestParam(required = false, defaultValue = "null")
			String name) {

		ModelAndView mV =
				new ModelAndView(ViewRouteHelper.HELLO);

		mV.addObject("name", name);

		return mV;
	}

	// EJEMPLO: /hello/Juan
	@GetMapping("/hello/{name}")
	public ModelAndView helloParams2(
			@PathVariable String name) {

		ModelAndView mV =
				new ModelAndView(ViewRouteHelper.HELLO);

		mV.addObject("name", name);

		return mV;
	}

	// REDIRECCIÓN DESDE / HACIA EL INDEX
	@GetMapping("/")
	public RedirectView redirectToHomeIndex() {

		return new RedirectView(ViewRouteHelper.ROUTE);
	}
}
