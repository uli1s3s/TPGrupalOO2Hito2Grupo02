
package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.services.IEmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("emailService")
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarCredenciales(String destinatario, String username, String contrasenaPlana) {

        String asunto = "Tus credenciales de acceso a Epicentro Gourmet";

        String texto = "¡Hola!\n\n"
                + "Te damos la bienvenida al equipo de trabajo de los festivales de Epicentro Gourmet.\n\n"
                + "Estos son tus datos para iniciar sesión:\n"
                + "Usuario: " + username + "\n"
                + "Contraseña temporal: " + contrasenaPlana + "\n\n"
                + "Podés ingresar en: http://localhost:8080/login\n"
                + "Te recomendamos cambiar la contraseña desde tu perfil.\n\n"
                + "Saludos,\nEpicentro Gourmet";

        enviar(destinatario, asunto, texto);
    }

    private void enviar(String destinatario, String asunto, String texto) {

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();

            mensaje.setFrom("epicentrogourmet@localhost");
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(texto);

            mailSender.send(mensaje);

            log.info("Correo enviado a Mailpit para: {}", destinatario);

        } catch (MailException e) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
        }
    }
}
