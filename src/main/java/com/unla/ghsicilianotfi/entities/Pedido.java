package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long idPedido;

    @Column(nullable = false)
    protected LocalDate fechaPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    protected Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id")
    protected UnidadVenta unidadVenta;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    protected List<DetallePedido> detalles = new ArrayList<>();

    // Constructor sin ID
    public Pedido(LocalDate fechaPedido, List<DetallePedido> detalles) {
        this.fechaPedido = fechaPedido;
        this.detalles = new ArrayList<>();

        if (detalles != null) {
            for (DetallePedido detalle : detalles) {
                detalle.setPedido(this);
                this.detalles.add(detalle);
            }
        }
    }

    // Constructor con ID
    public Pedido(Long idPedido, LocalDate fechaPedido,
                  List<DetallePedido> detalles) {
        this.idPedido = idPedido;
        this.fechaPedido = fechaPedido;
        this.detalles = new ArrayList<>();

        if (detalles != null) {
            for (DetallePedido detalle : detalles) {
                detalle.setPedido(this);
                this.detalles.add(detalle);
            }
        }
    }

    // Constructor que recibe solamente la fecha del pedido
    public Pedido(LocalDate fechaPedido) {
        this.fechaPedido = fechaPedido;
    }


    @Transient
    public double getTotal() {

        double total = 0;

        for (DetallePedido detalle : detalles) {
            total += detalle.getCantidad()
                    * detalle.getPrecioUnitario();
        }

        return total;
    }


    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        this.detalles.add(detalle);
    }

    public void quitarDetalle(DetallePedido detalle) {
        if (this.detalles.remove(detalle)) {
            detalle.setPedido(null);
        }
    }
}