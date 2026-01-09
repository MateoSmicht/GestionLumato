package modelo;

import java.util.Objects;

public class Categoria {
    private int id;
    private String nombre;
    private Integer idPadre; 


    public Categoria() {
    }

    public Categoria(int id, String nombre, Integer idPadre) {
        this.id = id;
        this.nombre = nombre;
        this.idPadre = idPadre;
    }

    // --- GETTERS Y SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } 
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Integer getIdPadre() { return idPadre; }
    public void setIdPadre(Integer idPadre) { this.idPadre = idPadre; }


    public boolean esMadre() {
        return idPadre == null || idPadre <= 0;
    }


    public boolean esSubcategoria() {
        return !esMadre();
    }
    
    // Método útil para saber si esta categoría es hija de una específica
    public boolean esHijaDe(int idPosiblePadre) {
        return this.idPadre != null && this.idPadre == idPosiblePadre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;

        return id == categoria.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return nombre; // Esto es lo que se ve en el ComboBox
    }
}