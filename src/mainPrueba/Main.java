package mainPrueba;

import java.awt.EventQueue;
import controlador.ControladorCategoria;
import controlador.ControladorStock;
import interfaz.MainForm;
import modelo.Empresa;
import modelo.Gerente;
import modelo.Usuario;
// Asegúrate de que tus repositorios estén en este paquete o ajusta el import
import persistencia.RepositorioCategoriaJSON; 
import persistencia.RepositorioCategoria;
import persistencia.RepositorioProductoJSON;
import persistencia.RepositorioProducto;

public class Main {
    public static void main(String[] args) {
        
        // 1. INICIALIZAR EL MODELO Y REPOSITORIOS
        Empresa miNegocio = new Empresa("Gestion Lumato V");
        
        // Repositorios (Capa de Datos)
        RepositorioProducto repoProd = new RepositorioProductoJSON();
        RepositorioCategoria repoCat = new RepositorioCategoriaJSON();

        // 2. INICIALIZAR LOS CONTROLADORES (Capa Lógica)
        // Controlador de Stock (Productos)
        ControladorStock controlProd = new ControladorStock(miNegocio, repoProd);
        
        // Controlador de Categorías (Necesita ambos repositorios para validar)
        ControladorCategoria controlCat = new ControladorCategoria(repoCat, repoProd);


        // 3. CARGA INICIAL DE DATOS (Solo si el archivo está vacío)
        try {
            if (repoCat.obtenerTodas().isEmpty()) {
                System.out.println("Cargando categorías iniciales...");
                
                // CORRECCIÓN AQUÍ: El segundo parámetro es boolean (false), no null.
                controlCat.guardarNuevaCategoria("Almacen", false, null);
                controlCat.guardarNuevaCategoria("Perfumeria", false, null);
                controlCat.guardarNuevaCategoria("Limpieza", false, null);
                controlCat.guardarNuevaCategoria("Bebidas", false, null);
                
                System.out.println("Categorías creadas exitosamente en JSON.");
            }
        } catch (Exception e) {
            System.err.println("Error al crear categorías iniciales: " + e.getMessage());
        }
        
        // 4. CREACIÓN DE USUARIO (Hardcodeado para entrar)
        Usuario admin = new Gerente("admin", "123", "Mateo Smicht");
        miNegocio.agregarUsuario(admin);

        // 5. INICIAR LA INTERFAZ GRÁFICA
        EventQueue.invokeLater(() -> {
            try {
                // IMPORTANTE: Asegúrate de que el constructor de MainForm coincida con esto:
                // public MainForm(Empresa e, Usuario u, ControladorStock cs, ControladorCategoria cc)
                MainForm ventana = new MainForm(miNegocio, admin, controlProd, controlCat);
                
                ventana.setVisible(true);
                ventana.setLocationRelativeTo(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}