
package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cierre_caja",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cierre_unidad_fecha",
                columnNames = {"unidad_id", "fecha"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCierre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_id", nullable = false)
    private UnidadVenta unidadVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cajero_id", nullable = false)
    private Cajero cajero;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalDateTime fechaHoraCierre;

    @Column(nullable = false)
    private int cantidadPedidos;

    @Column(nullable = false)
    private double totalRecaudado;

    // ============================================
    // NUEVOS ATRIBUTOS: REAPERTURA DE CAJA
    // ============================================

    // true: caja cerrada
    // false: caja reabierta
    @Column(nullable = false)
    private boolean cerrada = true;

    // Fecha y hora de la última reapertura
    @Column
    private LocalDateTime fechaHoraReapertura;

    // Motivo indicado por el cajero
    @Column(length = 255)
    private String motivoReapertura;

    // Cantidad de veces que se reabrió la caja
    @Column(nullable = false)
    private int cantidadReaperturas = 0;

    public CierreCaja(
            UnidadVenta unidadVenta,
            Cajero cajero,
            LocalDate fecha,
            int cantidadPedidos,
            double totalRecaudado) {

        this.unidadVenta = unidadVenta;
        this.cajero = cajero;
        this.fecha = fecha;
        this.fechaHoraCierre = LocalDateTime.now();
        this.cantidadPedidos = cantidadPedidos;
        this.totalRecaudado = totalRecaudado;

        this.cerrada = true;
        this.cantidadReaperturas = 0;
    }

    // ============================================
    // REABRIR CAJA
    // ============================================

    public void reabrir(String motivo) {

        if (!cerrada) {
            throw new IllegalStateException(
                    "La caja ya se encuentra abierta."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "Debés indicar un motivo para reabrir la caja."
            );
        }

        this.cerrada = false;
        this.fechaHoraReapertura = LocalDateTime.now();
        this.motivoReapertura = motivo.trim();
        this.cantidadReaperturas++;
    }

    // ============================================
    // VOLVER A CERRAR CAJA
    // ============================================

    public void volverACerrar(
            Cajero cajero,
            int cantidadPedidos,
            double totalRecaudado) {

        if (cerrada) {
            throw new IllegalStateException(
                    "La caja ya se encuentra cerrada."
            );
        }

        this.cajero = cajero;
        this.cantidadPedidos = cantidadPedidos;
        this.totalRecaudado = totalRecaudado;
        this.fechaHoraCierre = LocalDateTime.now();
        this.cerrada = true;
    }
}
