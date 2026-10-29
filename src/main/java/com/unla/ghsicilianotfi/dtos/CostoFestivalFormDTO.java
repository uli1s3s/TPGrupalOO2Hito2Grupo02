package com.unla.ghsicilianotfi.dtos;

import com.unla.ghsicilianotfi.entities.TipoCostoFestival;
import com.unla.ghsicilianotfi.entities.TipoUnidadAplicable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CostoFestivalFormDTO {

    @NotNull(message = "El tipo de costo es obligatorio")
    private TipoCostoFestival tipoCosto;

    private TipoUnidadAplicable tipoUnidadAplicable;

    private BigDecimal monto;

    private boolean aplicar;
}
