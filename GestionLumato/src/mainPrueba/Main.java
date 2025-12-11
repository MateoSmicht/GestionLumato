package mainPrueba;
import java.awt.EventQueue;
import java.math.BigDecimal;
import modelo.*;
import interfaz.MainForm; // <--- Importamos el Menú Principal directo

public class Main {
    public static void main(String[] args) {
        Empresa miNegocio = new Empresa("Gestion Lumato V");

        // ... carga de productos ...
        Categoria almacen = new Categoria(1, "Almacén");
        Categoria perfumeria = new Categoria(2, "perfumeria");
        Producto arroz = new Producto("A-01", "77912", almacen, "Arroz Gallo","BULTO",24, 
                new BigDecimal("1200"), new BigDecimal("0.30"), new BigDecimal("0.00"));
        arroz.agregarStock(50,true);
        miNegocio.agregarProducto(arroz);
        Producto coca = new Producto("A-02", "779", almacen, "Manaos Cola 2,25L", "BULTO", 6,
                new BigDecimal("1200"), new BigDecimal("0.30"), new BigDecimal("0.00"));
        coca.agregarStock(10,true);
        miNegocio.agregarProducto(coca);
        
     // PRODUCTO: Pasta Dental (La caja trae 12)
        Producto colgate = new Producto(
                "COL-12", "7798049448084", perfumeria, 
                "Colgate Total 12", 
                "CAJA", // Nombre de la unidad mayor
                12,     // Factor
                new BigDecimal("1000"), new BigDecimal("0.3"), new BigDecimal("0.21"));
        colgate.agregarStock(100,false);
        miNegocio.agregarProducto(colgate);

        // GUARDAMOS EL USUARIO EN UNA VARIABLE PARA USARLO ABAJO
        Usuario admin = new Gerente("admin", "123", "Mateo Smicht");
        miNegocio.agregarUsuario(admin);
        
        miNegocio.agregarUsuario(new Cajero("Micaela", "123", "Micaela sanchez"));

        // Iniciar Ventana DIRECTO AL MENU (Bypass Login)
        EventQueue.invokeLater(() -> {
            try {
                // ACÁ ESTABA TU ERROR: Le pasamos la empresa Y el usuario admin
                MainForm ventana = new MainForm(miNegocio, admin); 
                
                ventana.setVisible(true);
                ventana.setLocationRelativeTo(null);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}