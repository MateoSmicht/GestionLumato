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

    public Empresa(String nombre) {
        this.nombre = nombre;
        this.stock = new HashMap<>();
        this.usuarios = new HashMap<>();
        this.historialVentas = new ArrayList<>();
    }

    public void agregarUsuario(Usuario u) {
        this.usuarios.put(u.getUsername(), u);
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

    public void registrarVenta(Venta venta) {
        this.historialVentas.add(venta);
    }

    public List<Venta> getHistorialVentas() { return historialVentas; }
    public String getNombre() { return nombre; }
}