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

    public void agregarItem(Producto producto, int cantidad) {
        if (producto.getCantidadStock() >= cantidad) {
            producto.descontarStock(cantidad);
            DetalleVenta detalle = new DetalleVenta(producto, cantidad);
            this.items.add(detalle);
            this.total = this.total.add(detalle.calcularSubtotal());
        } else {
            System.out.println("Error: Stock insuficiente");
        }
    }
    public void eliminarItem(DetalleVenta detalle) {
        if (this.items.contains(detalle)) {
            this.total = this.total.subtract(detalle.calcularSubtotal());
            detalle.getProducto().agregarStock(detalle.getCantidad());
            this.items.remove(detalle);
        }
    }
    public void agregarCantidadAlProducto(DetalleVenta detalle) {
    	
    }
    public long getId() { return id; }
    public BigDecimal getTotal() { return total; }
    public Usuario getVendedor() { return vendedor; }
    public List<DetalleVenta> getItems() { return items; }
}