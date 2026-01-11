package persistencia;


import java.util.List;
import modelo.Categoria;

public interface RepositorioCategoria {
    void guardar(Categoria categoria);
    Categoria buscarPorId(int id);
    List<Categoria> obtenerTodas();
    void eliminar(int id);
}