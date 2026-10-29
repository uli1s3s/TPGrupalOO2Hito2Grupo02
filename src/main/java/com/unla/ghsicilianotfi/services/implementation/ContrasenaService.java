
package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CambioContrasenaDTO;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;
import com.unla.ghsicilianotfi.services.IContrasenaService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContrasenaService implements IContrasenaService {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ContrasenaService(
            IUsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void cambiarContrasena(
            String nombreUsuario,
            CambioContrasenaDTO datos) {

        Usuario usuario = usuarioRepository
                .findByNombreConRol(nombreUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario no existe"));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(
                datos.getContrasenaActual(),
                usuario.getContrasena())) {

            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta");
        }

        // Verificar confirmación
        if (!datos.getContrasenaNueva().equals(
                datos.getConfirmarContrasena())) {

            throw new IllegalArgumentException(
                    "Las nuevas contraseñas no coinciden");
        }

        // Impedir reutilizar la contraseña actual
        if (passwordEncoder.matches(
                datos.getContrasenaNueva(),
                usuario.getContrasena())) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente a la actual");
        }

        // Cifrar nueva contraseña
        usuario.setContrasena(
                passwordEncoder.encode(
                        datos.getContrasenaNueva()));

        usuarioRepository.save(usuario);
    }
}
