package com.unla.ghsicilianotfi.controllers.api.v1;

import com.unla.ghsicilianotfi.dtos.CajeroDTO;
import com.unla.ghsicilianotfi.services.ICajeroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cajeros")
@Tag(name = "Cajero", description = "Endpoints para la gestión integral y de consulta de cajeros del predio gastronómico")
public class CajeroRestController {

    private final ICajeroService cajeroService;

    public CajeroRestController(ICajeroService cajeroService) {
        this.cajeroService = cajeroService;
    }

    @Operation(
            summary = "Listar todos los cajeros",
            description = "Obtiene la lista completa de cajeros, con opción de incluir solo aquellos que se encuentren laboralmente activos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cajeros obtenido exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CajeroDTO.class))))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CajeroDTO>> getCajeros(
            @Parameter(description = "Si es true, lista únicamente los cajeros con estado activo", example = "false")
            @RequestParam(name = "soloActivos", required = false, defaultValue = "false") boolean soloActivos) {
        List<CajeroDTO> cajeros = soloActivos ? cajeroService.getAllActivos() : cajeroService.getAll();
        return ResponseEntity.ok(cajeros);
    }

    @Operation(
            summary = "Filtrar cajeros con criterios múltiples",
            description = "Permite buscar y filtrar cajeros combinando texto (nombre o apellido), turno, estado de actividad y sueldo base mínimo. Todos los parámetros son opcionales."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cajeros que coinciden con los filtros especificados",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CajeroDTO.class))))
    })
    @GetMapping(value = "/filtrar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CajeroDTO>> filtrarCajeros(
            @Parameter(description = "Texto a buscar en nombre o apellido (insensible a mayúsculas/minúsculas)", example = "Laura")
            @RequestParam(name = "texto", required = false) String texto,
            @Parameter(description = "Turno laboral asignado (MAÑANA, TARDE o NOCHE)", example = "MAÑANA")
            @RequestParam(name = "turno", required = false) String turno,
            @Parameter(description = "Estado de actividad laboral (true: activos, false: inactivos)", example = "true")
            @RequestParam(name = "activo", required = false) Boolean activo,
            @Parameter(description = "Sueldo base mínimo mensual", example = "500000.0")
            @RequestParam(name = "sueldoMinimo", required = false) Double sueldoMinimo) {
        return ResponseEntity.ok(cajeroService.findConFiltros(texto, turno, activo, sueldoMinimo));
    }

    @Operation(
            summary = "Obtener un cajero por su ID",
            description = "Retorna el detalle completo de un cajero registrado según su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cajero encontrado",
                    content = @Content(schema = @Schema(implementation = CajeroDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cajero no encontrado con el ID indicado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CajeroDTO> getCajeroById(
            @Parameter(description = "Identificador único del cajero", example = "1")
            @PathVariable(name = "id") Long id) {
        return cajeroService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar cajeros por turno",
            description = "Retorna todos los cajeros que trabajan en un turno determinado (MAÑANA, TARDE o NOCHE)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cajeros del turno indicado",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CajeroDTO.class))))
    })
    @GetMapping(value = "/turno/{turno}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CajeroDTO>> getCajerosPorTurno(
            @Parameter(description = "Turno laboral", example = "MAÑANA")
            @PathVariable(name = "turno") String turno) {
        return ResponseEntity.ok(cajeroService.findByTurno(turno));
    }

    @Operation(
            summary = "Dar de alta un nuevo cajero",
            description = "Registra un nuevo cajero en el predio gastronómico. Valida que sea mayor de 18 años, que el DNI y el email sean únicos. Genera automáticamente un usuario con rol EMPLEADO y envía las credenciales por correo electrónico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cajero dado de alta exitosamente con sus credenciales creadas y enviadas por email",
                    content = @Content(schema = @Schema(implementation = CajeroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (menor de edad, DNI/email duplicado o campos obligatorios faltantes)")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCajero(@Valid @RequestBody CajeroDTO dto) {
        dto.setIdPersonal(null);
        try {
            CajeroDTO creado = cajeroService.insertOrUpdate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Actualizar datos de un cajero",
            description = "Actualiza los datos personales y laborales de un cajero existente. Valida mayoría de edad y DNI único si fue modificado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cajero actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = CajeroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o inconsistentes"),
            @ApiResponse(responseCode = "404", description = "Cajero no encontrado con el ID indicado")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCajero(
            @Parameter(description = "Identificador único del cajero a modificar", example = "1")
            @PathVariable(name = "id") Long id,
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

    @Operation(
            summary = "Dar de baja lógica a un cajero",
            description = "Marca como inactivo al cajero y deshabilita su usuario de credenciales en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cajero dado de baja lógicamente"),
            @ApiResponse(responseCode = "404", description = "Cajero no encontrado")
    })
    @PatchMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(
            @Parameter(description = "Identificador único del cajero a dar de baja", example = "1")
            @PathVariable(name = "id") Long id) {
        if (cajeroService.darDeBajaLogica(id)) {
            return ResponseEntity.ok("Cajero dado de baja lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Reactivar a un cajero dado de baja",
            description = "Marca como activo nuevamente al cajero y reactiva sus credenciales en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cajero reactivado lógicamente"),
            @ApiResponse(responseCode = "404", description = "Cajero no encontrado")
    })
    @PatchMapping("/{id}/alta")
    public ResponseEntity<?> darDeAlta(
            @Parameter(description = "Identificador único del cajero a reactivar", example = "1")
            @PathVariable(name = "id") Long id) {
        if (cajeroService.darDeAltaLogica(id)) {
            return ResponseEntity.ok("Cajero reactivado lógicamente");
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Eliminar físicamente a un cajero",
            description = "Elimina permanentemente el registro del cajero si no tiene restricciones de integridad referencial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cajero eliminado con éxito"),
            @ApiResponse(responseCode = "404", description = "Cajero no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCajero(
            @Parameter(description = "Identificador único del cajero a eliminar", example = "1")
            @PathVariable(name = "id") Long id) {
        if (cajeroService.remove(id)) {
            return ResponseEntity.ok("Cajero eliminado con éxito");
        }
        return ResponseEntity.notFound().build();
    }
}

