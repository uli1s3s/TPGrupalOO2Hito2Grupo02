package com.unla.ghsicilianotfi.controllers.api.v1;

import com.unla.ghsicilianotfi.dtos.CategoriaCocineroDTO;
import com.unla.ghsicilianotfi.services.ICategoriaCocineroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias-cocinero")
public class CategoriaCocineroRestController {

    private final ICategoriaCocineroService categoriaCocineroService;

    public CategoriaCocineroRestController(ICategoriaCocineroService categoriaCocineroService) {
        this.categoriaCocineroService = categoriaCocineroService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoriaCocineroDTO>> getCategorias() {
        return ResponseEntity.ok(categoriaCocineroService.getAll());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoriaCocineroDTO> getCategoriaById(@PathVariable(name = "id") Long id) {
        return categoriaCocineroService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCategoria(@Valid @RequestBody CategoriaCocineroDTO dto) {
        try {
            CategoriaCocineroDTO creada = categoriaCocineroService.insertOrUpdate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCategoria(@PathVariable(name = "id") Long id,
                                             @Valid @RequestBody CategoriaCocineroDTO dto) {
        if (categoriaCocineroService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        dto.setIdCategoria(id);
        try {
            CategoriaCocineroDTO actualizada = categoriaCocineroService.insertOrUpdate(dto);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategoria(@PathVariable(name = "id") Long id) {
        if (categoriaCocineroService.remove(id)) {
            return ResponseEntity.ok("Categoría eliminada con éxito");
        }
        return ResponseEntity.notFound().build();
    }
}

