package mainPrueba;
import java.awt.EventQueue;
import java.math.BigDecimal;
import modelo.*;
import interfaz.MainForm; // <--- Importamos el Menú Principal directo

public class Main {
    public static void main(String[] args) {
        Empresa miNegocio = new Empresa("Supermercado Java");

        // ... carga de productos ...
        Categoria almacen = new Categoria(1, "Almacén");
        Producto arroz = new Producto("A-01", "779123456", almacen, "Arroz Gallo", 
                new BigDecimal("1200"), new BigDecimal("0.30"), new BigDecimal("0.00"));
        arroz.agregarStock(100);
        miNegocio.agregarProducto(arroz);

        // GUARDAMOS EL USUARIO EN UNA VARIABLE PARA USARLO ABAJO
        Usuario admin = new Gerente("admin", "123", "Mateo Gerente");
        miNegocio.agregarUsuario(admin);
        
        miNegocio.agregarUsuario(new Cajero("pepe", "0000", "Pepe Cajero"));

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