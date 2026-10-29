package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import com.unla.ghsicilianotfi.configuration.ParametrosCanon;
import java.math.BigDecimal;

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
    
    // Prefijo del código unico según el tipo de unidad (FT o PD)
    public abstract String getPrefijoCodigo();
    
    // Cada tipo de unidad calcula su canon con su propia formula (polimorfismo)
    public abstract BigDecimal calcularCanon(ParametrosCanon parametros);

    // Superficie como BigDecimal, para hacer cuentas de dinero sin errores de redondeo
    protected BigDecimal getSuperficieDecimal() {
        return new BigDecimal(Float.toString(superficieM2));
    }
    
}