package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

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
    
    // Empleados de la unidad. Cada empleado pertenece a una sola unidad.
    // La columna id_unidad queda en la tabla personal y es opcional.
    @OneToMany
    @JoinColumn(name = "id_unidad")
    private Set<Personal> empleados = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "festival_unidad",
            joinColumns = @JoinColumn(name = "id_unidad"),
            inverseJoinColumns = @JoinColumn(name = "id_festival")
    )
    private Set<Festival> festivales = new HashSet<>();
    
    // polimorfismo
    public abstract String getTipo();

    // Cada subclase describe sus datos propios
    public abstract String getDetalle();
    
    // Prefijo del código único según el tipo de unidad (FT o PD)
    public abstract String getPrefijoCodigo();
    
}