package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "liquidacion_de_haber")
@Getter
@Setter
@NoArgsConstructor
public class LiquidacionDeHaber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLiquidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personal", nullable = false)
    private Personal personal;

    // Período liquidado, ej: "2026-10" o "Agosto 2026"
    @Column(nullable = false, length = 30)
    private String periodo;

    @Column(nullable = false)
    private LocalDate fechaLiquidacion;

    @Column(nullable = false)
    private int diasTrabajados;

    @Column(nullable = false)
    private double sueldoBase;

    @Column(nullable = false)
    private double adicional;

    @Column(nullable = false)
    private String detalleAdicional;

    // Porcentaje según tramo de días (ej: 50, 100, 150)
    @Column(nullable = false)
    private int porcentajeAplicado;

    @Column(nullable = false)
    private double totalACobrar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = true)
    private UnidadVenta unidadVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_festival", nullable = true)
    private Festival festival;

    public LiquidacionDeHaber(Personal personal, String periodo, LocalDate fechaLiquidacion,
                              int diasTrabajados, double sueldoBase, double adicional,
                              String detalleAdicional, int porcentajeAplicado, double totalACobrar,
                              UnidadVenta unidadVenta, Festival festival) {
        this.personal = personal;
        this.periodo = periodo;
        this.fechaLiquidacion = fechaLiquidacion;
        this.diasTrabajados = diasTrabajados;
        this.sueldoBase = sueldoBase;
        this.adicional = adicional;
        this.detalleAdicional = detalleAdicional;
        this.porcentajeAplicado = porcentajeAplicado;
        this.totalACobrar = totalACobrar;
        this.unidadVenta = unidadVenta;
        this.festival = festival;
    }
}
