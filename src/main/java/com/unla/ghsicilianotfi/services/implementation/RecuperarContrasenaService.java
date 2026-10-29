
package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.entities.TokenRecuperacion;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.repositories.ITokenRecuperacionRepository;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class RecuperarContrasenaService {

    private final IUsuarioRepository usuarioRepository;
    private final ITokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username:}")
    private String remitente;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public RecuperarContrasenaService(
            IUsuarioRepository usuarioRepository,
            ITokenRecuperacionRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender) {

        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // SOLICITAR RECUPERACIÓN DE CONTRASEÑA

    @Transactional
    public void solicitarRecuperacion(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        // Buscar directamente al usuario por su correo
        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByEmailIgnoreCase(email.trim())
                        .filter(u -> Boolean.TRUE.equals(u.getActivo()));

        // No revelar si el correo está registrado
        if (usuarioEncontrado.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioEncontrado.get();

        // Buscar solicitudes anteriores no utilizadas
        List<TokenRecuperacion> anteriores =
                tokenRepository.findByUsuario_IdUsuarioAndUtilizadoFalse(
                        usuario.getIdUsuario());

        LocalDateTime ahora = LocalDateTime.now();

        // Comprobar si ya existe un enlace vigente
        boolean existeTokenActivo = anteriores.stream()
                .anyMatch(t ->
                        t.getFechaExpiracion().isAfter(ahora));

        // Impedir otra solicitud mientras el enlace esté activo
        if (existeTokenActivo) {
            throw new IllegalStateException(
                    "Ya se envió un enlace de recuperación a tu correo. "
                            + "Podrás solicitar uno nuevo cuando venza "
                            + "el enlace anterior (máximo 15 minutos)."
            );
        }

        // Marcar como utilizados los tokens anteriores vencidos
        for (TokenRecuperacion anterior : anteriores) {
            anterior.setUtilizado(true);
        }

        tokenRepository.saveAll(anteriores);

        // Generar token aleatorio seguro
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        // Guardar únicamente el hash del token
        TokenRecuperacion recuperacion = new TokenRecuperacion();

        recuperacion.setUsuario(usuario);
        recuperacion.setTokenHash(calcularHash(token));
        recuperacion.setFechaExpiracion(
                ahora.plusMinutes(15));
        recuperacion.setUtilizado(false);

        tokenRepository.save(recuperacion);

        // Preparar enlace de recuperación
        String enlace = baseUrl
                + "/restablecer-contrasena?token="
                + token;

        // Verificar configuración SMTP
        if (remitente == null || remitente.isBlank()) {
            throw new IllegalStateException(
                    "El correo SMTP no está configurado.");
        }

        // Crear correo electrónico
        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom(remitente);
        mensaje.setTo(usuario.getEmail());
        mensaje.setSubject(
                "Recuperar contraseña - Epicentro Gourmet");

        mensaje.setText(
                "Hola:\n\n"
                        + "Recibimos una solicitud para restablecer tu contraseña.\n\n"
                        + "Ingresá al siguiente enlace:\n"
                        + enlace + "\n\n"
                        + "El enlace vence en 15 minutos y solo puede "
                        + "utilizarse una vez.\n\n"
                        + "Si no solicitaste este cambio, ignorá el correo.\n\n"
                        + "Epicentro Gourmet"
        );

        // Enviar correo mediante Gmail SMTP
        mailSender.send(mensaje);
    }

    // VERIFICAR SI EL TOKEN ES VÁLIDO

    @Transactional(readOnly = true)
    public boolean tokenValido(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        return tokenRepository.findByTokenHash(calcularHash(token))
                .filter(t -> !t.isUtilizado())
                .filter(t -> t.getFechaExpiracion()
                        .isAfter(LocalDateTime.now()))
                .isPresent();
    }

    // RESTABLECER CONTRASEÑA

    @Transactional
    public void restablecerContrasena(
            String token,
            String nuevaContrasena,
            String confirmacion) {

        // Validar longitud mínima
        if (nuevaContrasena == null
                || nuevaContrasena.length() < 8) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres.");
        }

        // Validar coincidencia
        if (!nuevaContrasena.equals(confirmacion)) {

            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden.");
        }

        // Validar token recibido
        if (token == null || token.isBlank()) {

            throw new IllegalArgumentException(
                    "El enlace de recuperación no es válido.");
        }

        // Buscar token por su hash
        TokenRecuperacion recuperacion =
                tokenRepository.findByTokenHash(calcularHash(token))
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El enlace de recuperación no es válido."));

        // Verificar que no esté utilizado ni vencido
        if (recuperacion.isUtilizado()
                || !recuperacion.getFechaExpiracion()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "El enlace venció o ya fue utilizado.");
        }

        Usuario usuario = recuperacion.getUsuario();

        // Verificar que la cuenta esté activa
        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            throw new IllegalArgumentException(
                    "No se puede restablecer esta cuenta.");
        }

        // Impedir reutilizar la contraseña anterior
        if (passwordEncoder.matches(
                nuevaContrasena,
                usuario.getContrasena())) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente a la anterior.");
        }

        // Guardar nueva contraseña con BCrypt
        usuario.setContrasena(
                passwordEncoder.encode(nuevaContrasena));

        usuarioRepository.save(usuario);

        // Invalidar todos los tokens pendientes del usuario
        List<TokenRecuperacion> pendientes =
                tokenRepository.findByUsuario_IdUsuarioAndUtilizadoFalse(
                        usuario.getIdUsuario());

        for (TokenRecuperacion pendiente : pendientes) {
            pendiente.setUtilizado(true);
        }

        tokenRepository.saveAll(pendientes);
    }

    // GENERAR HASH SHA-256 DEL TOKEN

    private String calcularHash(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "No se pudo generar el hash del token.", e);
        }
    }
}
