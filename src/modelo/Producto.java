package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Producto {
    
    private String codigoInterno;
    private String codigoBarra;
    private Categoria categoria;
    private String descripcion;
    private int cantidadStock; 
    private String nombreUnidad; 
    private int factor;          
    private BigDecimal precioCosto;
    private BigDecimal ppp;
    private BigDecimal porcentajeGanancia;
    private BigDecimal alicuotaIVA;
    private List<String> codigosSecundarios;

    public Producto(String codigoInterno, String codigoBarra, Categoria categoria, 
                    String descripcion, String nombreUnidad, int factor, 
                    BigDecimal precioCosto, BigDecimal porcentajeGanancia, BigDecimal alicuotaIVA) {
        
        this.codigoInterno = codigoInterno;
        this.codigoBarra = codigoBarra;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.nombreUnidad = nombreUnidad;
        this.factor = (factor > 0) ? factor : 1; 
        this.precioCosto = precioCosto;
        this.ppp = precioCosto;
        this.porcentajeGanancia = porcentajeGanancia;
        this.alicuotaIVA = alicuotaIVA;
        this.cantidadStock = 0;
        this.codigosSecundarios = new ArrayList<>();
    }
    
    
    //Dado porcenje de ganancia calcula el precio final de venta.
    public BigDecimal calcularPrecioFinal() {
        try {
            BigDecimal costo = this.precioCosto;
            BigDecimal porcentajeGanancia = this.porcentajeGanancia;
            BigDecimal alicuotaIVA = this.alicuotaIVA;

            BigDecimal gananciaDinero = costo.multiply(porcentajeGanancia);
            BigDecimal precioNeto = costo.add(gananciaDinero);
            BigDecimal ivaDinero = precioNeto.multiply(alicuotaIVA);
            
            return precioNeto.add(ivaDinero).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException | ArithmeticException e) {
            return BigDecimal.ZERO; // Si faltan datos o son inválidos
        }
    }
    
 
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
    
    public void agregarCodigoSecundario(String codigo) {
        if (!codigo.equals(this.codigoBarra) && !codigosSecundarios.contains(codigo)) {
            codigosSecundarios.add(codigo);
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
    public BigDecimal getPrecioCosto() {
    	return this.precioCosto;
    }
    public BigDecimal getPorcentajeGanancia() {
    	return this.porcentajeGanancia;
    }
    public BigDecimal getAlicuotaIVA() {
    	return this.alicuotaIVA;
    }
    public Categoria getCategoria() { return categoria;}
    
    public List<String> getCodigosSecundarios() {
        return codigosSecundarios;
    }
    
    public BigDecimal getPpp() {
        return (ppp != null) ? ppp : precioCosto;
    }

    public void setPpp(BigDecimal ppp) {
        this.ppp = ppp;
    }
    public void setCantidadStock(int cStock) {
        this.cantidadStock = cStock;
    }
    
    public void setPrecioCosto(BigDecimal precioCosto) { this.precioCosto = precioCosto; }
    public void setPorcentajeGanancia(BigDecimal porcentajeGanancia) { this.porcentajeGanancia = porcentajeGanancia; }
    public void setAlicuotaIVA(BigDecimal alicuotaIVA) { this.alicuotaIVA = alicuotaIVA; }

    @Override
    public String toString() {
        // Esto define cómo se ve el producto en las listas desplegables
        return descripcion + "  |  $ " + calcularPrecioFinal();
    }
}