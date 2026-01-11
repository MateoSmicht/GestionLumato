package persistencia;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import modelo.Categoria;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RepositorioCategoriaJSON implements RepositorioCategoria {

    private final String RUTA_ARCHIVO = "categorias.json"; // Archivo separado
    private final Gson gson;

    public RepositorioCategoriaJSON() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Categoria> obtenerTodas() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(archivo)) {
            Type listaType = new TypeToken<ArrayList<Categoria>>() {}.getType();
            List<Categoria> lista = gson.fromJson(reader, listaType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void guardar(Categoria c) {
        List<Categoria> lista = obtenerTodas();
        // Upsert: Si ya existe (mismo ID), lo actualizamos. Si no, agregamos.
        lista.removeIf(cat -> cat.getId() == c.getId());
        lista.add(c);
        guardarEnArchivo(lista);
    }

    @Override
    public Categoria buscarPorId(int id) {
        for (Categoria c : obtenerTodas()) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    @Override
    public void eliminar(int id) {
        List<Categoria> lista = obtenerTodas();
        boolean borrado = lista.removeIf(c -> c.getId() == id);
        if (borrado) guardarEnArchivo(lista);
    }

    private void guardarEnArchivo(List<Categoria> lista) {
        try (Writer writer = new FileWriter(RUTA_ARCHIVO)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}