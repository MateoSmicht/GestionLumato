package persistencia;
import modelo.Venta;
import java.util.List;

public interface RepositorioVenta {
    void guardar(Venta venta);
    List<Venta> obtenerTodas();
    int generadorIdVentas();
}
