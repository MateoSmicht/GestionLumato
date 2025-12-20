package interfaz;

import javax.swing.*;
import java.awt.*;
import modelo.Empresa;

public class PanelAltaStock extends JPanel {

    private static final long serialVersionUID = 1L;

    // Guardamos la acción en una variable privada final (segura)
    private final Runnable accionVolver; 

    // CAMBIO: El constructor ahora pide la acción obligatoriamente
    public PanelAltaStock(Empresa empresa, Runnable accionVolver) {
        this.accionVolver = accionVolver;
        
        setLayout(new BorderLayout());
        
        // INSTANCIAMOS EL PANEL MAESTRO
        PanelFormularioProducto formulario = new PanelFormularioProducto(
            empresa, 
            "", // Sin código predefinido
            
            // 1. ACCIÓN AL GUARDAR (EXITOSO)
            () -> { 
                // Al limpiar, creamos un nuevo panel pero LE PASAMOS LA MISMA ACCIÓN
                removeAll();
                add(new PanelAltaStock(empresa, accionVolver)); 
                revalidate();
                repaint();
            }, 
            
            // 2. ACCIÓN AL CANCELAR
            () -> {
                if (accionVolver != null) {
                    accionVolver.run(); 
                }
            }
        );
        
        add(formulario, BorderLayout.CENTER);
    }
}