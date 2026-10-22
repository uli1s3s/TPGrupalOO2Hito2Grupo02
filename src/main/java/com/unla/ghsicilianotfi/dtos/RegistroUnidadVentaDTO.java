package com.unla.ghsicilianotfi.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistroUnidadVentaDTO {

    // ===== Datos comunes de la unidad =====

    @NotBlank(message = "Selecciona el tipo de unidad")
    @Pattern(regexp = "FOOD_TRUCK|PUESTO_DESMONTABLE",
             message = "El tipo de unidad no es valido")
    private String tipoUnidad;

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombreComercial;

    @NotNull(message = "La superficie es obligatoria")
    @DecimalMin(value = "0.01", message = "La superficie debe ser mayor a cero")
    private Float superficieM2;

    @NotBlank(message = "El código único es obligatorio")
    @Size(max = 10, message = "El código unico admite hasta 10 caracteres")
    private String codigoUnico;

    // ===== Datos especificos de FoodTruck =====

    private String patente;

    private Boolean electricidad;

    // ===== Datos especificos de PuestoDesmontable =====

    @Min(value = 1, message = "La cantidad de carpas debe ser al menos 1")
    private Integer cantidadCarpas;

    @Min(value = 1, message = "El tiempo de montaje debe ser al menos 1 minuto")
    private Integer tiempoMontajeMinutos;

    // ===== Datos del usuario responsable =====

    @NotBlank(message = "El nombre de usuario del responsable es obligatorio")
    private String nombreResponsable;

    @NotBlank(message = "El email del responsable es obligatorio")
    @Email(message = "Ingresá un email válido")
    private String emailResponsable;

    @NotBlank(message = "La contraseña del responsable es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasenaResponsable;
}