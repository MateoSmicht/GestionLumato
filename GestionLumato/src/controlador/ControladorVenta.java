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

    public String agregarPorInput(String entrada) throws Exception {
        if (entrada.isEmpty()) return "";
        String codigo = entrada;
        int cantidad = 1;

        if (entrada.contains("*")) {
            String[] partes = entrada.split("\\*");
            cantidad = Integer.parseInt(partes[0]);
            codigo = partes[1];
        }

        Producto p = empresa.buscarProducto(codigo);
        if (p == null) throw new Exception("Producto no encontrado: " + codigo);

        // --- VALIDACIÓN DE STOCK (LECTURA) ---
        // Verificamos si alcanza, pero NO descontamos todavía.
        // Nota: Para ser perfecto, deberíamos sumar la cantidad que ya pusimos en el carrito
        // pero para este nivel, validar contra el stock actual está bien.
        if (p.getCantidadStock() < cantidad) {
            throw new Exception("Stock insuficiente. Hay: " + p.getCantidadStock());
        }

        ventaActual.agregarItem(p, cantidad);
        return "OK";
    }

    public void eliminarItem(int indice) {
        if (indice >= 0 && indice < ventaActual.getItems().size()) {
            DetalleVenta d = ventaActual.getItems().get(indice);
            ventaActual.eliminarItem(d);
        }
    }

 

    public BigDecimal calcularVuelto(BigDecimal pago) {
        BigDecimal total = ventaActual.getTotal();
        if (pago.compareTo(total) < 0) throw new IllegalArgumentException("Insuficiente");
        return pago.subtract(total);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return empresa.buscarProductosPorNombre(nombre);
    }

    public Producto buscarPorCodigo(String codigo) {
        return empresa.buscarProducto(codigo);
    }
    
    public void finalizarVenta() {
        // 1. Descontar Stock
        for (DetalleVenta detalle : ventaActual.getItems()) {
            detalle.getProducto().descontarStock(detalle.getCantidad());
        }
        // 2. Registrar
        empresa.registrarVenta(ventaActual);
        
        // 3. Borrar el pendiente de ESTE vendedor (si había)
        empresa.borrarVentaPendiente(vendedor);
    }

    public void guardarVentaEnEspera() {
        if (!ventaActual.getItems().isEmpty()) {
            // Guardamos usando al vendedor como clave
            empresa.setVentaPendiente(vendedor, ventaActual);
        }
    }

    public boolean existeVentaPendiente() {
        // Preguntamos por ESTE vendedor
        return empresa.hayVentaPendiente(vendedor);
    }

    public void restaurarVentaPendiente() {
        // Recuperamos la de ESTE vendedor
        Venta recuperada = empresa.getVentaPendiente(vendedor);
        if (recuperada != null) {
            this.ventaActual = recuperada;
        }
    }

    public void descartarVentaPendiente() {
        // Borramos la de ESTE vendedor
        empresa.borrarVentaPendiente(vendedor);
        nuevaVenta();
    }

}
