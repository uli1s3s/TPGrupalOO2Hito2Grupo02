package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plato")
@Getter
@Setter
@NoArgsConstructor
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlato;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private double precioVenta;

    @Column(nullable = false)
    private double costoProduccion;

    @Column(nullable = false)
    private boolean activo = true;
    

}