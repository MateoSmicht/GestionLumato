package interfaz;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;

import modelo.Empresa;
import modelo.Usuario;
import modelo.Producto;
import modelo.Venta;
import modelo.DetalleVenta;
import javax.swing.SwingConstants;

public class PanelVenta extends JPanel {

	private static final long serialVersionUID = 1L;

	// Referencias
	private Empresa empresa;
	private Usuario vendedor;
	private Venta ventaActual;

	// Componentes Visuales
	private JTextField txtBusquedaProducto;
	private JTable tableDetalle;
	private DefaultTableModel modeloTabla; // Para manejar los datos de la grilla
	private JLabel lblTotalPagar;

	/**
	 * Constructor del Panel
	 */
	public PanelVenta(Empresa empresa, Usuario vendedor) {
		this.empresa = empresa;
		this.vendedor = vendedor;

		// Iniciamos una nueva venta vacía al crear el panel
		this.ventaActual = new Venta(vendedor);

		setLayout(null); // Absolute layout para WindowBuilder
		setBounds(0, 0, 780, 500); // Tamaño sugerido

		// 1. ZONA DE BÚSQUEDA (Arriba)
		JLabel lblBuscar = new JLabel("INGRESE CODIGO:");
		lblBuscar.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblBuscar.setBounds(20, 31, 150, 25);
		add(lblBuscar);

		txtBusquedaProducto = new JTextField();
		txtBusquedaProducto.setBounds(170, 20, 347, 51);
		add(txtBusquedaProducto);

		// El "Enter" mágico
		txtBusquedaProducto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				procesarEntradaProducto();
			}
		});

		// 2. TABLA DE DETALLE (Centro)
		// Configuramos las columnas
		String[] columnas = { "Cód.", "Descripción", "Precio Unit.", "Cant.", "Subtotal" };
		modeloTabla = new DefaultTableModel(columnas, 0) {
			// Hacemos que la tabla no sea editable (solo lectura)
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tableDetalle = new JTable(modeloTabla);
		JScrollPane scrollPane = new JScrollPane(tableDetalle);
		// ... (Código anterior del constructor: Buscador, Tabla, etc.) ...

		// 2. TABLA DE DETALLE (Centro)
		// ... (Tu código de la tabla) ...
		scrollPane.setBounds(139, 82, 602, 339); // Reducimos un poco el ancho para que entre el botón al costado
		add(scrollPane);

		// BOTÓN ELIMINAR (Al costado de la tabla) ---
		JButton btnEliminar = new JButton("<html><center>Eliminar<br>Item (F7)</center></html>");
		btnEliminar.setForeground(Color.RED);
		btnEliminar.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnEliminar.setBounds(20, 250, 100, 51); // Al costado de la tabla
		add(btnEliminar);

		btnEliminar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eliminarProductoSeleccionado();
			}
		});

		// --- NUEVO: ATAJO DE TECLADO (F7) ---
		// Esto hace que F7 funcione aunque el foco esté en el campo de texto
		KeyStroke teclaF7 = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		Object keyMapKey = "ELIMINAR_ITEM"; // Una etiqueta interna

		// 1. Mapeamos la tecla F7 a la etiqueta
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(teclaF7, keyMapKey);

		// 2. Mapeamos la etiqueta a la acción de borrar
		this.getActionMap().put(keyMapKey, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eliminarProductoSeleccionado();
			}
		});

		// ... (Código anterior: Botón Eliminar F7) ...

		// --- NUEVO: BOTÓN BUSCAR POR NOMBRE (F5) ---
		JButton btnBuscar = new JButton("<html><center>Buscar<br>Nombre(F5)</center></html>");
		btnBuscar.setForeground(new Color(0, 0, 128)); // Azul oscuro
		btnBuscar.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnBuscar.setBounds(20, 128, 100, 50); // Al lado del de Eliminar
		add(btnBuscar);

		btnBuscar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abrirBusquedaPorNombre();
			}
		});
		

		// --- NUEVO: ATAJO DE TECLADO (F5) ---
		KeyStroke teclaF5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		Object keyBusqueda = "BUSCAR_NOMBRE";

		// 1. Mapear F5
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(teclaF5, keyBusqueda);

		// 2. Asignar Acción
		this.getActionMap().put(keyBusqueda, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirBusquedaPorNombre();
			}
		});
		// ... (Código anterior: Botón Buscar F5) ...

		// --- NUEVO: BOTÓN CONSULTAR PRECIO (F6) ---
		JButton btnPrecio = new JButton("<html><center>Consultar<br>Precio (F6)</center></html>");
		btnPrecio.setForeground(new Color(255, 140, 0)); // Naranja oscuro
		btnPrecio.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnPrecio.setBounds(20, 189, 100, 50); // Ajustá la posición X según tu diseño
		add(btnPrecio);

		btnPrecio.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        abrirConsultaPrecio();
		    }
		});

		// --- NUEVO: ATAJO DE TECLADO (F6) ---
		KeyStroke teclaF6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		Object keyPrecio = "CONSULTAR_PRECIO";

		// 1. Mapear F6
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(teclaF6, keyPrecio);

		// 2. Asignar Acción
		this.getActionMap().put(keyPrecio, new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        abrirConsultaPrecio();
		    }
		});

		// ... (Resto del constructor: Totales y Botón Cobrar) ...

		// 3. TOTAL Y BOTONES (Abajo)
		JLabel lblTextoTotal = new JLabel("TOTAL A PAGAR:");
		lblTextoTotal.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblTextoTotal.setBounds(430, 432, 180, 30);
		add(lblTextoTotal);

		lblTotalPagar = new JLabel("$ 0.00");
		lblTotalPagar.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblTotalPagar.setForeground(new Color(0, 128, 0)); // Verde
		lblTotalPagar.setBounds(620, 430, 150, 30);
		add(lblTotalPagar);

		JButton btnCobrar = new JButton("COBRAR (F12)");
		btnCobrar.setBackground(new Color(50, 205, 50));
		btnCobrar.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnCobrar.setBounds(0, 430, 150, 39);
		add(btnCobrar);

		btnCobrar.addActionListener(e -> cerrarVenta());
		
		// --- NUEVO: ATAJO DE TECLADO (F12) PARA COBRAR ---
		KeyStroke teclaF12 = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
		Object keyCobrar = "COBRAR_VENTA";

		// 1. Mapeamos la tecla F12 a la etiqueta interna
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(teclaF12, keyCobrar);

		// 2. Mapeamos la etiqueta a la acción real
		this.getActionMap().put(keyCobrar, new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        cerrarVenta();
		    }
		});
	}

	// --- LÓGICA DE NEGOCIO ---

	private void procesarEntradaProducto() {
		String entrada = txtBusquedaProducto.getText().trim();
		if (entrada.isEmpty())
			return;

		String codigoBuscado = entrada;
		int cantidad = 1;

		try {
			// Lógica del multiplicador (3*CODE)
			if (entrada.contains("*")) {
				String[] partes = entrada.split("\\*");
				cantidad = Integer.parseInt(partes[0]);
				codigoBuscado = partes[1];
			}

			Producto p = empresa.buscarProducto(codigoBuscado);

			if (p != null) {
				// Agregar al modelo de negocio
				ventaActual.agregarItem(p, cantidad);

				// Actualizar la vista
				actualizarTabla();
				txtBusquedaProducto.setText("");
			} else {
				java.awt.Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "Producto no encontrado.");
				txtBusquedaProducto.selectAll();
			}

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error en formato: " + ex.getMessage());
		}
	}

	private void actualizarTabla() {
		// Limpiamos la tabla visual
		modeloTabla.setRowCount(0);

		// Recorremos la lista real de ítems de la venta
		for (DetalleVenta d : ventaActual.getItems()) {
			Object[] fila = new Object[] { d.getProducto().getCodigoBarra(), d.getProducto().getDescripcion(),
					"$" + d.getProducto().calcularPrecioFinal(), d.getCantidad(), "$" + d.calcularSubtotal() };
			modeloTabla.addRow(fila);
		}

		// Actualizamos el cartel de total
		lblTotalPagar.setText("$ " + ventaActual.getTotal());
	}

	

	private void cerrarVenta() {
	    if (ventaActual.getItems().isEmpty()) {
	        JOptionPane.showMessageDialog(this, "No hay artículos para cobrar.", "Error", JOptionPane.WARNING_MESSAGE);
	        return;
	    }

	    // 1. Preguntar METODO DE PAGO
	    String[] opciones = {"Efectivo", "Débito", "Crédito", "Transferencia"};
	    
	    int seleccion = JOptionPane.showOptionDialog(
	            this,
	            "Seleccione el medio de pago:",
	            "Cerrar Venta - Total: $" + ventaActual.getTotal(),
	            JOptionPane.DEFAULT_OPTION,
	            JOptionPane.QUESTION_MESSAGE,
	            null,
	            opciones,
	            opciones[0] // Efectivo por defecto
	    );

	    // Si cierra la ventana o pone cancelar, salimos sin cobrar
	    if (seleccion == -1) return;

	    boolean cobroExitoso = false;

	    // 2. Procesar según la opción
	    switch (seleccion) {
	        case 0: // EFECTIVO
	            cobroExitoso = procesarPagoEfectivo();
	            break;
	            
	        case 1: // DÉBITO
	        case 2: // CRÉDITO
	        case 3: // TRANSFERENCIA
	            // Aquí podrías pedir número de comprobante o lote si quisieras
	            cobroExitoso = true; // Asumimos que pasó la tarjeta
	            break;
	    }

	    // 3. Si el cobro se confirmó, finalizamos
	    if (cobroExitoso) {
	        // Guardar en el historial
	        empresa.registrarVenta(ventaActual);
	        
	        JOptionPane.showMessageDialog(this, "Venta registrada con éxito!");
	        
	        // Resetear para la próxima
	        this.ventaActual = new Venta(vendedor);
	        actualizarTabla();
	        lblTotalPagar.setText("$ 0.00"); // Resetear label visual
	        txtBusquedaProducto.requestFocus();
	    }
	}

	/**
	 * Método auxiliar para manejar la lógica del dinero y el vuelto
	 */
	private boolean procesarPagoEfectivo() {
	    BigDecimal total = ventaActual.getTotal();
	    
	    while (true) {
	        String input = JOptionPane.showInputDialog(this, 
	                "Total a Pagar: $ " + total + "\n\nIngrese monto entregado por el cliente:", 
	                "Cobro en Efectivo", 
	                JOptionPane.PLAIN_MESSAGE);

	        // Si cancela (null) o deja vacío, cancelamos el cobro
	        if (input == null || input.trim().isEmpty()) {
	            return false;
	        }

	        try {
	            // Intentamos convertir lo que escribió a número
	            BigDecimal pagaCon = new BigDecimal(input);
	            
	            // Verificamos si le alcanza (pago >= total)
	            // compareTo devuelve: -1 (menor), 0 (igual), 1 (mayor)
	            if (pagaCon.compareTo(total) < 0) {
	                java.awt.Toolkit.getDefaultToolkit().beep();
	                JOptionPane.showMessageDialog(this, "Monto insuficiente. Faltan: $ " + total.subtract(pagaCon));
	            } else {
	                // LE ALCANZA -> Calculamos vuelto
	                BigDecimal vuelto = pagaCon.subtract(total);
	                
	                // Mostramos el vuelto en grande
	                String mensajeVuelto = "<html><h1>Su vuelto es: <span style='color:blue'>$ " + vuelto + "</span></h1></html>";
	                
	                JOptionPane.showMessageDialog(this, mensajeVuelto, "Vuelto", JOptionPane.INFORMATION_MESSAGE);
	                return true; // Cobro confirmado
	            }
	            
	        } catch (NumberFormatException e) {
	            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido.");
	        }
	    }
	}

	private void eliminarProductoSeleccionado() {
		// 1. Obtener qué fila está seleccionada en la tabla visual
		int filaSeleccionada = tableDetalle.getSelectedRow();

		if (filaSeleccionada == -1) {
			// No hay nada seleccionado
			java.awt.Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para eliminar.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// 2. Obtener el objeto DetalleVenta correspondiente
		// Como el orden de la tabla visual es el mismo que el de la lista del modelo:
		DetalleVenta detalleABorrar = ventaActual.getItems().get(filaSeleccionada);

		// 3. Confirmación (Opcional, para seguridad)
		int confirm = JOptionPane.showConfirmDialog(this,
				"¿Borrar " + detalleABorrar.getProducto().getDescripcion() + "?", "Confirmar",
				JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {

			// 4. Llamar a la lógica de negocio (Modelo)
			// Esto resta el total y devuelve el stock (método que corregimos antes)
			ventaActual.eliminarItem(detalleABorrar);

			// 5. Actualizar la Vista
			actualizarTabla(); // Refresca la grilla

			// Mensaje en consola para control
			System.out.println("Eliminado: " + detalleABorrar.getProducto().getDescripcion());

			// Volver el foco al buscador para seguir vendiendo rápido
			txtBusquedaProducto.requestFocus();
		}
	}
	private void abrirBusquedaPorNombre() {
	    // 1. Pedir texto de búsqueda
	    String textoBusqueda = JOptionPane.showInputDialog(this, 
	            "Ingrese nombre del producto:", 
	            "Búsqueda Manual (F5)", 
	            JOptionPane.QUESTION_MESSAGE);

	    // Si cancela o deja vacío, salimos
	    if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
	        return;
	    }

	    // 2. Buscar en el modelo
	    List<Producto> resultados = empresa.buscarProductosPorNombre(textoBusqueda);

	    // 3. Evaluar resultados
	    if (resultados.isEmpty()) {
	        java.awt.Toolkit.getDefaultToolkit().beep();
	        JOptionPane.showMessageDialog(this, "No se encontraron productos con: " + textoBusqueda);
	        return;
	    }

	    // --- CAMBIO: SIEMPRE MOSTRAMOS EL SELECTOR ---
	    // No importa si hay 1 o 100, obligamos a elegir para confirmar.
	    
	    Object[] opciones = resultados.toArray();
	    
	    Producto productoSeleccionado = (Producto) JOptionPane.showInputDialog(
	            this,
	            "Seleccione el producto correcto:",
	            "Resultados de Búsqueda",
	            JOptionPane.PLAIN_MESSAGE,
	            null, // Icono default
	            opciones, // La lista de opciones
	            opciones[0] // El primero queda marcado por defecto
	    );

	    // 4. Agregar a la venta (Solo si el usuario le dio OK)
	    if (productoSeleccionado != null) {
	        // Agregamos 1 unidad
	        ventaActual.agregarItem(productoSeleccionado, 1);
	        
	        actualizarTabla();
	        System.out.println("Seleccionado: " + productoSeleccionado.getDescripcion());
	        
	        // Volver el foco al buscador principal para seguir trabajando
	        txtBusquedaProducto.requestFocus();
	    }
	}
	
	private void abrirConsultaPrecio() {
	    // 1. Pedir estrictamente el código
	    String codigoEntrada = JOptionPane.showInputDialog(this, 
	            "Escanee o escriba el Código de Barras:", 
	            "Consulta de Precio (F6)", 
	            JOptionPane.QUESTION_MESSAGE);

	    // Si cancela o deja vacío, no hacemos nada
	    if (codigoEntrada == null || codigoEntrada.trim().isEmpty()) {
	        return;
	    }

	    // 2. Búsqueda Directa (Solo por código)
	    // Esto usa tu HashMap, así que es instantáneo
	    Producto productoEncontrado = empresa.buscarProducto(codigoEntrada);

	    if (productoEncontrado != null) {
	        // --- CASO ÉXITO: MOSTRAR PRECIO ---
	        
	        // Armamos un cartel bonito con HTML básico
	        String mensaje = "<html><center>"
	                + "<h2>" + productoEncontrado.getDescripcion() + "</h2>"
	                + "<p>Código: " + productoEncontrado.getCodigoBarra() + "</p>"
	                + "<p>Stock actual: " + productoEncontrado.getCantidadStock() + "</p>" // Dato extra útil
	                + "<br>"
	                + "<h1 style='color:green; font-size: 20px'>$ " + productoEncontrado.calcularPrecioFinal() + "</h1>"
	                + "</center></html>";

	        JOptionPane.showMessageDialog(this, 
	                mensaje, 
	                "Verificador de Precios", 
	                JOptionPane.INFORMATION_MESSAGE);
	                
	    } else {
	        // --- CASO ERROR: NO EXISTE ---
	        java.awt.Toolkit.getDefaultToolkit().beep(); // Sonido de alerta
	        JOptionPane.showMessageDialog(this, 
	                "No existe ningún producto con el código: " + codigoEntrada, 
	                "Producto No Encontrado", 
	                JOptionPane.ERROR_MESSAGE);
	    }
	    
	    // 3. Siempre devolvemos el foco al campo principal para seguir vendiendo
	    txtBusquedaProducto.requestFocus();
	}
}