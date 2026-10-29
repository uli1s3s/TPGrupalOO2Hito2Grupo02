package com.unla.ghsicilianotfi.configuration;

import com.unla.ghsicilianotfi.services.IVerificacionEmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Que hacer cuando falla el inicio de sesión.
 * Si el usuario es un responsable que todavía no verifico su email,
 * se lo manda a la pantalla para ingresar el código.
 * En cualquier otro caso se vuelve al login con el error de siempre.
 */
@Component
public class LoginFallidoHandler implements AuthenticationFailureHandler {

    private final IVerificacionEmailService verificacionEmailService;

    public LoginFallidoHandler(IVerificacionEmailService verificacionEmailService) {
        this.verificacionEmailService = verificacionEmailService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String usuario = request.getParameter("username");
        String email = (usuario == null) ? "" : usuario.trim().toLowerCase(Locale.ROOT);

        // Cuenta deshabilitada + verificación pendiente => a la pantalla del codigo
        if (exception instanceof DisabledException
                && verificacionEmailService.estaPendienteDeVerificacion(email)) {

            response.sendRedirect(request.getContextPath()
                    + "/registro-responsable/verificar?email="
                    + URLEncoder.encode(email, StandardCharsets.UTF_8)
                    + "&pendiente=true");
            return;
        }

        // Cualquier otro error: igual que antes
        response.sendRedirect(request.getContextPath() + "/login?error=true");
    }
}