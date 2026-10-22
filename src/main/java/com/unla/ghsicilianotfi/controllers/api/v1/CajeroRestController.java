package com.unla.ghsicilianotfi.controllers.api.v1;

import com.unla.ghsicilianotfi.dtos.CajeroDTO;
import com.unla.ghsicilianotfi.services.ICajeroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cajeros")
public class CajeroRestController {

    private final ICajeroService cajeroService;

    public CajeroRestController(ICajeroService cajeroService) {
        this.cajeroService = cajeroService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CajeroDTO>> getCajeros(
            @RequestParam(name = "soloActivos", required = false, defaultValue = "false") boolean soloActivos) {
        List<CajeroDTO> cajeros = soloActivos ? cajeroService.getAllActivos() : cajeroService.getAll();
        return ResponseEntity.ok(cajeros);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CajeroDTO> getCajeroById(@PathVariable(name = "id") Long id) {
        return cajeroService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/turno/{turno}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CajeroDTO>> getCajerosPorTurno(@PathVariable(name = "turno") String turno) {
        return ResponseEntity.ok(cajeroService.findByTurno(turno));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCajero(@Valid @RequestBody CajeroDTO dto) {
        try {
            CajeroDTO creado = cajeroService.insertOrUpdate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCajero(@PathVariable(name = "id") Long id,
                                         @Valid @RequestBody CajeroDTO dto) {
        if (cajeroService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        dto.setIdPersonal(id);
        try {
            CajeroDTO actualizado = cajeroService.insertOrUpdate(dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable(name = "id") Long id) {
        if (cajeroService.darDeBajaLogica(id)) {
            return ResponseEntity.ok("Cajero dado de baja lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/alta")
    public ResponseEntity<?> darDeAlta(@PathVariable(name = "id") Long id) {
        if (cajeroService.darDeAltaLogica(id)) {
            return ResponseEntity.ok("Cajero reactivado lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCajero(@PathVariable(name = "id") Long id) {
        if (cajeroService.remove(id)) {
            return ResponseEntity.ok("Cajero eliminado con éxito");
        }
        return ResponseEntity.notFound().build();
    }
}

