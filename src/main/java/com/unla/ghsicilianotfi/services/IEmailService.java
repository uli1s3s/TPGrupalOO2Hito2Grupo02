package com.unla.ghsicilianotfi.services;

public interface IEmailService {

    void enviarCredenciales(String destinatario, String username, String contrasenaPlana);
    
    // Codigo para verificar el email de un responsable recien registrado
    void enviarCodigoVerificacion(String destinatario, String codigo);
}

