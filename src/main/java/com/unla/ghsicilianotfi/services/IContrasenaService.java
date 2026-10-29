package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.CambioContrasenaDTO;

public interface IContrasenaService {

    void cambiarContrasena(
            String nombreUsuario,
            CambioContrasenaDTO datos
    );
}