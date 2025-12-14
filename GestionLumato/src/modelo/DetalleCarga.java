package modelo;

public class DetalleCarga {
    private Producto producto;
    private int cantidad;
    private boolean esBulto;
    private String codigoLeido; 

    // Constructor actualizado
    public DetalleCarga(Producto producto, int cantidad, boolean esBulto, String codigoLeido) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.esBulto = esBulto;
        this.codigoLeido = codigoLeido;
    }

    // --- Lógica de Negocio ---
    public int getUnidadesReales() {
        return esBulto ? cantidad * producto.getFactor() : cantidad;
    }

    // Getters y Setters
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public boolean isEsBulto() { return esBulto; }
 
    public String getCodigoLeido() { return codigoLeido; }
}