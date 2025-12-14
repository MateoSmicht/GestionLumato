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
    
 // En modelo/Empresa.java

    public void unificarProductos(String codPrincipal, String codDuplicado) throws Exception {
        Producto principal = stock.get(codPrincipal);
        Producto duplicado = stock.get(codDuplicado);

        if (principal == null || duplicado == null) {
            throw new Exception("Uno de los productos no existe.");
        }
        if (principal == duplicado) { // Comparan referencia de memoria
            throw new Exception("¡Son el mismo producto! No se pueden unificar.");
        }

        // 1. Sumar Stocks
        int stockDelDuplicado = duplicado.getCantidadStock();
        if (stockDelDuplicado > 0) {
            // Asumimos que ambos son unidad o bulto, sumamos directo unidades
            principal.agregarStock(stockDelDuplicado, false); 
        }

        // 2. Guardar el código viejo como secundario en el principal
        principal.agregarCodigoSecundario(codDuplicado);
        
        // 3. Si el duplicado tenía sus propios alias, también nos los robamos
        for (String alias : duplicado.getCodigosSecundarios()) {
            principal.agregarCodigoSecundario(alias);
            // Re-apuntamos esos alias también en el mapa
            stock.put(alias, principal);
        }

        // 4. EL PASO CLAVE: Re-apuntamos el código del duplicado al objeto principal
        stock.put(codDuplicado, principal);
        
        // Ahora 'stock.get(codDuplicado)' devuelve el objeto 'principal'.
        // El objeto 'duplicado' viejo queda sin referencias y el recolector de basura lo borra.
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