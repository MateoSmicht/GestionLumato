package modelo;

public class Categoria {
    private int id;
    private String nombre;
    private Integer idPadre; //campo: null si es Madre, ID si es Hija

    public Categoria(int id, String nombre, Integer idPadre) {
        this.id = id;
        this.nombre = nombre;
        this.idPadre = idPadre;
    }

    // --- GETTERS Y SETTERS ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Integer getIdPadre() { return idPadre; }
    public void setIdPadre(Integer idPadre) { this.idPadre = idPadre; }

    public boolean esMadre() {
        return idPadre == null || idPadre <= 0;
    }

    @Override
    public String toString() {
        return nombre;
    }
}