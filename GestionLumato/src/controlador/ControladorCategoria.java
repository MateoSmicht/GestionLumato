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

    


    public void modificarCategoria(Categoria cat, String nuevoNombre, Categoria nuevaMadre) throws Exception {
        // 1. Validaciones básicas
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new Exception("El nombre no puede estar vacío.");
        }

        // 2. Validación de Jerarquía: No puede ser su propio padre
        if (nuevaMadre != null && nuevaMadre.getId() == cat.getId()) {
            throw new Exception("Una categoría no puede ser su propia madre.");
        }

        // 3. Aplicar cambios
        cat.setNombre(nuevoNombre);

        if (nuevaMadre != null) {
            cat.setIdPadre(nuevaMadre.getId()); // Se convierte en Subcategoría
        } else {
            cat.setIdPadre(null); // Se convierte en Categoría Principal (Madre)
        }
        
        // Aquí (si usaras BD) iría: dao.actualizar(cat);
    }

    // --- Métodos para llenar los ComboBox ---
    
    public List<Categoria> obtenerCategoriasMadre() {
        return empresa.getCategoriasMadre();
    }

    public List<Categoria> obtenerTodas() {
        return empresa.getCategorias(); // Asumiendo que devuelve List<Categoria>
    }
}