package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "personal")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long idPersonal;

    @Column(nullable = false)
    protected String nombre;

    @Column(nullable = false)
    protected String apellido;

    @Column(nullable = false, unique = true)
    protected String dni;

    @Column(nullable = false)
    protected LocalDate fechaNacimiento;

    @Column(nullable = false)
    protected LocalDate fechaIngreso;

    @Column(nullable = false)
    protected double sueldoBase;

    @Column(nullable = false)
    protected boolean activo = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    protected Usuario credenciales;

    public Personal(String nombre, String apellido, String dni,
                    LocalDate fechaNacimiento, LocalDate fechaIngreso, double sueldoBase,
                    Usuario credenciales) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaIngreso = fechaIngreso;
        this.sueldoBase = sueldoBase;
        this.credenciales = credenciales;
        this.activo = true;
    }

    public Personal(Long idPersonal, String nombre, String apellido, String dni,
                    LocalDate fechaNacimiento, LocalDate fechaIngreso, double sueldoBase,
                    boolean activo, Usuario credenciales) {
        this.idPersonal = idPersonal;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaIngreso = fechaIngreso;
        this.sueldoBase = sueldoBase;
        this.activo = activo;
        this.credenciales = credenciales;
    }
}

