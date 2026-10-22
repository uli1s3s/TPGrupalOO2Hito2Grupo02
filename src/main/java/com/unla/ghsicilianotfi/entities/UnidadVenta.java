package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unidad_venta")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class UnidadVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUnidad;

    @Column(nullable = false)
    private String nombreComercial;

    @Column(nullable = false)
    private Float superficieM2;

    @Column(nullable = false, unique = true, length = 10)
    private String codigoUnico;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Usuario responsable;
}