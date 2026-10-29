
package com.unla.ghsicilianotfi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_pedido")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long idDetalle;

    @Column(nullable = false)
    protected int cantidad;

    // Precio de venta al momento de registrar el pedido
    @Column(nullable = false)
    protected double precioUnitario;

    // Costo de producción al momento de registrar el pedido
    @Column(nullable = false)
    protected double costoUnitario;

    // Un detalle pertenece a un pedido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    protected Pedido pedido;

    // Un detalle corresponde a un plato
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plato_id", nullable = false)
    protected Plato plato;

    // Constructor sin ID
    public DetallePedido(int cantidad, Pedido pedido, Plato plato) {
        this.cantidad = cantidad;
        this.pedido = pedido;
        this.plato = plato;

        if (plato != null) {
            this.precioUnitario = plato.getPrecioVenta();
            this.costoUnitario = plato.getCostoProduccion();
        }
    }

    // Constructor con ID
    public DetallePedido(Long idDetalle, int cantidad,
                         Pedido pedido, Plato plato) {
        this(cantidad, pedido, plato);
        this.idDetalle = idDetalle;
    }
}
