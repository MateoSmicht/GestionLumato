package persistencia;


import com.google.gson.*; 
import com.google.gson.reflect.TypeToken;
import modelo.Venta;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;



public class RepositorioVentaJSON implements RepositorioVenta {

    private final String RUTA_ARCHIVO = "ventas_historial.json";
    private final Gson gson;

    public RepositorioVentaJSON() {
        // --- AQUÍ ESTÁ LA MAGIA ---
        // Configuramos Gson para que entienda LocalDateTime
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }


    @Override
    public void guardar(Venta venta) {
        List<Venta> historial = obtenerTodas();
        historial.add(venta);
        try (Writer writer = new FileWriter(RUTA_ARCHIVO)) {
            gson.toJson(historial, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    public int totalVentas() {
    	return obtenerTodas().size();
    }
    
    public int generadorIdVentas() {
    	return totalVentas() + 1;
    }
    

    @Override
    public List<Venta> obtenerTodas() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(archivo)) {
            Type listaType = new TypeToken<ArrayList<Venta>>() {}.getType();
            List<Venta> lista = gson.fromJson(reader, listaType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) { return new ArrayList<>(); }
    }


    // Esto le enseña a Gson a leer/escribir fechas en formato ISO
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            // De Java a JSON (String)
            return new JsonPrimitive(src.format(formatter)); 
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            // De JSON (String) a Java
            return LocalDateTime.parse(json.getAsString(), formatter); 
        }
    }
}