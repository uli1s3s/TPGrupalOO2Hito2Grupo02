package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "food_truck")
@PrimaryKeyJoinColumn(name = "id_unidad")
@Getter
@Setter
@NoArgsConstructor
public class FoodTruck extends UnidadVenta {

    @Column(nullable = false)
    private String patente;

    @Column(nullable = false)
    private Boolean electricidad;
}