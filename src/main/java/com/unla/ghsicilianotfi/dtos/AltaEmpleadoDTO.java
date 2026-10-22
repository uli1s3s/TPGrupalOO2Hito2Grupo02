package com.unla.ghsicilianotfi.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AltaEmpleadoDTO {

    private Long idPersonal;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    @Positive(message = "El sueldo base debe ser mayor a 0")
    private double sueldoBase = 100000.0;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String email;

    @NotBlank(message = "Debe seleccionar si es Cocinero o Cajero")
    private String tipoEmpleado = "COCINERO"; // "COCINERO" o "CAJERO"

    // Campos de Cocinero
    private Long idCategoria;
    private String nombreCategoria;
    private String especialidad;

    // Campos de Cajero
    private String turno = "MAÑANA"; // "MAÑANA" o "NOCHE"

    private boolean activo = true;

    // Métodos utilitarios para Thymeleaf
    public String getDetalleRol() {
        if ("COCINERO".equalsIgnoreCase(tipoEmpleado)) {
            String cat = (nombreCategoria != null && !nombreCategoria.isBlank()) ? nombreCategoria : "Sin categoría";
            String esp = (especialidad != null && !especialidad.isBlank()) ? " (" + especialidad + ")" : "";
            return cat + esp;
        } else if ("CAJERO".equalsIgnoreCase(tipoEmpleado)) {
            return "Turno: " + (turno != null ? turno : "Mañana");
        }
        return "-";
    }
}

