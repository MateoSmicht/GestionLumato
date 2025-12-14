package controlador;

import java.util.ArrayList;
import java.util.List;
import modelo.Empresa;
import modelo.Producto;
import modelo.DetalleCarga; // <--- Importamos la nueva clase

public class ControladorCargaStock {

    private Empresa empresa;
    private List<DetalleCarga> listaItems; // Usamos DetalleCarga

    public ControladorCargaStock(Empresa empresa) {
        this.empresa = empresa;
        this.listaItems = new ArrayList<>();
    }

    // --- YA NO EXISTE LA CLASE INTERNA AQUÍ ---

    // Método buscar para el F6
    public Producto buscarProducto(String codigo) {
        return empresa.buscarProducto(codigo);
    }
    
    public Empresa getEmpresa() { return empresa; }

    public void agregarItem(String entrada, boolean modoBulto) throws Exception {
        if (entrada.isEmpty()) return;

        int cantidad = 1;
        String codigo = entrada;

        if (entrada.contains("*")) {
            String[] partes = entrada.split("\\*");
            cantidad = Integer.parseInt(partes[0]);
            codigo = partes[1];
        }

        Producto p = empresa.buscarProducto(codigo);
        if (p == null) throw new Exception("Producto no encontrado: " + codigo);

        // Creamos el objeto del modelo
        listaItems.add(new DetalleCarga(p, cantidad, modoBulto));
    }
    
    // Método nuevo para cuando viene del Alta con cantidad específica
    public void agregarItemConCantidad(String codigo, int cantidad, boolean modoBulto) throws Exception {
        Producto p = empresa.buscarProducto(codigo);
        if (p == null) throw new Exception("Error interno.");
        
        int cantFinal = (cantidad > 0) ? cantidad : 1;
        listaItems.add(new DetalleCarga(p, cantFinal, modoBulto));
    }

    public void eliminarItem(int index) {
        if (index >= 0 && index < listaItems.size()) {
            listaItems.remove(index);
        }
    }
    
    public void modificarCantidad(int index, int nuevaCant) {
        if (index >= 0 && index < listaItems.size() && nuevaCant > 0) {
            listaItems.get(index).setCantidad(nuevaCant);
        }
    }

    public void confirmarCargaMasiva() {
        for (DetalleCarga item : listaItems) {
            item.getProducto().agregarStock(item.getCantidad(), item.isEsBulto());
        }
        listaItems.clear();
    }
    
    public List<DetalleCarga> getListaItems() {
        return listaItems;
    }
    
    public java.util.List<Producto> buscarPorNombre(String nombre) {
        return empresa.buscarProductosPorNombre(nombre);
    }
}