package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "costo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_costo_festival_tipo",
                columnNames = {"festival_id", "tipo_costo"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Costo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCosto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_costo", nullable = false, length = 30)
    private TipoCostoFestival tipoCosto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidad_aplicable", nullable = false, length = 30)
    private TipoUnidadAplicable tipoUnidadAplicable;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
