package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "puesto_desmontable")
@PrimaryKeyJoinColumn(name = "id_unidad")
@Getter
@Setter
@NoArgsConstructor
public class PuestoDesmontable extends UnidadVenta {

    @Column(nullable = false)
    private Integer cantidadCarpas;

    @Column(nullable = false)
    private Integer tiempoMontajeMinutos;
    
    @Override
    public String getTipo() {
        return "Puesto desmontable";
    }

    @Override
    public String getDetalle() {
        return cantidadCarpas + " carpa(s) - montaje de " + tiempoMontajeMinutos + " min";
    }
    
    @Override
    public String getPrefijoCodigo() {
        return "PD";
    }
}