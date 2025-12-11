package controlador;

import java.util.ArrayList;
import java.util.List;
import modelo.Empresa;
import modelo.Producto;

public class ControladorCargaStock {

    private Empresa empresa;
    private List<ItemCarga> listaItems;

    public ControladorCargaStock(Empresa empresa) {
        this.empresa = empresa;
        this.listaItems = new ArrayList<>();
    }

    // --- CLASE INTERNA PARA GUARDAR LO QUE VAMOS A CARGAR ---
    public class ItemCarga {
        private Producto producto;
        private int cantidad;
        private boolean esBulto;

        public ItemCarga(Producto p, int cant, boolean bulto) {
            this.producto = p;
            this.cantidad = cant;
            this.esBulto = bulto;
        }
        
        // Getters para la tabla
        public Producto getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public boolean isEsBulto() { return esBulto; }
        
        // Calcula cuántas unidades reales se sumarán (Visual)
        public int getUnidadesReales() {
            return esBulto ? cantidad * producto.getFactor() : cantidad;
        }
        
        public void setCantidad(int cant) { this.cantidad = cant; }
    }

    // --- MÉTODOS LÓGICOS ---

    public void agregarItem(String entrada, boolean modoBulto) throws Exception {
        if (entrada.isEmpty()) return;

        // 1. Lógica de multiplicador (ej: "3*779...")
        int cantidad = 1;
        String codigo = entrada;

        if (entrada.contains("*")) {
            String[] partes = entrada.split("\\*");
            cantidad = Integer.parseInt(partes[0]);
            codigo = partes[1];
        }

        Producto p = empresa.buscarProducto(codigo);
        if (p == null) {
            throw new Exception("Producto no encontrado: " + codigo);
        }

        // Agregamos a la lista temporal
        listaItems.add(new ItemCarga(p, cantidad, modoBulto));
    }

    public void eliminarItem(int index) {
        if (index >= 0 && index < listaItems.size()) {
            listaItems.remove(index);
        }
    }

    public void agregarItemConCantidad(String codigo, int cantidad, boolean modoBulto) throws Exception {
        Producto p = empresa.buscarProducto(codigo);
        
        if (p == null) {
            throw new Exception("Error interno: Producto recién creado no encontrado.");
        }

        // Si la cantidad es 0 (el usuario no puso nada), ponemos 1 por defecto para que aparezca
        int cantidadFinal = (cantidad > 0) ? cantidad : 1;

        listaItems.add(new ItemCarga(p, cantidadFinal, modoBulto));
    }

    public Producto buscarProducto(String codigo) {
        return empresa.buscarProducto(codigo);
    }

    public Empresa getEmpresa() {
        return empresa;
    }
    
    public void modificarCantidad(int index, int nuevaCant) {
        if (index >= 0 && index < listaItems.size() && nuevaCant > 0) {
            listaItems.get(index).setCantidad(nuevaCant);
        }
    }

    public void confirmarCargaMasiva() {
        // Recorremos la lista y aplicamos los cambios reales a la empresa
        for (ItemCarga item : listaItems) {
            item.getProducto().agregarStock(item.getCantidad(), item.isEsBulto());
        }
        // Limpiamos la lista
        listaItems.clear();
    }
    
    public List<ItemCarga> getListaItems() {
        return listaItems;
    }
    
    public void vaciarLista() {
        listaItems.clear();
    }
    
    // Delegamos la búsqueda para el F5
    public java.util.List<Producto> buscarPorNombre(String nombre) {
        return empresa.buscarProductosPorNombre(nombre);
    }
}