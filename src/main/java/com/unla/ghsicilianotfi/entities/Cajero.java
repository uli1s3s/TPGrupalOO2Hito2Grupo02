package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cajero")
@PrimaryKeyJoinColumn(name = "id_personal")
@Getter
@Setter
@NoArgsConstructor
public class Cajero extends Personal {

    @Column(nullable = false)
    private String turno;

    public Cajero(String nombre, String apellido, String dni,
                  LocalDate fechaNacimiento, LocalDate fechaIngreso, double sueldoBase,
                  Usuario credenciales, String turno) {
        super(nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldoBase, credenciales);
        this.turno = turno;
    }

    public Cajero(Long idPersonal, String nombre, String apellido, String dni,
                  LocalDate fechaNacimiento, LocalDate fechaIngreso, double sueldoBase,
                  boolean activo, Usuario credenciales, String turno) {
        super(idPersonal, nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldoBase, activo, credenciales);
        this.turno = turno;
    }
}

