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
     * Agrega un producto a la venta actual.
     * Soporta formato "Cantidad*Codigo" (ej: "3*7791234")
     */
    public String agregarPorInput(String entrada, boolean esBulto) throws Exception {
        if (entrada == null || entrada.trim().isEmpty()) return "";
        
        String codigoInput = entrada;
        int cantidad = 1;

        // 1. Parsear "Cantidad*Codigo"
        if (entrada.contains("*")) {
            try {
                String[] partes = entrada.split("\\*");
                cantidad = Integer.parseInt(partes[0]);
                codigoInput = partes[1]; // El código real escaneado
            } catch (NumberFormatException e) {
                throw new Exception("Formato inválido. Use 'CANTIDAD*CODIGO'");
            }
        }

        // 2. Buscar Producto (Por Barra o por Interno)
        // El mapa de empresa ya resuelve el aliasing automáticamente
        Producto p = empresa.buscarProducto(codigoInput);
        

        if (p == null) {
            throw new Exception("Producto no encontrado: " + codigoInput);
        }

        // 3. Validar Stock
        int factorReal = esBulto ? p.getFactor() : 1;
        int demandaTotalEnUnidades = cantidad * factorReal;
        
        if (p.getCantidadStock() < demandaTotalEnUnidades) {
            throw new Exception("Stock insuficiente. Disponibles: " + p.getCantidadStock() + " unidades.");
        }

        // 4. Agregar al carrito
        // IMPORTANTE: Pasamos 'codigoInput' para que el ticket muestre exactamente lo que se leyó
        ventaActual.agregarItem(p, cantidad, esBulto, codigoInput);
        
        return "OK";
    }

    public void finalizarVenta() {
        for (DetalleVenta detalle : ventaActual.getItems()) {
            // Usamos el método que calcula (Cant * Factor)
            int cantidadADescontar = detalle.getCantidadUnidadesReales();
            
            // Descontamos directo (ya validamos antes)
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
        if (pago.compareTo(total) < 0) throw new IllegalArgumentException("Pago Insuficiente");
        return pago.subtract(total);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return empresa.buscarProductosPorNombre(nombre);
    }

    public Producto buscarPorCodigo(String codigo) {
        return empresa.buscarProducto(codigo);
    }
    
    // --- GESTIÓN DE VENTAS EN ESPERA ---

    public void guardarVentaEnEspera() {
        if (!ventaActual.getItems().isEmpty()) {
            empresa.setVentaPendiente(vendedor, ventaActual);
        }
    }

    public boolean existeVentaPendiente() {
        return empresa.hayVentaPendiente(vendedor);
    }

    public void restaurarVentaPendiente() {
        Venta recuperada = empresa.getVentaPendiente(vendedor);
        if (recuperada != null) {
            this.ventaActual = recuperada;
        }
    }

    public void descartarVentaPendiente() {
        empresa.borrarVentaPendiente(vendedor);
        nuevaVenta();
    }
}
