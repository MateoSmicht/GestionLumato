package controlador;

import java.util.List;
import java.util.stream.Collectors;
import modelo.Categoria;
import modelo.Producto;

import persistencia.RepositorioCategoria;
import persistencia.RepositorioProducto;

public class ControladorCategoria {

    // Dependencias: Necesitamos acceder a Categorías (para CRUD) y Productos (para validaciones)
    private RepositorioCategoria repoCategoria;
    private RepositorioProducto repoProducto;

    public ControladorCategoria(RepositorioCategoria repoCategoria, RepositorioProducto repoProducto) {
        this.repoCategoria = repoCategoria;
        this.repoProducto = repoProducto;
    }

    public void guardarNuevaCategoria(String nombre, boolean esSubcategoria, Categoria categoriaMadre) throws Exception {
        // 1. Validar Nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new Exception("El nombre de la categoría no puede estar vacío.");
        }

        // 2. Validar Jerarquía
        Integer idPadre = null;
        if (esSubcategoria) {
            if (categoriaMadre == null) {
                throw new Exception("Si es una subcategoría, debe seleccionar a qué Categoría Madre pertenece.");
            }
            idPadre = categoriaMadre.getId();
        }

        // 3. Crear y Guardar (Ahora calculamos el ID nosotros)
        int nuevoId = generarNuevoId();
        Categoria nueva = new Categoria(nuevoId, nombre.trim(), idPadre);
        
        // ¡Al JSON!
        repoCategoria.guardar(nueva);
    }
    
    public void eliminarCategoria(Categoria cat) throws Exception {
        // Validación 1: ¿Es padre de alguien? (Reemplaza a empresa.categoriaEstaEnUso)
        boolean tieneHijas = repoCategoria.obtenerTodas().stream()
                .anyMatch(c -> c.esHijaDe(cat.getId()));
        
        if (tieneHijas) {
            throw new Exception("No se puede eliminar: Esta categoría tiene subcategorías asociadas.");
        }

        // Validación 2: ¿Tiene productos? (Reemplaza a empresa.categoriaTieneProductos)
        // Aquí usamos el repoProducto para verificar el JSON de productos
        boolean tieneProductos = repoProducto.obtenerTodos().stream()
                .anyMatch(p -> p.getCategoria() != null && 
                               (p.getCategoria().getId() == cat.getId() || p.getCategoria().esHijaDe(cat.getId())));

        if (tieneProductos) {
            throw new Exception("No se puede eliminar: Hay productos asociados a esta categoría.");
        }

        // Si pasa las validaciones, borramos del JSON
        repoCategoria.eliminar(cat.getId());
    }

    public void modificarCategoria(Categoria cat, String nuevoNombre, Categoria nuevaMadre) throws Exception {
        // 1. Validaciones básicas
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new Exception("El nombre no puede estar vacío.");
        }

        // 2. Validación de Jerarquía: No puede ser su propio padre
        if (nuevaMadre != null && nuevaMadre.getId() == cat.getId()) {
            throw new Exception("Una categoría no puede ser su propia madre.");
        }

        // 3. Aplicar cambios al objeto en memoria
        cat.setNombre(nuevoNombre);

        if (nuevaMadre != null) {
            cat.setIdPadre(nuevaMadre.getId());
        } else {
            cat.setIdPadre(null);
        }
        
        // 4. ¡IMPORTANTÍSIMO! Guardar el cambio en el JSON
        repoCategoria.guardar(cat);
    }

    // --- Métodos de Consulta (Reemplazan a los de Empresa) ---
    
    public List<Categoria> obtenerCategoriasMadre() {
        return repoCategoria.obtenerTodas().stream()
                .filter(Categoria::esMadre)
                .collect(Collectors.toList());
    }

    public List<Categoria> obtenerSubCategorias(int idPadre) {
        return repoCategoria.obtenerTodas().stream()
                .filter(c -> c.esHijaDe(idPadre))
                .collect(Collectors.toList());
    }

    public List<Categoria> obtenerTodas() {
        return repoCategoria.obtenerTodas();
    }
    
    public Categoria buscarPorId(int id) {
        return repoCategoria.buscarPorId(id);
    }

    // --- Auxiliares ---
    
    private int generarNuevoId() {
        return repoCategoria.obtenerTodas().stream()
                .mapToInt(Categoria::getId)
                .max()
                .orElse(0) + 1;
    }
}