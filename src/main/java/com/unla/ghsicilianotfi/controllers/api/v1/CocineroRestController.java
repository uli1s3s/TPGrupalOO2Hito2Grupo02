package com.unla.ghsicilianotfi.controllers.api.v1;

import com.unla.ghsicilianotfi.dtos.CocineroDTO;
import com.unla.ghsicilianotfi.services.ICocineroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cocineros")
public class CocineroRestController {

    private final ICocineroService cocineroService;

    public CocineroRestController(ICocineroService cocineroService) {
        this.cocineroService = cocineroService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CocineroDTO>> getCocineros(
            @RequestParam(name = "soloActivos", required = false, defaultValue = "false") boolean soloActivos) {
        List<CocineroDTO> cocineros = soloActivos ? cocineroService.getAllActivos() : cocineroService.getAll();
        return ResponseEntity.ok(cocineros);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CocineroDTO> getCocineroById(@PathVariable(name = "id") Long id) {
        return cocineroService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/categoria/{idCategoria}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CocineroDTO>> getCocinerosPorCategoria(@PathVariable(name = "idCategoria") Long idCategoria) {
        return ResponseEntity.ok(cocineroService.findByCategoria(idCategoria));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCocinero(@Valid @RequestBody CocineroDTO dto) {
        try {
            CocineroDTO creado = cocineroService.insertOrUpdate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCocinero(@PathVariable(name = "id") Long id,
                                           @Valid @RequestBody CocineroDTO dto) {
        if (cocineroService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        dto.setIdPersonal(id);
        try {
            CocineroDTO actualizado = cocineroService.insertOrUpdate(dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable(name = "id") Long id) {
        if (cocineroService.darDeBajaLogica(id)) {
            return ResponseEntity.ok("Cocinero dado de baja lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/alta")
    public ResponseEntity<?> darDeAlta(@PathVariable(name = "id") Long id) {
        if (cocineroService.darDeAltaLogica(id)) {
            return ResponseEntity.ok("Cocinero reactivado lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCocinero(@PathVariable(name = "id") Long id) {
        if (cocineroService.remove(id)) {
            return ResponseEntity.ok("Cocinero eliminado con éxito");
        }
        return ResponseEntity.notFound().build();
    }
}

