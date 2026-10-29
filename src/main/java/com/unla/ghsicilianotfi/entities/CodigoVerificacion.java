package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Codigo de 6 digitos que se envia por mail al responsable que se registra,
 * para confirmar que el email es suyo antes de activar la cuenta
 */
@Entity
@Table(name = "codigo_verificacion")
@Getter
@Setter
@NoArgsConstructor
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCodigo;

    // Usuario al que pertenece el codigo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 6)
    private String codigo;

    // Fecha y hora hasta la que el codigo es válido
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    // Cantidad de veces que se ingreso un codigo incorrecto
    @Column(nullable = false)
    private int intentos = 0;

    // true cuando ya se uso (o se anulo al pedir uno nuevo)
    @Column(nullable = false)
    private boolean utilizado = false;

    public boolean estaVencido() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
}