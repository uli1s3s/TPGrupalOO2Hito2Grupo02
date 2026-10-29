package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.entities.Usuario;

public interface IVerificacionEmailService {

    // Genera un codigo nuevo para el usuario (anula los anteriores) y lo envia por mail
    void generarYEnviarCodigo(Usuario usuario);

    // Verifica el codigo ingresado; si es correcto, activa la cuenta
    void verificarCodigo(String email, String codigoIngresado);

    // Envía un codigo nuevo a un usuario que todavía no verifico su email
    void reenviarCodigo(String email);

    // true si existe una cuenta con ese email que todavia no fue verificada
    boolean estaPendienteDeVerificacion(String email);
}