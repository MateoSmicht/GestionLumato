package modelo;

public class DetalleCarga {
    private Producto producto;
    private int cantidad;
    private boolean esBulto;

    public DetalleCarga(Producto producto, int cantidad, boolean esBulto) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.esBulto = esBulto;
    }

    // --- Lógica de Negocio propia del detalle ---
    
    public int getUnidadesReales() {
        return esBulto ? cantidad * producto.getFactor() : cantidad;
    }

    // Getters y Setters
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public boolean isEsBulto() { return esBulto; }
}