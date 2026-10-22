package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cocinero")
@PrimaryKeyJoinColumn(name = "id_personal")
@Getter
@Setter
@NoArgsConstructor
public class Cocinero extends Personal {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaCocinero categoria;

    @Column(nullable = true)
    private String especialidad;

    public Cocinero(String nombre, String apellido, String dni,
                    LocalDate fechaNacimiento, LocalDate fechaIngreso, double sueldoBase,
                    Usuario credenciales, CategoriaCocinero categoria, String especialidad) {
        super(nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldoBase, credenciales);
        this.categoria = categoria;
        this.especialidad = especialidad;
    }

    public Cocinero(Long idPersonal, String nombre, String apellido, String dni,
                    LocalDate fechaNacimiento, LocalDate fechaIngreso, double sueldoBase,
                    boolean activo, Usuario credenciales, CategoriaCocinero categoria, String especialidad) {
        super(idPersonal, nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldoBase, activo, credenciales);
        this.categoria = categoria;
        this.especialidad = especialidad;
    }
}

