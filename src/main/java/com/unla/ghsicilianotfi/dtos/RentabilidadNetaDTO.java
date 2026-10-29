
package com.unla.ghsicilianotfi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Rentabilidad neta de una unidad de venta en un festival.
 * No se persiste: se calcula en el momento a partir de pedidos, platos, liquidaciones y canon.
 */
@Getter
@AllArgsConstructor
public class RentabilidadNetaDTO {

    private Long idUnidad;
    private String nombreUnidad;
    private String codigoUnidad;
    private Long idFestival;
    private String nombreFestival;

    private BigDecimal totalVentas;      // Σ cantidad x precio de venta
    private BigDecimal costoPlatos;      // Σ cantidad x costo de producción
    private BigDecimal gananciaBruta;    // ventas - costo de platos
    private BigDecimal sueldos;          // liquidaciones de la unidad en el festival
    private BigDecimal canon;            // según los costos del festival
    private BigDecimal rentabilidadNeta; // ganancia bruta - sueldos - canon

    // true si la unidad ganó plata (o quedo en cero) en el festival
    public boolean isPositiva() {
        return rentabilidadNeta.signum() >= 0;
    }
}