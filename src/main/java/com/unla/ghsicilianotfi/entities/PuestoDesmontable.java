package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.unla.ghsicilianotfi.helpers.TarifaCanon;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
    
    // Canon = (superficie x costo por m2) - (minutos de montaje x descuento por minuto)
    @Override
    public BigDecimal calcularCanon(TarifaCanon parametros) {
        BigDecimal canon = getSuperficieDecimal().multiply(parametros.getCostoPorM2());

        BigDecimal descuento = parametros.getDescuentoPorMinutoMontaje()
                .multiply(BigDecimal.valueOf(tiempoMontajeMinutos));

        canon = canon.subtract(descuento);

        // El canon nunca puede ser negativo
        if (canon.compareTo(BigDecimal.ZERO) < 0) {
            canon = BigDecimal.ZERO;
        }

        return canon.setScale(2, RoundingMode.HALF_UP);
    }
}