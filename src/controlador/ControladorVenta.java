package controlador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import modelo.*;
import persistencia.RepositorioProducto;
import persistencia.RepositorioVenta;

public class ControladorVenta {

    // --- DEPENDENCIAS ---
    private RepositorioProducto repoProducto;
    private RepositorioVenta repoVenta;
    
    // --- ESTADO DE LA SESIÓN ---
    private Venta ventaActual;
    private Usuario vendedor;
    
    // --- MEMORIA TEMPORAL (Ventas en espera por vendedor) ---
    private static Map<Usuario, Venta> ventasPendientes = new HashMap<>();

    public ControladorVenta(Usuario vendedor, RepositorioProducto repoProducto, RepositorioVenta repoVenta) {
        this.vendedor = vendedor;
        this.repoProducto = repoProducto;
        this.repoVenta = repoVenta;
        this.ventaActual = new Venta(vendedor);
        this.ventaActual.setId(this.repoVenta.generadorIdVentas());
    }

    public void nuevaVenta() {
        this.ventaActual = new Venta(vendedor);
        this.ventaActual.setId(this.repoVenta.generadorIdVentas());
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    public String agregarPorInput(String entrada, boolean esBulto) throws Exception {
        if (entrada == null || entrada.trim().isEmpty()) return "";
        
        String codigoInput = entrada;
        int cantidad = 1;

        // 1. Parsear "Cantidad*Codigo"
        if (entrada.contains("*")) {
            try {
                String[] partes = entrada.split("\\*");
                cantidad = Integer.parseInt(partes[0]);
                codigoInput = partes[1];
            } catch (NumberFormatException e) {
                throw new Exception("Formato inválido. Use 'CANTIDAD*CODIGO'");
            }
        }

        // 2. Buscar Producto (USANDO REPOSITORIO)
        Producto p = repoProducto.buscarPorCodigo(codigoInput);

        if (p == null) {
            throw new Exception("Producto no encontrado: " + codigoInput);
        }

        // 3. Validar Stock
        int factorReal = esBulto ? p.getFactor() : 1;
        int demandaTotalEnUnidades = cantidad * factorReal;
        
        // Verificamos si alcanza (considerando lo que ya pusimos en el carrito)
        // Ojo: Si agregas 2 veces el mismo producto, hay que sumar lo que ya está en la venta.
        int yaEnCarrito = ventaActual.calcularUnidadesEnCarrito(p);
        
        if (p.getCantidadStock() < (demandaTotalEnUnidades + yaEnCarrito)) {
            throw new Exception("Stock insuficiente. Disponibles: " + p.getCantidadStock());
        }

        // 4. Agregar al carrito
        ventaActual.agregarItem(p, cantidad, esBulto, codigoInput);
        
        return "OK";
    }

    public void finalizarVenta() {
    	
        // 1. Descontar Stock y Actualizar JSON de Productos
        for (DetalleVenta detalle : ventaActual.getItems()) {
            int cantidadADescontar = detalle.getCantidadUnidadesReales();
            Producto p = detalle.getProducto();
            
            p.descontarStock(cantidadADescontar, false);
            
            // Guardar el producto actualizado en el disco
            repoProducto.guardar(p);
        }
        ventaActual.actualizarFechaHora();
        // 2. Guardar la Venta en el Historial (JSON de Ventas)
        repoVenta.guardar(ventaActual);
        
        // 3. Limpiar pendientes si existían
        if (vendedor != null) ventasPendientes.remove(vendedor);
        
        // 4. Resetear para la siguiente

        nuevaVenta();

    }

    // ... (eliminarItem, modificarCantidadItem, calcularVuelto quedan IGUAL) ...
    public void eliminarItem(int indice) {
        if (indice >= 0 && indice < ventaActual.getItems().size()) {
            ventaActual.eliminarItem(ventaActual.getItems().get(indice));
        }
    }

    public void modificarCantidadItem(int indice, int nuevaCantidad) throws Exception {
       // ... Lógica idéntica, solo asegúrate de validar stock ...
       if (indice < 0 || indice >= ventaActual.getItems().size()) return;
       if (nuevaCantidad <= 0) throw new Exception("Cantidad debe ser mayor a 0.");

       DetalleVenta detalle = ventaActual.getItems().get(indice);
       Producto p = detalle.getProducto();
       
       // Validación simple (sin considerar que ya está en el carrito, se podría mejorar)
       if (p.getCantidadStock() < nuevaCantidad * detalle.getFactorSnapshot()) {
           throw new Exception("Stock insuficiente.");
       }
       detalle.setCantidad(nuevaCantidad);
       ventaActual.recalcularTotal();
    }

    public BigDecimal calcularVuelto(BigDecimal pago) {
        BigDecimal total = ventaActual.getTotal();
        if (pago.compareTo(total) < 0) throw new IllegalArgumentException("Pago Insuficiente");
        return pago.subtract(total);
    }

    // --- BÚSQUEDAS (Delegadas al Repo) ---

    public List<Producto> buscarPorNombre(String nombre) {
        // Como el repoProducto básico quizás no tiene buscarPorNombre, lo simulamos con streams
        // O idealmente agregas buscarPorNombre() a la interfaz RepositorioProducto
        return repoProducto.obtenerTodos().stream()
                .filter(p -> p.coincideCon(nombre))
                .collect(Collectors.toList());
    }

    public Producto buscarPorCodigo(String codigo) {
        return repoProducto.buscarPorCodigo(codigo);
    }
    
    // --- GESTIÓN DE VENTAS EN ESPERA (Ahora local en el controlador) ---

    public void guardarVentaEnEspera() {
        if (!ventaActual.getItems().isEmpty()) {
            ventasPendientes.put(vendedor, ventaActual);
            // Creamos una nueva vacía para seguir operando
            nuevaVenta(); 
            
        }
    }

    public boolean existeVentaPendiente() {
        return ventasPendientes.containsKey(vendedor);
    }

    public void restaurarVentaPendiente() {
        if (ventasPendientes.containsKey(vendedor)) {
            this.ventaActual = ventasPendientes.get(vendedor);
            // La sacamos del mapa para que no se duplique
            ventasPendientes.remove(vendedor);
        }
    }

    public void descartarVentaPendiente() {
        ventasPendientes.remove(vendedor);
        nuevaVenta();
    }
}