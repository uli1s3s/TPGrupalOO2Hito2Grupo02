package com.unla.ghsicilianotfi.controllers.api.v1;

import com.unla.ghsicilianotfi.dtos.CocineroDTO;
import com.unla.ghsicilianotfi.services.ICocineroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cocineros")
@Tag(name = "Cocineros", description = "Endpoints para la gestión integral y consulta de cocineros del predio gastronómico")
public class CocineroRestController {

    private final ICocineroService cocineroService;

    public CocineroRestController(ICocineroService cocineroService) {
        this.cocineroService = cocineroService;
    }

    @Operation(
            summary = "Listar todos los cocineros",
            description = "Obtiene la lista completa de cocineros, con opción de incluir solo aquellos que se encuentren laboralmente activos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cocineros obtenido exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CocineroDTO.class))))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CocineroDTO>> getCocineros(
            @Parameter(description = "Si es true, lista únicamente los cocineros con estado activo", example = "false")
            @RequestParam(name = "soloActivos", required = false, defaultValue = "false") boolean soloActivos) {
        List<CocineroDTO> cocineros = soloActivos ? cocineroService.getAllActivos() : cocineroService.getAll();
        return ResponseEntity.ok(cocineros);
    }

    @Operation(
            summary = "Filtrar cocineros con criterios múltiples",
            description = "Permite buscar y filtrar cocineros combinando texto (búsqueda parcial sobre nombre o apellido), categoría, estado de actividad y sueldo base mínimo. Todos los parámetros son opcionales."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cocineros que coinciden con los filtros especificados",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CocineroDTO.class))))
    })
    @GetMapping(value = "/filtrar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CocineroDTO>> filtrarCocineros(
            @Parameter(description = "Texto a buscar en nombre o apellido (insensible a mayúsculas/minúsculas)", example = "Carlos")
            @RequestParam(name = "texto", required = false) String texto,

            @Parameter(description = "ID de la categoría de cocinero (ver /api/v1/categorias-cocinero)", example = "1")
            @RequestParam(name = "idCategoria", required = false) Long idCategoria,

            @Parameter(description = "Estado de actividad laboral (true: activos, false: inactivos)", example = "true")
            @RequestParam(name = "activo", required = false) Boolean activo,

            @Parameter(description = "Sueldo base mínimo mensual", example = "500000.0")
            @RequestParam(name = "sueldoMinimo", required = false) Double sueldoMinimo) {
        List<CocineroDTO> resultado = cocineroService.findConFiltros(texto, idCategoria, activo, sueldoMinimo);
        return ResponseEntity.ok(resultado);
    }

    @Operation(
            summary = "Obtener un cocinero por su ID",
            description = "Retorna el detalle completo de un cocinero registrado según su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocinero encontrado",
                    content = @Content(schema = @Schema(implementation = CocineroDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cocinero no encontrado con el ID indicado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CocineroDTO> getCocineroById(
            @Parameter(description = "Identificador único del cocinero", example = "1")
            @PathVariable(name = "id") Long id) {
        return cocineroService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar cocineros por ID de categoría",
            description = "Retorna todos los cocineros que pertenecen a una categoría gastronómica determinada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cocineros de la categoría indicada",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CocineroDTO.class))))
    })
    @GetMapping(value = "/categoria/{idCategoria}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CocineroDTO>> getCocinerosPorCategoria(
            @Parameter(description = "ID de la categoría de cocinero", example = "1")
            @PathVariable(name = "idCategoria") Long idCategoria) {
        return ResponseEntity.ok(cocineroService.findByCategoria(idCategoria));
    }

    @Operation(
            summary = "Dar de alta un nuevo cocinero",
            description = "Registra un nuevo cocinero en el predio gastronómico. Requiere que se asigne una categoría existente (consultar /api/v1/categorias-cocinero). Valida que sea mayor de 18 años, que el DNI y el email sean únicos. Genera automáticamente un usuario con rol EMPLEADO y envía las credenciales por correo electrónico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cocinero dado de alta exitosamente con sus credenciales creadas y enviadas por email",
                    content = @Content(schema = @Schema(implementation = CocineroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (menor de edad, DNI/email duplicado, categoría no existente o campos obligatorios faltantes)")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCocinero(@Valid @RequestBody CocineroDTO dto) {
        // Garantiza que se trate de una inserción nueva autoincrementable y no una modificación accidental
        dto.setIdPersonal(null);
        try {
            CocineroDTO creado = cocineroService.insertOrUpdate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Actualizar datos de un cocinero",
            description = "Actualiza los datos personales y laborales de un cocinero existente. Valida mayoría de edad y DNI único si fue modificado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocinero actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = CocineroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o inconsistentes"),
            @ApiResponse(responseCode = "404", description = "Cocinero no encontrado con el ID indicado")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCocinero(
            @Parameter(description = "Identificador único del cocinero a modificar", example = "1")
            @PathVariable(name = "id") Long id,
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

    @Operation(
            summary = "Dar de baja lógica a un cocinero",
            description = "Marca como inactivo al cocinero y deshabilita su usuario de credenciales en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocinero dado de baja lógicamente"),
            @ApiResponse(responseCode = "404", description = "Cocinero no encontrado")
    })
    @PatchMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(
            @Parameter(description = "Identificador único del cocinero a dar de baja", example = "1")
            @PathVariable(name = "id") Long id) {
        if (cocineroService.darDeBajaLogica(id)) {
            return ResponseEntity.ok("Cocinero dado de baja lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Reactivar a un cocinero dado de baja",
            description = "Marca como activo nuevamente al cocinero y reactiva sus credenciales en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocinero reactivado lógicamente"),
            @ApiResponse(responseCode = "404", description = "Cocinero no encontrado")
    })
    @PatchMapping("/{id}/alta")
    public ResponseEntity<?> darDeAlta(
            @Parameter(description = "Identificador único del cocinero a reactivar", example = "1")
            @PathVariable(name = "id") Long id) {
        if (cocineroService.darDeAltaLogica(id)) {
            return ResponseEntity.ok("Cocinero reactivado lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Eliminar físicamente a un cocinero",
            description = "Elimina permanentemente el registro del cocinero si no tiene restricciones de integridad referencial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocinero eliminado con éxito"),
            @ApiResponse(responseCode = "404", description = "Cocinero no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCocinero(
            @Parameter(description = "Identificador único del cocinero a eliminar", example = "1")
            @PathVariable(name = "id") Long id) {
        if (cocineroService.remove(id)) {
            return ResponseEntity.ok("Cocinero eliminado con éxito");
        }
        return ResponseEntity.notFound().build();
    }
}
