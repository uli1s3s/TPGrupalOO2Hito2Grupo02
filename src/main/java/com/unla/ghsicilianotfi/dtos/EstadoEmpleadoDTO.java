package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fila de la tabla unificada de personal en "Mi unidad".
 * Muestra cada empleado con su estado: libre, ocupado en una unidad o dado de baja.
 * No se persiste: se arma en el momento.
 */
@Getter
@AllArgsConstructor
public class EstadoEmpleadoDTO {

    // Estados posibles
    public static final String LIBRE = "LIBRE";
    public static final String OCUPADO = "OCUPADO";
    public static final String DADO_DE_BAJA = "DADO_DE_BAJA";

    private Long idPersonal;
    private String nombre;
    private String apellido;
    private String dni;
    private String tipo;          // Cocinero o Cajero
    private String email;
    private String estado;        // LIBRE, OCUPADO o DADO_DE_BAJA
    private Long idUnidad;        // unidad donde trabaja (null si está libre o de baja)
    private String nombreUnidad;  // nombre de esa unidad (null si está libre o de baja)

    // Metodos de ayuda para la vista (Thymeleaf)
    public boolean isLibre() {
        return LIBRE.equals(estado);
    }

    public boolean isOcupado() {
        return OCUPADO.equals(estado);
    }

    public boolean isDadoDeBaja() {
        return DADO_DE_BAJA.equals(estado);
    }

    // Texto que se muestra en la columna "Estado"
    public String getEstadoTexto() {
        if (isOcupado()) {
            return "Ocupado en " + nombreUnidad;
        }
        if (isDadoDeBaja()) {
            return "Dado de baja";
        }
        return "Libre";
    }
}