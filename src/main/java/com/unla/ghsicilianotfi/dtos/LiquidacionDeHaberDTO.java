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
    private String nombreFestival;

    public String getPeriodoDescripcion() {
        if (periodo == null || periodo.trim().isEmpty()) {
            return "-";
        }
        try {
            java.time.YearMonth ym = java.time.YearMonth.parse(periodo.trim());
            String mes = ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es", "AR"));
            String mesCap = mes.substring(0, 1).toUpperCase() + mes.substring(1);
            return "Liquidación de " + mesCap + " " + ym.getYear();
        } catch (Exception e) {
            return periodo;
        }
    }

    public String getNombreFestivalLimpio() {
        if (nombreFestival == null || nombreFestival.trim().isEmpty()) {
            return "Festival General";
        }
        return nombreFestival.replaceAll("\\s*\\([^)]*\\)", "").trim();
    }
}
