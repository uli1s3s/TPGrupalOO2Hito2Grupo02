package com.unla.ghsicilianotfi.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PlatoFormDTO {

    private Long idPlato;

    @NotBlank(message = "El nombre del plato es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    private String nombre;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales")
    private BigDecimal precioVenta;

    @NotNull(message = "El costo de producción es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El costo admite hasta 10 enteros y 2 decimales")
    private BigDecimal costoProduccion;

    @NotNull(message = "Seleccioná la unidad de venta")
    private Long idUnidad;

    private String nombreUnidad;

    private boolean activo = true;
}
