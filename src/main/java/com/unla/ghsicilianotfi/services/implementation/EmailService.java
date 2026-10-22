package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.services.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("emailService")
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Override
    public void enviarCredenciales(String destinatario, String username, String contrasenaPlana) {
        log.info("==================================================================");
        log.info("[ENVÍO DE EMAIL AUTOMÁTICO - EPICENTRO GOURMET]");
        log.info("Para: {}", destinatario);
        log.info("Asunto: Tus credenciales de acceso a Epicentro Gourmet");
        log.info("Bienvenido al equipo de trabajo de los Festivales Gastronómicos.");
        log.info("Usuario: {}", username);
        log.info("Contraseña temporal: {}", contrasenaPlana);
        log.info("Podés iniciar sesión en: http://localhost:8080/login");
        log.info("==================================================================");
    }
}

