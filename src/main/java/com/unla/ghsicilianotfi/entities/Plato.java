
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

    // Constructor sin ID
    public Plato(String nombre, double precioVenta,
                 double costoProduccion) {

        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.costoProduccion = costoProduccion;
        this.activo = true;
    }

    // Constructor con ID
    public Plato(Long idPlato, String nombre,
                 double precioVenta, double costoProduccion,
                 boolean activo) {

        this.idPlato = idPlato;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.costoProduccion = costoProduccion;
        this.activo = activo;
    }
}
