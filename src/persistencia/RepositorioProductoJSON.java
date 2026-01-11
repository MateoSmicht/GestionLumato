package persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import modelo.Producto;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositorioProductoJSON implements RepositorioProducto {

    private final String RUTA_ARCHIVO = "productos.json";
    private final Gson gson;
    private Map<String, Producto> mapaId;//MAPA DE PRODUCTOS CON CLAVE CODIGO INTERNO
    private Map<String, Producto> mapaBarra;//MAPA DE PRODUCTOS CON CLAVE CODIGO BARRA

    public RepositorioProductoJSON() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.mapaId = new HashMap<>(); 
        this.mapaBarra = new HashMap<>();
        
        cargarCacheDesdeArchivo();
    }

    private void cargarCacheDesdeArchivo() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) return;

        try (Reader reader = new FileReader(archivo)) {
            Type listaType = new TypeToken<ArrayList<Producto>>() {}.getType();
            List<Producto> lista = gson.fromJson(reader, listaType);
            
            if (lista != null) {
                for (Producto p : lista) {
                    // Llenamos AMBOS mapas
                    mapaId.put(p.getCodigoInterno(), p);
                    
                    // Solo indexamos si tiene código de barra (por seguridad)
                    if (p.getCodigoBarra() != null && !p.getCodigoBarra().isEmpty()) {
                    	mapaBarra.put(p.getCodigoBarra(), p); 
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS PÚBLICOS ---

    @Override
    public Producto buscarPorCodigo(String codigoBarra) {
        if (codigoBarra == null) return null;
        return mapaBarra.get(codigoBarra);
    }
    
    // Método extra por si alguna vez necesitas buscar por ID interno (opcional)
    public Producto buscarPorIdInterno(String id) {
        return mapaId.get(id);
    }

    @Override
    public List<Producto> obtenerTodos() {
        return new ArrayList<>(mapaId.values());
    }

    @Override
    public void guardar(Producto p) {
        Producto versionAnterior = mapaId.get(p.getCodigoInterno());
        
        if (versionAnterior != null) {
            // A. Borrar código de barra principal viejo
            String barraVieja = versionAnterior.getCodigoBarra();
            if (barraVieja != null) {
                mapaBarra.remove(barraVieja);
            }
            
            // B. Borrar TODOS los códigos secundarios viejos 
            for (String aliasViejo : versionAnterior.getCodigosSecundarios()) {
                mapaBarra.remove(aliasViejo);
            }
        }

        // --- PASO 2: ACTUALIZACIÓN (
        
        // A. Guardar en el Mapa Maestro (ID Interno)
        mapaId.put(p.getCodigoInterno(), p);
        
        // B. Indexar Código Principal
        if (p.getCodigoBarra() != null && !p.getCodigoBarra().isEmpty()) {
            mapaBarra.put(p.getCodigoBarra(), p);
        }
        
        // C. Indexar Códigos Secundarios / Alias (Esto es clave para "Unificar")
        for (String alias : p.getCodigosSecundarios()) {
            if (alias != null && !alias.isEmpty()) {
                mapaBarra.put(alias, p);
            }
        }
        
        // --- PASO 3: PERSISTENCIA ---
        guardarCambiosEnArchivo();
    }

    @Override
    public void eliminar(String codigoInterno) {
        Producto p = mapaId.remove(codigoInterno);
        
        // Si existía, también lo sacamos del índice de barras
        if (p != null && p.getCodigoBarra() != null) {
        	mapaBarra.remove(p.getCodigoBarra());
        }
        
        guardarCambiosEnArchivo();
    }

    public void eliminarIndiceBarra(String codigoBarra) {
        if (codigoBarra != null) {
            mapaBarra.remove(codigoBarra);
        }
    }

    // --- ESCRITURA ---
    private void guardarCambiosEnArchivo() {
        try (Writer writer = new FileWriter(RUTA_ARCHIVO)) {
            // Guardamos basándonos en el mapa de IDs que es el "maestro"
            List<Producto> listaParaGuardar = new ArrayList<>(mapaId.values());
            gson.toJson(listaParaGuardar, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}