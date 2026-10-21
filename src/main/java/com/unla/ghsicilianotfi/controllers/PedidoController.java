
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.dtos.RegistrarPedidoDTO;
import com.unla.ghsicilianotfi.services.implementation.PedidoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<String> registrarPedido(
            @RequestBody RegistrarPedidoDTO datos) {

        pedidoService.registrarPedido(datos);

        return ResponseEntity.ok("Pedido registrado correctamente");
    }
}
