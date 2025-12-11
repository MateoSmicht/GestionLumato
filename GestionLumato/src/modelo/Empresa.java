package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Empresa {
    private String nombre;
    private Map<String, Producto> stock;    // Clave: Código de Barra
    private Map<String, Usuario> usuarios;  // Clave: Username
    private List<Venta> historialVentas; 
    private Map<String, Venta> ventasPendientes;

    public Empresa(String nombre) {
        this.nombre = nombre;
        this.stock = new HashMap<>();
        this.usuarios = new HashMap<>();
        this.historialVentas = new ArrayList<>();
        this.ventasPendientes = new HashMap<>();
    }

    public void agregarUsuario(Usuario u) {
        this.usuarios.put(u.getUsername(), u);
    }
   
    public boolean existeCodigoInterno(String codigoInterno) {
        for (Producto p : this.stock.values()) {
            if (p.getCodigoInterno().equalsIgnoreCase(codigoInterno)) {
                return true; 
            }
        }
        return false; 
    }
    
    public boolean elProductoYaEstaCargado(String codigoBarra) {
    	return stock.containsKey(codigoBarra);
    }
    
    public int obtenerTodoElStock() {
    	return stock.size();
    }

    public Usuario login(String username, String password) {
        Usuario u = this.usuarios.get(username);
        if (u != null && u.validarPassword(password)) return u;
        return null;
    }

    public void agregarProducto(Producto p) {
        this.stock.put(p.getCodigoBarra(), p);
    }
    
    public Producto buscarProducto(String codigo) {
        return this.stock.get(codigo);
    }
    
    
    public List<Producto> buscarProductosPorNombre(String nombreBusqueda) {
        List<Producto> resultados = new java.util.ArrayList<>();  
        String busqueda = nombreBusqueda.toLowerCase().trim();
        for (Producto p : this.stock.values()) {    
            if (p.getDescripcion().toLowerCase().contains(busqueda)) {
                resultados.add(p);
            }
        }      
        return resultados;
    }
    
    public boolean validarNumero(String texto) {
        // 1. Chequeamos si es null O si esta vacío O si NO son digitos
        if (texto == null || texto.trim().isEmpty() || !texto.matches("\\d+")) {
            return true;
        }
        return false;
    }

    public void registrarVenta(Venta venta) {
        this.historialVentas.add(venta);
    }
    
    public void setVentaPendiente(Usuario u, Venta v) {
        // Guardamos la venta en el casillero de ESTE usuario
        this.ventasPendientes.put(u.getUsername(), v);
    }

    public Venta getVentaPendiente(Usuario u) {
        // Recuperamos solo la de ESTE usuario
        return this.ventasPendientes.get(u.getUsername());
    }
    
    public void borrarVentaPendiente(Usuario u) {
        this.ventasPendientes.remove(u.getUsername());
    }

    public boolean hayVentaPendiente(Usuario u) {
        // Verificamos si existe y si tiene ítems
        Venta v = this.ventasPendientes.get(u.getUsername());
        return v != null && !v.getItems().isEmpty();
    }


    public List<Venta> getHistorialVentas() { return historialVentas; }
    public String getNombre() { return nombre; }
}