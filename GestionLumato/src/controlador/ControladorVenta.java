package controlador;

import java.math.BigDecimal;
import java.util.List;
import modelo.*;

public class ControladorVenta {

    private Empresa empresa;
    private Venta ventaActual;
    private Usuario vendedor;

    public ControladorVenta(Empresa empresa, Usuario vendedor) {
        this.empresa = empresa;
        this.vendedor = vendedor;
        this.ventaActual = new Venta(vendedor);
    }

    public void nuevaVenta() {
        this.ventaActual = new Venta(vendedor);
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    /**
     * Intenta agregar un producto parseando el texto (ej: "3*779...")
     * Retorna el mensaje de éxito o lanza excepción con el error.
     */
    public String agregarPorInput(String entrada) throws Exception {
        if (entrada.isEmpty()) return "";

        String codigoBuscado = entrada;
        int cantidad = 1;

        // Lógica de parsing
        if (entrada.contains("*")) {
            String[] partes = entrada.split("\\*");
            cantidad = Integer.parseInt(partes[0]);
            codigoBuscado = partes[1];
        }

        Producto p = empresa.buscarProducto(codigoBuscado);

        if (p != null) {
            ventaActual.agregarItem(p, cantidad);
            return "Agregado: " + cantidad + " x " + p.getDescripcion();
        } else {
            throw new Exception("Producto no encontrado: " + codigoBuscado);
        }
    }

    public void eliminarItem(int indice) {
        if (indice >= 0 && indice < ventaActual.getItems().size()) {
            DetalleVenta d = ventaActual.getItems().get(indice);
            ventaActual.eliminarItem(d);
        }
    }

    public void finalizarVenta() {
        empresa.registrarVenta(ventaActual);
    }
    
    // Método puro de lógica para el vuelto
    public BigDecimal calcularVuelto(BigDecimal pago, BigDecimal total) {
        if (pago.compareTo(total) < 0) {
            throw new IllegalArgumentException("Monto insuficiente");
        }
        return pago.subtract(total);
    }
    
    // Delegamos la búsqueda a la empresa
    public List<Producto> buscarPorNombre(String nombre) {
        return empresa.buscarProductosPorNombre(nombre);
    }
    
    public Producto buscarPorCodigo(String codigo) {
        return empresa.buscarProducto(codigo);
    }
}
