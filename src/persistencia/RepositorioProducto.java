package persistencia;

import java.util.List;
import modelo.Producto;

public interface RepositorioProducto {
    void guardar(Producto producto);      
    Producto buscarPorCodigo(String codigoInterno);
    List<Producto> obtenerTodos();
    void eliminar(String codigoInterno);
}
