
package com.unla.ghsicilianotfi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.unla.ghsicilianotfi.services.implementation.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

	private final UserService userService;
	private final LoginFallidoHandler loginFallidoHandler;

	public SecurityConfiguration(UserService userService, LoginFallidoHandler loginFallidoHandler) {
		this.userService = userService;
		this.loginFallidoHandler = loginFallidoHandler;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable)

				.authorizeHttpRequests(auth -> {

					// RECURSOS ESTÁTICOS
					auth.requestMatchers(
							"/css/**",
							"/imgs/**",
							"/js/**",
							"/vendor/**"
					).permitAll();

					// PÁGINAS PÚBLICAS
					auth.requestMatchers(
							"/",
							"/index",
							"/login",
							"/error"
					).permitAll();

					// REGISTRO PUBLICO DE RESPONSABLES
					// Incorporado desde develop
					auth.requestMatchers(
							"/registro-responsable",
							"/registro-responsable/**"
					).permitAll();

					// CAMBIAR CONTRASEÑA
					// Cualquier usuario que haya iniciado sesión
					auth.requestMatchers(
							"/cambiar-contrasena"
					).authenticated();

					// PEDIDOS
					auth.requestMatchers(
							"/api/v1/pedidos",
							"/api/v1/pedidos/**"
					).hasAnyRole(
							"ADMINISTRADOR",
							"RESPONSABLE",
							"EMPLEADO"
					);

					// PERSONAL
					auth.requestMatchers(
							"/empleados",
							"/empleados/**"
					).hasAnyRole(
							"ADMINISTRADOR",
							"RESPONSABLE"
					);

					// LIQUIDACIONES: Administradores y Responsables
					auth.requestMatchers(
							"/liquidaciones",
							"/liquidaciones/**"
					).hasAnyRole(
							"ADMINISTRADOR",
							"RESPONSABLE"
					);

					// RECIBOS DE SUELDO: Administradores, Responsables y Empleados
					auth.requestMatchers(
							"/recibos",
							"/recibos/**"
					).hasAnyRole(
							"ADMINISTRADOR",
							"RESPONSABLE",
							"EMPLEADO"
					);

					// GESTIÓN DE FESTIVALES: solo administradores
					auth.requestMatchers(
							"/festivales",
							"/festivales/**"
					).hasRole("ADMINISTRADOR");

					auth.requestMatchers(
							"/platos",
							"/platos/**"
					).hasRole("RESPONSABLE");

					auth.requestMatchers("/api/v1/**").permitAll();

					// RESTO DE LAS RUTAS
					auth.anyRequest().authenticated();
				})

				.formLogin(login -> {
					login.loginPage("/login");
					login.loginProcessingUrl("/loginprocess");
					login.usernameParameter("username");
					login.passwordParameter("password");
					login.defaultSuccessUrl("/loginsuccess", true);
					login.failureHandler(loginFallidoHandler);
					login.permitAll();
				})

				.logout(logout -> {
					logout.logoutUrl("/logout");
					logout.logoutSuccessUrl("/login");
					logout.permitAll();
				})

				.build();
	}

	@Bean
	AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration
	) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	AuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider provider =
				new DaoAuthenticationProvider();

		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(userService);

		return provider;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
