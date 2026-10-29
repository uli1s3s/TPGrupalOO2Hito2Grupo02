package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.unla.ghsicilianotfi.configuration.ParametrosCanon;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "food_truck")
@PrimaryKeyJoinColumn(name = "id_unidad")
@Getter
@Setter
@NoArgsConstructor
public class FoodTruck extends UnidadVenta {

    @Column(nullable = false)
    private String patente;

    @Column(nullable = false)
    private Boolean electricidad;
    
    @Override
    public String getTipo() {
        return "Food truck";
    }

    @Override
    public String getDetalle() {
        return "Patente " + patente
                + (Boolean.TRUE.equals(electricidad) ? " - con electricidad" : " - sin electricidad");
    }
    
    @Override
    public String getPrefijoCodigo() {
        return "FT";
    }
    
    // Canon = (superficie x costo por m2) + plus si requiere electricidad
    @Override
    public BigDecimal calcularCanon(ParametrosCanon parametros) {
        BigDecimal canon = getSuperficieDecimal().multiply(parametros.getCostoPorM2());

        if (Boolean.TRUE.equals(electricidad)) {
            canon = canon.add(parametros.getPlusElectricidad());
        }

        return canon.setScale(2, RoundingMode.HALF_UP);
    }
}