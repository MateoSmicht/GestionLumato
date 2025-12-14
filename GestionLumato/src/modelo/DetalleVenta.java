package modelo;

import java.math.BigDecimal;

public class DetalleVenta {
    private Producto producto;
    private int cantidad; // Cantidad de "Bultos" o "Unidades" según corresponda
    
    // Snapshots (Copias de los datos al momento de la venta)
    private String nombreUnidadSnapshot;
    private int factorSnapshot;
    private BigDecimal precioUnitarioSnapshot;
    private String codigoLeido;

    public DetalleVenta(Producto producto, int cantidad, boolean esBulto, String codigoLeido) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.codigoLeido = codigoLeido;
        
        if (esBulto) {
            // Vendemos la CAJA
            this.nombreUnidadSnapshot = producto.getNombreUnidad(); // Ej: "CAJA"
            this.factorSnapshot = producto.getFactor();             // Ej: 12
            
            // El precio unitario visual es el del BULTO (Precio Unidad * Factor)
            this.precioUnitarioSnapshot = producto.calcularPrecioFinal()
                                          .multiply(new BigDecimal(this.factorSnapshot));
        } else {
            // Vendemos SUELTO
            this.nombreUnidadSnapshot = "UNI";
            this.factorSnapshot = 1;
            this.precioUnitarioSnapshot = producto.calcularPrecioFinal();
        }
    }

    public BigDecimal calcularSubtotal() {
        return this.precioUnitarioSnapshot.multiply(new BigDecimal(cantidad));
    }
    
    // Método clave para descontar stock real (Unidades base)
    public int getCantidadUnidadesReales() {
        return this.cantidad * this.factorSnapshot;
    }

    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    // Getters
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public String getNombreUnidadSnapshot() { return nombreUnidadSnapshot; }
    public int getFactorSnapshot() { return factorSnapshot; }
    public BigDecimal getPrecioUnitarioSnapshot() { return precioUnitarioSnapshot; }
    
    public String getCodigoLeido() {
        return codigoLeido;
    }
}