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

    public String agregarPorInput(String entrada, boolean esBulto) throws Exception {
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

        // Validar Stock (Calculando unidades reales totales)
        int factorReal = esBulto ? p.getFactor() : 1;
        int demandaTotal = cantidad * factorReal;
        
        if (p.getCantidadStock() < demandaTotal) {
            throw new Exception("Stock insuficiente. Disponibles: " + p.getCantidadStock() + " unidades.");
        }

        ventaActual.agregarItem(p, cantidad, esBulto);
        return "OK";
    }

    // MÉTODO MODIFICADO: Descuenta stock usando las unidades reales
    public void finalizarVenta() {
        for (DetalleVenta detalle : ventaActual.getItems()) {
            // Usamos el método nuevo que calcula (Cant * Factor)
            int cantidadADescontar = detalle.getCantidadUnidadesReales();
            
            // Descontamos directo (ya validamos antes)
            // Nota: usamos false porque ya calculamos la cantidad real nosotros
            detalle.getProducto().descontarStock(cantidadADescontar, false); 
        }
        empresa.registrarVenta(ventaActual);
        if (vendedor != null) empresa.borrarVentaPendiente(vendedor);
    }

    public void eliminarItem(int indice) {
        if (indice >= 0 && indice < ventaActual.getItems().size()) {
            DetalleVenta d = ventaActual.getItems().get(indice);
            ventaActual.eliminarItem(d);
        }
    }


    public void modificarCantidadItem(int indice, int nuevaCantidad) throws Exception {
        if (indice < 0 || indice >= ventaActual.getItems().size()) return;
        
        if (nuevaCantidad <= 0) {
            throw new Exception("La cantidad debe ser mayor a 0. Use 'Eliminar' para borrar.");
        }

        DetalleVenta detalle = ventaActual.getItems().get(indice);
        Producto p = detalle.getProducto();

        // Validar Stock (Lectura)
        if (p.getCantidadStock() < nuevaCantidad) {
            throw new Exception("Stock insuficiente. Disponible: " + p.getCantidadStock());
        }

        // Si pasa, actualizamos
        detalle.setCantidad(nuevaCantidad);
        
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        for(DetalleVenta d : ventaActual.getItems()) {
            nuevoTotal = nuevoTotal.add(d.calcularSubtotal());
        }
       
        ventaActual.recalcularTotal(); 
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
