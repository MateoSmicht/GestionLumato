package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Producto {
    
    private String codigoInterno;
    private String codigoBarra;
    private Categoria categoria;
    private String descripcion;
    
    // Stock SIEMPRE en la unidad más pequeña
    private int cantidadStock; 
    
    private String nombreUnidad; // "UNI", "CAJA"
    private int factor;          // Ej: 12
    
    private BigDecimal precioCosto;
    private BigDecimal porcentajeGanancia;
    private BigDecimal alicuotaIVA;

    public Producto(String codigoInterno, String codigoBarra, Categoria categoria, 
                    String descripcion, String nombreUnidad, int factor, 
                    BigDecimal precioCosto, BigDecimal porcentajeGanancia, BigDecimal alicuotaIVA) {
        
        this.codigoInterno = codigoInterno;
        this.codigoBarra = codigoBarra;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.nombreUnidad = nombreUnidad;
        
        // Si no sabés el factor, ponés 1 y el sistema funciona como unidad simple
        this.factor = (factor > 0) ? factor : 1; 
        
        this.precioCosto = precioCosto;
        this.porcentajeGanancia = porcentajeGanancia;
        this.alicuotaIVA = alicuotaIVA;
        this.cantidadStock = 0;
    }

    // ... (Método calcularPrecioFinal igual que antes) ...
    public BigDecimal calcularPrecioFinal() {
        BigDecimal ganancia = this.precioCosto.multiply(this.porcentajeGanancia);
        BigDecimal precioNeto = this.precioCosto.add(ganancia);
        BigDecimal valorIVA = precioNeto.multiply(this.alicuotaIVA);
        return precioNeto.add(valorIVA).setScale(2, RoundingMode.HALF_UP);
    }
    
    // --- LÓGICA DE STOCK FLEXIBLE ---
    
    /**
     * Carga stock. 
     * @param cantidad: Cuánto entra.
     * @param esPorBulto: TRUE si cargás cajas cerradas, FALSE si cargás sueltos.
     */
    public void agregarStock(int cantidad, boolean esPorBulto) {
        if (cantidad > 0) {
            int cantidadReal;
            
            if (esPorBulto) {
                // Entran cajas: multiplicamos por el factor
                cantidadReal = cantidad * this.factor; 
            } else {
                // Entran sueltos: sumamos directo
                cantidadReal = cantidad;
            }
            
            this.cantidadStock += cantidadReal;
        }
    }
    
    /**
     * Descuenta stock (Venta).
     * @param cantidadVenta: Cuánto sale.
     * @param esPorBulto: TRUE si vendés la caja cerrada, FALSE si vendés unidad suelta.
     */
    public void descontarStock(int cantidadVenta, boolean esPorBulto) {
        int cantidadReal;
        
        if (esPorBulto) {
            cantidadReal = cantidadVenta * this.factor;
        } else {
            cantidadReal = cantidadVenta;
        }
        
        if (cantidadReal <= this.cantidadStock) {
            this.cantidadStock -= cantidadReal;
        } else {
            throw new IllegalArgumentException("Stock insuficiente. Faltan unidades.");
        }
    }
    
    // ... (Getters y toString iguales) ...
    public String getCodigoInterno() { return codigoInterno; }
    public String getCodigoBarra() { return codigoBarra; }
    public String getDescripcion() { return descripcion; }
    public int getCantidadStock() { return cantidadStock; }
    public String getNombreUnidad() { return nombreUnidad; }
    public int getFactor() { return factor; }
    public BigDecimal getPrecio() {
    	return this.calcularPrecioFinal();
    }

    @Override
    public String toString() {
        // Esto define cómo se ve el producto en las listas desplegables
        return descripcion + "  |  $ " + calcularPrecioFinal();
    }
}