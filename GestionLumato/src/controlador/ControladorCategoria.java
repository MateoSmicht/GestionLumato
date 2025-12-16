package controlador;


import java.util.List;
import modelo.Categoria;
import modelo.Empresa;

public class ControladorCategoria {

    private Empresa empresa;

    public ControladorCategoria(Empresa empresa) {
        this.empresa = empresa;
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

        // 3. Delegar al Modelo
        // Nota: Asumimos que empresa.crearCategoria ya maneja la lógica de IDs y guardado
        empresa.crearCategoria(nombre.trim(), idPadre);
    }

    public void modificarNombre(Categoria categoria, String nuevoNombre) throws Exception {
        if (categoria == null) {
            throw new Exception("Debe seleccionar una categoría para editar.");
        }
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new Exception("El nuevo nombre no puede estar vacío.");
        }
        
        empresa.modificarCategoria(categoria, nuevoNombre.trim());
    }

    // --- Métodos para llenar los ComboBox ---
    
    public List<Categoria> obtenerCategoriasMadre() {
        return empresa.getCategoriasMadre();
    }

    public List<Categoria> obtenerTodas() {
        return empresa.getCategorias(); // Asumiendo que devuelve List<Categoria>
    }
}