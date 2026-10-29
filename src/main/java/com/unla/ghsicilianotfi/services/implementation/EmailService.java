package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.services.IEmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío de mails por SMTP (Gmail con contraseña de aplicacion).
 * Los datos de la cuenta se leen de las variables de entorno MAIL_USERNAME y MAIL_PASSWORD.
 * Si no estan configuradas, el mail se muestra en la consola para no frenar el flujo.
 */
@Service("emailService")
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    // Cuenta desde la que se envian los mails (spring.mail.username)
    @Value("${spring.mail.username:}")
    private String remitente;

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

    // Envia un mail de texto simple. Si falla, lo registra en el log sin cortar la operacion.
    private void enviar(String destinatario, String asunto, String texto) {

        // Sin cuenta configurada: se muestra en consola (modo desarrollo)
        if (remitente == null || remitente.isBlank()) {
            log.warn("MAIL_USERNAME no configurado: el mail no se envía, se muestra en consola.");
            log.info("Para: {} | Asunto: {}\n{}", destinatario, asunto, texto);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(texto);

            mailSender.send(mensaje);
            log.info("Mail enviado a {}", destinatario);

        } catch (MailException e) {
            // No se corta el alta del empleado si el mail falla (por ejemplo, sin internet)
            log.error("No se pudo enviar el mail a {}: {}", destinatario, e.getMessage());
        }
    }
    
    @Override
    public void enviarCodigoVerificacion(String destinatario, String codigo) {

        String asunto = "Código de verificación - Epicentro Gourmet";
        String texto = "¡Hola!\n\n"
                + "Gracias por registrarte como responsable en Epicentro Gourmet.\n\n"
                + "Tu código de verificación es: " + codigo + "\n\n"
                + "Ingresalo en la pantalla de verificación para activar tu cuenta.\n"
                + "El código vence en 15 minutos.\n\n"
                + "Si no fuiste vos, ignorá este mensaje.\n\n"
                + "Saludos,\nEpicentro Gourmet";

        enviar(destinatario, asunto, texto);
    }
    
    @Override
    public void enviarAvisoReactivacion(String destinatario, String username, String contrasenaPlana, String nombreUnidad) {

        String asunto = "Tu cuenta de Epicentro Gourmet fue reactivada";
        String texto = "¡Hola!\n\n"
                + "Te volvieron a dar de alta en el equipo de trabajo de Epicentro Gourmet.\n"
                + "Unidad de venta asignada: " + nombreUnidad + "\n\n"
                + "Estos son tus nuevos datos para iniciar sesión:\n"
                + "Usuario: " + username + "\n"
                + "Contraseña temporal: " + contrasenaPlana + "\n\n"
                + "Tu contraseña anterior ya no funciona.\n"
                + "Podés ingresar en: http://localhost:8080/login\n"
                + "Te recomendamos cambiar la contraseña desde tu perfil.\n\n"
                + "Saludos,\nEpicentro Gourmet";

        enviar(destinatario, asunto, texto);
    }
}