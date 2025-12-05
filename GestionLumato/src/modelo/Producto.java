package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Producto {
    private String codigoInterno;
    private String codigoBarra;
    private Categoria categoria;
    private String descripcion;
    private int cantidadStock; 
    
    private BigDecimal precioCosto;
    private BigDecimal porcentajeGanancia;
    private BigDecimal alicuotaIVA;

    public Producto(String codigoInterno, String codigoBarra, Categoria categoria, 
                    String descripcion, BigDecimal precioCosto, 
                    BigDecimal porcentajeGanancia, BigDecimal alicuotaIVA) {
        this.codigoInterno = codigoInterno;
        this.codigoBarra = codigoBarra;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precioCosto = precioCosto;
        this.porcentajeGanancia = porcentajeGanancia;
        this.alicuotaIVA = alicuotaIVA;
        this.cantidadStock = 0;
    }

    public BigDecimal calcularPrecioFinal() {
        BigDecimal ganancia = this.precioCosto.multiply(this.porcentajeGanancia);
        BigDecimal precioNeto = this.precioCosto.add(ganancia);
        BigDecimal valorIVA = precioNeto.multiply(this.alicuotaIVA);
        return precioNeto.add(valorIVA).setScale(2, RoundingMode.HALF_UP);
    }
    
    public void agregarStock(int cantidad) { if (cantidad > 0) this.cantidadStock += cantidad; }
    
    public void descontarStock(int cantidad) {
        if (cantidad <= this.cantidadStock) this.cantidadStock -= cantidad;
        else throw new IllegalArgumentException("Stock insuficiente");
    }

    public String getCodigoInterno() { return codigoInterno; }
    public String getCodigoBarra() { return codigoBarra; }
    public String getDescripcion() { return descripcion; }
    public int getCantidadStock() { return cantidadStock; }
    public Categoria getCategoria() { return categoria; }
    
 // En modelo/Producto.java
    @Override
    public String toString() {
        // Esto es lo que se verá en la lista desplegable
        return descripcion + " - $" + calcularPrecioFinal();
    }
}