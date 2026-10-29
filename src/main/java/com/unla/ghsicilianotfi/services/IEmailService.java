package com.unla.ghsicilianotfi.services;

public interface IEmailService {

    void enviarCredenciales(String destinatario, String username, String contrasenaPlana);
    
    // Codigo para verificar el email de un responsable recien registrado
    void enviarCodigoVerificacion(String destinatario, String codigo);
    
    // Aviso al empleado reactivado: unidad asignada y nueva contraseña temporal
    void enviarAvisoReactivacion(String destinatario, String username, String contrasenaPlana, String nombreUnidad);
}

