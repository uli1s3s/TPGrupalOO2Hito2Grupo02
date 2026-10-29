package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "festival")
@Getter
@Setter
@NoArgsConstructor
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFestival;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String temporada;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(
            mappedBy = "festival",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Costo> costos = new ArrayList<>();

    @ManyToMany(mappedBy = "festivales")
    private Set<UnidadVenta> unidades = new HashSet<>();

    @OneToMany(mappedBy = "festival")
    private List<Pedido> pedidos = new ArrayList<>();

    // Duración total del festival en días (inclusivo)
    public int getDuracionDias() {
        if (fechaInicio == null || fechaFin == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
    }

    // Nombre sin anotaciones de duración entre paréntesis
    public String getNombreLimpio() {
        if (nombre == null) {
            return "";
        }
        return nombre.replaceAll("\\s*\\([^)]*\\)", "").trim();
    }
}
