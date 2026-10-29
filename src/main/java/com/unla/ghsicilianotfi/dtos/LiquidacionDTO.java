package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado de la liquidación de un empleado.
 * Contiene los datos calculados para mostrar en la vista previa.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionDTO {

    private Long idPersonal;
    private String empleado;          // "Apellido, Nombre"
    private String dni;
    private String tipo;              // Cocinero / Cajero
    private String unidad;            // Nombre comercial de la unidad
    private BigDecimal sueldoBase;
    private BigDecimal adicional;
    private String detalleAdicional;  // "Plus Lavaplatos" / "3 año(s) de antigüedad"
    private int diasFestival;
    private int porcentaje;           // 50, 100, 150...
    private BigDecimal totalACobrar;
    private LocalDate fechaIngreso;
    private boolean yaLiquidado;
    private String periodo;

    public LiquidacionDTO(Long idPersonal, String empleado, String dni, String tipo,
                          String unidad, BigDecimal sueldoBase, BigDecimal adicional,
                          String detalleAdicional, int diasFestival, int porcentaje,
                          BigDecimal totalACobrar, LocalDate fechaIngreso) {
        this.idPersonal = idPersonal;
        this.empleado = empleado;
        this.dni = dni;
        this.tipo = tipo;
        this.unidad = unidad;
        this.sueldoBase = sueldoBase;
        this.adicional = adicional;
        this.detalleAdicional = detalleAdicional;
        this.diasFestival = diasFestival;
        this.porcentaje = porcentaje;
        this.totalACobrar = totalACobrar;
        this.fechaIngreso = fechaIngreso;
        this.yaLiquidado = false;
        this.periodo = "";
    }

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
}
