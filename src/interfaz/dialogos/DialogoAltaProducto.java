package interfaz.dialogos;

import javax.swing.*;

import controlador.ControladorStock;
import interfaz.PanelFormularioProducto;
import modelo.Empresa;

public class DialogoAltaProducto extends JDialog {

    private static final long serialVersionUID = 1L;
    private PanelFormularioProducto panelFormulario;
    private boolean guardado = false;

    public DialogoAltaProducto(JFrame parent, String codigo, ControladorStock cs) {
        super(parent, "Nuevo Producto", true);
        setSize(800, 580);
        setLocationRelativeTo(parent);
        
        // INSTANCIAMOS EL PANEL MAESTRO
        panelFormulario = new PanelFormularioProducto(
            cs, 
            codigo, 
            () -> { // Acción al guardar
                this.guardado = true;
                dispose(); // Cerrar ventana
            }, 
            () -> dispose() // Acción al cancelar: Cerrar ventana
        );
        
        setContentPane(panelFormulario);
    }
    
    public boolean isGuardadoExitoso() { return guardado; }
    
    public int getStockIngresado() {
        return panelFormulario.getStockIngresado();
    }
}