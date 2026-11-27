package com.unla.ghsicilianotfi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para la gestión y transferencia de datos de Cajeros")
public class CajeroDTO {

    @Schema(description = "Identificador único del personal (autoincremental, omitir al crear)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idPersonal;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre del cajero", example = "Laura")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Schema(description = "Apellido del cajero", example = "Pérez")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Schema(description = "Número de DNI único", example = "12345678")
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Schema(description = "Fecha de nacimiento (debe ser mayor a 18 años)", example = "1990-01-01")
    private LocalDate fechaNacimiento;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @Schema(description = "Fecha de ingreso a la empresa", example = "2020-01-01")
    private LocalDate fechaIngreso;

    @Positive(message = "El sueldo base debe ser mayor a 0")
    @Schema(description = "Sueldo base mensual", example = "700000.0")
    private double sueldoBase;

    @Schema(description = "Indica si el cajero se encuentra activo laboralmente", example = "true")
    private boolean activo = true;

    @NotBlank(message = "El turno es obligatorio")
    @Schema(description = "Turno laboral asignado (MAÑANA, TARDE o NOCHE)", example = "MAÑANA")
    private String turno;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Schema(description = "Correo electrónico para envío de credenciales", example = "laura.fernandez@epicentro.com")
    private String email;

    @Schema(description = "ID del usuario de credenciales asignado", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idUsuario;
}

