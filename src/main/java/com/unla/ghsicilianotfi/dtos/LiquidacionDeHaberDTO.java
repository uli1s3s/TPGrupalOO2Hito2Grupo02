package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionDeHaberDTO {

    private Long idLiquidacion;
    private Long idPersonal;
    private String nombreCompleto;
    private String dni;
    private String tipoEmpleado; // Cocinero o Cajero
    private String unidadVenta;
    private String periodo;
    private LocalDate fechaLiquidacion;
    private int diasTrabajados;
    private double sueldoBase;
    private double adicional;
    private String detalleAdicional;
    private int porcentajeAplicado;
    private double totalACobrar;
}

