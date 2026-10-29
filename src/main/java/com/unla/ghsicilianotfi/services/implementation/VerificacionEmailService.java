package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.entities.CodigoVerificacion;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.repositories.ICodigoVerificacionRepository;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;
import com.unla.ghsicilianotfi.services.IEmailService;
import com.unla.ghsicilianotfi.services.IVerificacionEmailService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service("verificacionEmailService")
public class VerificacionEmailService implements IVerificacionEmailService {

    // Minutos que dura un codigo y cantidad maxima de intentos fallidos
    private static final int MINUTOS_VALIDEZ = 15;
    private static final int MAX_INTENTOS = 5;

    // Generador de numeros al azar pensado para seguridad (no predecible)
    private final SecureRandom random = new SecureRandom();

    private final ICodigoVerificacionRepository codigoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IEmailService emailService;

    public VerificacionEmailService(ICodigoVerificacionRepository codigoRepository,
                                    IUsuarioRepository usuarioRepository,
                                    IEmailService emailService) {
        this.codigoRepository = codigoRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    // =====================================================
    // Generar y enviar un codigo nuevo
    // =====================================================
    @Override
    @Transactional
    public void generarYEnviarCodigo(Usuario usuario) {

        // 1. Anular los codigos anteriores que no se usaron
        for (CodigoVerificacion anterior : codigoRepository.findByUsuario_IdUsuarioAndUtilizadoFalse(usuario.getIdUsuario())) {
            anterior.setUtilizado(true);
            codigoRepository.save(anterior);
        }

        // 2. Crear el codigo nuevo: 6 dígitos al azar (000000 a 999999)
        CodigoVerificacion nuevo = new CodigoVerificacion();
        nuevo.setUsuario(usuario);
        nuevo.setCodigo(String.format("%06d", random.nextInt(1_000_000)));
        nuevo.setFechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ));
        codigoRepository.save(nuevo);

        // 3. Enviarlo por mail
        emailService.enviarCodigoVerificacion(usuario.getEmail(), nuevo.getCodigo());
    }

    // =====================================================
    // Verificar el codigo ingresado por el usuario
    // noRollbackFor: aunque el codigo sea incorrecto, el intento fallido se guarda igual
    // =====================================================
    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void verificarCodigo(String email, String codigoIngresado) {

        Usuario usuario = buscarUsuario(email);

        if (Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta ya está verificada. Podés iniciar sesión.");
        }

        CodigoVerificacion codigo = codigoRepository
                .findTopByUsuario_NombreOrderByIdCodigoDesc(usuario.getNombre())
                .orElseThrow(() -> new IllegalArgumentException("No hay un código pendiente. Pedí uno nuevo."));

        if (codigo.isUtilizado()) {
            throw new IllegalArgumentException("El código ya no es válido. Pedí uno nuevo.");
        }
        if (codigo.estaVencido()) {
            throw new IllegalArgumentException("El código venció. Pedí uno nuevo.");
        }
        if (codigo.getIntentos() >= MAX_INTENTOS) {
            throw new IllegalArgumentException("Superaste la cantidad de intentos. Pedí un código nuevo.");
        }

        // Codigo incorrecto: se suma un intento
        if (codigoIngresado == null || !codigo.getCodigo().equals(codigoIngresado.trim())) {
            codigo.setIntentos(codigo.getIntentos() + 1);
            codigoRepository.save(codigo);

            int restantes = MAX_INTENTOS - codigo.getIntentos();
            if (restantes <= 0) {
                throw new IllegalArgumentException("Código incorrecto. Superaste la cantidad de intentos: pedí un código nuevo.");
            }
            throw new IllegalArgumentException("Código incorrecto. Te quedan " + restantes + " intento(s).");
        }

        // Codigo correcto: se usa el codigo y se activa la cuenta
        codigo.setUtilizado(true);
        codigoRepository.save(codigo);

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    // =====================================================
    // Reenviar un codigo nuevo
    // =====================================================
    @Override
    @Transactional
    public void reenviarCodigo(String email) {

        Usuario usuario = buscarUsuario(email);

        if (Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta ya está verificada. Podés iniciar sesión.");
        }

        generarYEnviarCodigo(usuario);
    }

    // =====================================================
    // La cuenta existe y esta esperando la verificacion?
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public boolean estaPendienteDeVerificacion(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        String nombre = normalizar(email);

        return usuarioRepository.findByNombreConRol(nombre)
                .map(usuario -> !Boolean.TRUE.equals(usuario.getActivo())
                        && codigoRepository.findTopByUsuario_NombreOrderByIdCodigoDesc(nombre).isPresent())
                .orElse(false);
    }

    // Busca el usuario por email (el email es su nombre de usuario)
    private Usuario buscarUsuario(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Ingresá tu email");
        }
        return usuarioRepository.findByNombreConRol(normalizar(email))
                .orElseThrow(() -> new IllegalArgumentException("No existe una cuenta registrada con ese email"));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}