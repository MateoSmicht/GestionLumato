package interfaz;

import javax.swing.*;
import java.awt.*;
import modelo.Empresa;

public class PanelAltaStock extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // Para que el MainForm pueda asignar la acción de "Volver"
    public Runnable accionVolverExterna; 

    public PanelAltaStock(Empresa empresa) {
        setLayout(new BorderLayout());
        
        // INSTANCIAMOS EL PANEL MAESTRO
        PanelFormularioProducto formulario = new PanelFormularioProducto(
            empresa, 
            "", // Sin código predefinido
            () -> { 
                // Al guardar: Limpiamos y volvemos a crear el panel (Reset)
                removeAll();
                add(new PanelAltaStock(empresa)); 
                revalidate();
            }, 
            () -> {
                // Al cancelar: Llamamos a la acción del botón volver del Main
                if (accionVolverExterna != null) accionVolverExterna.run();
            }
        );
        
        add(formulario, BorderLayout.CENTER);
    }
}