package com.unla.ghsicilianotfi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para la gestión y transferencia de datos de Cocineros")
public class CocineroDTO {

    @Schema(description = "Identificador único del personal (autoincremental, omitir al crear)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idPersonal;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre del cocinero", example = "Carlos")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Schema(description = "Apellido del cocinero", example = "Gómez")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Schema(description = "Número de DNI único del cocinero", example = "35444888")
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Schema(description = "Fecha de nacimiento (debe ser mayor de 18 años)", example = "1995-04-12")
    private LocalDate fechaNacimiento;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @Schema(description = "Fecha de ingreso a la empresa", example = "2024-01-15")
    private LocalDate fechaIngreso;

    @Positive(message = "El sueldo base debe ser mayor a 0")
    @Schema(description = "Sueldo base mensual", example = "850000.0")
    private double sueldoBase;

    @Schema(description = "Indica si el cocinero se encuentra activo laboralmente", example = "true")
    private boolean activo = true;

    @Schema(description = "Especialidad gastronómica del cocinero", example = "Parrilla")
    private String especialidad;

    @NotNull(message = "La categoría de cocinero es obligatoria")
    @Schema(description = "ID de la categoría de cocinero (ver /api/v1/categorias-cocinero)", example = "1")
    private Long idCategoria;

    @Schema(description = "Nombre descriptivo de la categoría asignada", example = "AYUDANTE", accessMode = Schema.AccessMode.READ_ONLY)
    private String nombreCategoria;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Schema(description = "Correo electrónico institucional o personal para envío de credenciales", example = "carlos.gomez@epicentro.com")
    private String email;

    @Schema(description = "ID del usuario de credenciales asociado", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idUsuario;
}
