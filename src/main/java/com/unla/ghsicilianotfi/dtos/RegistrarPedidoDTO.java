package com.unla.ghsicilianotfi.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistrarPedidoDTO {

    private Long idFestival;

    private Long idUnidad;

    private List<DetallePedidoDTO> detalles;

}