package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private static long contadorId = 1;
    private long id;
    private LocalDateTime fechaHora;
    private Usuario vendedor;
    private List<DetalleVenta> items;
    private BigDecimal total;
    //Facturacion
    public Venta(Usuario vendedor) {
        this.id = contadorId++;
        this.fechaHora = LocalDateTime.now();
        this.vendedor = vendedor;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }



    public void agregarItem(Producto producto, int cantidad, boolean esBulto) {
        // Creamos el detalle pasando el flag esBulto
        DetalleVenta detalle = new DetalleVenta(producto, cantidad, esBulto);
        
        this.items.add(detalle);
        this.total = this.total.add(detalle.calcularSubtotal());
    }
    
    public void eliminarItem(DetalleVenta detalle) {
        if (this.items.contains(detalle)) {
            this.total = this.total.subtract(detalle.calcularSubtotal());
            this.items.remove(detalle);
        }
    }
 
    public void recalcularTotal() {
        this.total = BigDecimal.ZERO;
        for (DetalleVenta d : this.items) {
            this.total = this.total.add(d.calcularSubtotal());
        }
    }
    public long getId() { return id; }
    public BigDecimal getTotal() { return total; }
    public Usuario getVendedor() { return vendedor; }
    public List<DetalleVenta> getItems() { return items; }
}