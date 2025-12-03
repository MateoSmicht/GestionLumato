package modelo;

import java.math.BigDecimal;

public class DetalleVenta {
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitarioSnapshot;

    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitarioSnapshot = producto.calcularPrecioFinal();
    }

    public BigDecimal calcularSubtotal() {
        return this.precioUnitarioSnapshot.multiply(new BigDecimal(cantidad));
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    
    @Override
    public String toString() {
        return producto.getDescripcion() + " x" + cantidad + " ($" + calcularSubtotal() + ")";
    }
}