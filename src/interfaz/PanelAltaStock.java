package interfaz;

import javax.swing.*;

import controlador.ControladorCategoria;
import controlador.ControladorStock;

import java.awt.*;
import modelo.Empresa;

public class PanelAltaStock extends JPanel {

	private static final long serialVersionUID = 1L;

	// Guardamos la acción en una variable privada final (segura)
	private final Runnable accionVolver;
	private ControladorStock controlador;

	// CAMBIO: El constructor ahora pide la acción obligatoriamente
	public PanelAltaStock(Empresa empresa, Runnable accionVolver, ControladorStock cs,ControladorCategoria controladorCat) {
		this.accionVolver = accionVolver;
		this.controlador = cs;
		setLayout(new BorderLayout());

		// INSTANCIAMOS EL PANEL MAESTRO
		PanelFormularioProducto formulario = new PanelFormularioProducto( this.controlador,controladorCat, "", // Sin código predefinido

				// 1. ACCIÓN AL GUARDAR (EXITOSO)
				() -> {
					// Al limpiar, creamos un nuevo panel pero LE PASAMOS LA MISMA ACCIÓN
					removeAll();
					add(new PanelAltaStock(empresa, accionVolver,this.controlador, controladorCat));
					revalidate();
					repaint();
				},

				// 2. ACCIÓN AL CANCELAR
				() -> {
					if (accionVolver != null) {
						accionVolver.run();
					}
				});

		add(formulario, BorderLayout.CENTER);
	}
}