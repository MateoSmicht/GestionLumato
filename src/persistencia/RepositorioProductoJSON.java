package persistencia;



import modelo.Producto;
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RepositorioProductoJSON implements RepositorioProducto {

    private final String RUTA_ARCHIVO = "productos.json";
    private final Gson gson;

    public RepositorioProductoJSON() {
        // Usamos setPrettyPrinting para que el JSON sea legible por humanos (con enters y espacios)
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Producto> obtenerTodos() {
        File archivo = new File(RUTA_ARCHIVO);

        // 1. Si el archivo no existe, devolvemos lista vacía
        if (!archivo.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(archivo)) {
            // 2. EL TRUCO DEL TYPE TOKEN (Necesario para listas)
            Type listaProductosType = new TypeToken<ArrayList<Producto>>() {}.getType();
            
            // 3. Convertir Texto -> Objetos Java
            List<Producto> productos = gson.fromJson(reader, listaProductosType);
            
            // Protección extra: si el archivo estaba vacío, gson devuelve null
            return productos != null ? productos : new ArrayList<>();

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void guardar(Producto p) {
        List<Producto> productos = obtenerTodos();

        // Lógica de "UPSERT" (Update or Insert)
        // Si ya existe un producto con ese código, lo borramos para poner el nuevo
        productos.removeIf(prod -> prod.getCodigoInterno().equals(p.getCodigoInterno()));

        // Agregamos la versión nueva
        productos.add(p);

        guardarListaEnArchivo(productos);
    }

    @Override
    public Producto buscarPorCodigo(String codigoInterno) {
        List<Producto> productos = obtenerTodos();
        
        // Recorremos la lista en memoria buscando el código
        for (Producto p : productos) {
            if (p.getCodigoInterno().equals(codigoInterno)) {
                return p;
            }
        }
        return null; // No encontrado
    }

    @Override
    public void eliminar(String codigoInterno) {
        List<Producto> productos = obtenerTodos();
        
        // Borramos si coincide el código
        boolean borrado = productos.removeIf(p -> p.getCodigoInterno().equals(codigoInterno));

        if (borrado) {
            guardarListaEnArchivo(productos);
        }
    }

    // --- MÉTODOS PRIVADOS AUXILIARES ---

    private void guardarListaEnArchivo(List<Producto> productos) {
        try (Writer writer = new FileWriter(RUTA_ARCHIVO)) {
            // Convertir Objetos Java -> Texto JSON
            gson.toJson(productos, writer);
        } catch (IOException e) {
            e.printStackTrace();
            // En un sistema real, aquí lanzarías una excepción personalizada
        }
    }
}