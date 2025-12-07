package interfaz;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;

import controlador.ControladorVenta;
import modelo.Empresa;
import modelo.Usuario;
import modelo.Producto;
import modelo.Venta;
import modelo.DetalleVenta;

public class PanelVenta extends JPanel {

	private static final long serialVersionUID = 1L;

	// --- EL CEREBRO ---
	private ControladorVenta controlador;

	// --- ESTADO ---
	private boolean modoBulto = false;

	// --- COMPONENTES VISUALES ---
	private JTextField txtBusquedaProducto;
	private JTable tableDetalle;
	private DefaultTableModel modeloTabla;
	private JLabel lblTotalPagar;
	private JLabel lblInfoCantidad;

	public JButton btnVolver;
	private JButton btnModoBulto;

	// --- COLORES DE DISEÑO (FLAT UI) ---
	private final Color COLOR_FONDO = new Color(245, 246, 250); // Gris muy suave
	private final Color COLOR_HEADER = new Color(44, 62, 80); // Azul oscuro profesional
	private final Color COLOR_ACCENT = new Color(52, 152, 219); // Azul brillante
	private final Color COLOR_VERDE = new Color(39, 174, 96); // Verde Esmeralda
	private final Color COLOR_ROJO = new Color(231, 76, 60); // Rojo suave
	private final Color COLOR_NARANJA = new Color(230, 126, 34); // Naranja

	/**
	 * Constructor
	 */
	public PanelVenta(Empresa empresa, Usuario vendedor) {
		this.controlador = new ControladorVenta(empresa, vendedor);

		setLayout(null);
		setBackground(COLOR_FONDO); // Fondo general
		setBounds(0, 0, 784, 500);

		// =========================================================
		// 1. PANEL CABECERA (Fondo Azul Oscuro)
		// =========================================================
		JPanel panelHeader = new JPanel();
		panelHeader.setLayout(null);
		panelHeader.setBackground(COLOR_HEADER);
		panelHeader.setBounds(0, 0, 784, 80);
		add(panelHeader);

		JLabel lblBuscar = new JLabel("CÓDIGO DE BARRAS:");
		lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblBuscar.setForeground(Color.WHITE);
		lblBuscar.setBounds(20, 15, 200, 20);
		panelHeader.add(lblBuscar);

		txtBusquedaProducto = new JTextField();
		txtBusquedaProducto.setBounds(20, 35, 450, 35);
		txtBusquedaProducto.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtBusquedaProducto.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding interno
		panelHeader.add(txtBusquedaProducto);

		// Al presionar Enter
		txtBusquedaProducto.addActionListener(e -> agregarProductoPorInput());

		// Indicador de cantidad (1*)
		lblInfoCantidad = new JLabel("1*");
		lblInfoCantidad.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblInfoCantidad.setForeground(SystemColor.activeCaption); 
		lblInfoCantidad.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfoCantidad.setBounds(480, 35, 60, 35);
		panelHeader.add(lblInfoCantidad);

		// Botón Volver (Estilo Flat)
		btnVolver = new JButton("Volver al Menú");
		btnVolver.setBounds(632, 37, 130, 35);
		estilizarBoton(btnVolver, new Color(149, 165, 166), Color.WHITE); // Gris
		panelHeader.add(btnVolver);

		// =========================================================
		// 2. BARRA LATERAL DE BOTONES (Izquierda)
		// =========================================================

		int btnY = 100;
		int btnGap = 60; // Espacio entre botones

		// F7 - Modo Bulto
		btnModoBulto = new JButton("<html><center>Modo Bulto(F7)<br><font size=2>Desactivado</font></center></html>");
		btnModoBulto.setBounds(333, 83, 130, 35);
		estilizarBoton(btnModoBulto, Color.LIGHT_GRAY, Color.BLACK); // Gris desactivado
		btnModoBulto.addActionListener(e -> toggleModoBulto());
		add(btnModoBulto);

		// =========================================================
		// 3. TABLA CENTRAL (Estilizada)
		// =========================================================

		String[] columnas = { "Cód.", "Descripción", "Unidad", "Factor", "Precio Unit.", "Cant.", "Subtotal" };
		modeloTabla = new DefaultTableModel(columnas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 5;
			}
		};

		// Listener de edición (Lógica original)
		modeloTabla.addTableModelListener(e -> {
			if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
				int fila = e.getFirstRow();
				int columna = e.getColumn();
				if (columna == 5 && fila >= 0) {
					try {
						Object valor = modeloTabla.getValueAt(fila, columna);
						int nuevaCant = Integer.parseInt(valor.toString());
						controlador.modificarCantidadItem(fila, nuevaCant);
						SwingUtilities.invokeLater(() -> {
							actualizarTabla();
							enfocarBuscador();
						});
					} catch (Exception ex) {
						/* Ignorar error */ }
				}
			}
		});

		tableDetalle = new JTable(modeloTabla);

		// --- ESTILO VISUAL DE LA TABLA ---
		tableDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tableDetalle.setRowHeight(30); // Filas más altas
		tableDetalle.setShowGrid(true);
		tableDetalle.setGridColor(new Color(230, 230, 230));
		tableDetalle.setSelectionBackground(new Color(220, 230, 241)); // Selección suave
		tableDetalle.setSelectionForeground(Color.BLACK);

		// Header de la tabla (Azul y negrita)
		JTableHeader header = tableDetalle.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 13));
		header.setBackground(COLOR_HEADER);
		header.setForeground(Color.WHITE);
		header.setOpaque(true);

		// Alineación centrada para columnas numéricas
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		tableDetalle.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tableDetalle.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tableDetalle.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tableDetalle.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

		// --- AJUSTE DE ANCHO DE COLUMNAS (AQUÍ ESTÁ EL CAMBIO) ---
		tableDetalle.getColumnModel().getColumn(0).setPreferredWidth(100);
		tableDetalle.getColumnModel().getColumn(1).setPreferredWidth(250); // <--- Descripción ANCHA
		tableDetalle.getColumnModel().getColumn(2).setPreferredWidth(50);
		tableDetalle.getColumnModel().getColumn(3).setPreferredWidth(40);
		tableDetalle.getColumnModel().getColumn(4).setPreferredWidth(80);
		tableDetalle.getColumnModel().getColumn(5).setPreferredWidth(40);
		tableDetalle.getColumnModel().getColumn(6).setPreferredWidth(80);

		JScrollPane scrollPane = new JScrollPane(tableDetalle);
		scrollPane.setBounds(29, 121, 731, 279);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Borde sutil
		add(scrollPane);

		// =========================================================
		// 4. PANEL INFERIOR (Totales y Cobrar)
		// =========================================================
		JPanel panelFooter = new JPanel();
		panelFooter.setLayout(null);
		panelFooter.setBackground(Color.WHITE); // Blanco para resaltar el dinero
		panelFooter.setBounds(0, 420, 784, 80);
		panelFooter.setBorder(new LineBorder(new Color(220, 220, 220), 1));
		add(panelFooter);

		JLabel lblTextoTotal = new JLabel("TOTAL A PAGAR:");
		lblTextoTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblTextoTotal.setForeground(Color.GRAY);
		lblTextoTotal.setBounds(380, 20, 180, 40);
		panelFooter.add(lblTextoTotal);

		lblTotalPagar = new JLabel("$ 0.00");
		lblTotalPagar.setFont(new Font("Segoe UI", Font.BOLD, 36)); // Gigante
		lblTotalPagar.setForeground(COLOR_VERDE);
		lblTotalPagar.setBounds(540, 15, 230, 50);
		panelFooter.add(lblTotalPagar);

		JButton btnCobrar = new JButton("COBRAR (F12)");
		btnCobrar.setBounds(20, 15, 200, 50);
		estilizarBoton(btnCobrar, COLOR_VERDE, Color.WHITE);
		btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 20)); // Más grande
		btnCobrar.addActionListener(e -> iniciarProcesoCobro());
		panelFooter.add(btnCobrar);

		// F5 - Buscar
		JButton btnBuscar = new JButton("<html><center>BUSCAR (F5)<br><font size=2>Por Nombre</font></center></html>");
		btnBuscar.setBounds(48, 83, 139, 35);
		add(btnBuscar);
		estilizarBoton(btnBuscar, COLOR_ACCENT, Color.WHITE);

		// F6 - Precio
		JButton btnPrecio = new JButton("<html><center>PRECIO (F6)<br><font size=2>Consultar</font></center></html>");
		btnPrecio.setBounds(197, 83, 130, 35);
		add(btnPrecio);
		estilizarBoton(btnPrecio, COLOR_NARANJA, Color.WHITE);

		// F8 - Eliminar
		JButton btnEliminar = new JButton(
				"<html><center>ELIMINAR (F8)<br><font size=2>Borrar Ítem</font></center></html>");
		btnEliminar.setBounds(473, 83, 130, 35);
		add(btnEliminar);
		estilizarBoton(btnEliminar, COLOR_ROJO, Color.WHITE);
		btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
		btnPrecio.addActionListener(e -> abrirConsultaPrecio());
		btnBuscar.addActionListener(e -> abrirBusquedaPorNombre());

		// =========================================================
		// 5. ATAJOS DE TECLADO
		// =========================================================
		setupAtajo(KeyEvent.VK_F1, "F1", e -> enfocarBuscador());
		setupAtajo(KeyEvent.VK_F5, "F5", e -> abrirBusquedaPorNombre());
		setupAtajo(KeyEvent.VK_F6, "F6", e -> abrirConsultaPrecio());
		setupAtajo(KeyEvent.VK_F7, "F7", e -> toggleModoBulto());
		setupAtajo(KeyEvent.VK_F8, "F8", e -> eliminarProductoSeleccionado());
		setupAtajo(KeyEvent.VK_F12, "F12", e -> iniciarProcesoCobro());

		txtBusquedaProducto.requestFocus();
		verificarSesionPendiente();
	}

	// --- MÉTODO AUXILIAR DE DISEÑO ---
	private void estilizarBoton(JButton btn, Color bg, Color fg) {
		btn.setBackground(bg);
		btn.setForeground(fg);
		btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btn.setFocusPainted(false); // Quita el recuadro feo de foco
		btn.setBorderPainted(false); // Estilo Flat
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Manito al pasar el mouse
	}

	// ========================================================================
	// MÉTODOS DE LÓGICA (CON CORRECCIÓN DE UNIDAD/BULTO)
	// ========================================================================

	private void enfocarBuscador() {
		txtBusquedaProducto.requestFocusInWindow();
		txtBusquedaProducto.selectAll();
	}

	private void toggleModoBulto() {
		this.modoBulto = !this.modoBulto;

		if (this.modoBulto) {
			estilizarBoton(btnModoBulto, new Color(46, 204, 113), Color.WHITE); // Verde activo
			btnModoBulto.setText("<html><center>Modo Bulto(F7)<br>ACTIVO</center></html>");
		} else {
			estilizarBoton(btnModoBulto, Color.LIGHT_GRAY, Color.BLACK); // Gris inactivo
			btnModoBulto.setText("<html><center>Modo Bulto(F7)<br>Desactivado</center></html>");
		}
		enfocarBuscador();
	}

	private void actualizarIndicadorCantidad(String entrada) {
		if (entrada.contains("*")) {
			try {
				String cantStr = entrada.split("\\*")[0];
				lblInfoCantidad.setText(cantStr + "*");
			} catch (Exception e) {
				lblInfoCantidad.setText("1*");
			}
		} else {
			lblInfoCantidad.setText("1*");
		}
	}

	private void agregarProductoPorInput() {
		try {
			String entrada = txtBusquedaProducto.getText().trim();
			// Lógica con modoBulto
			String mensaje = controlador.agregarPorInput(entrada, this.modoBulto);
			actualizarIndicadorCantidad(entrada);
			actualizarTabla();

			List<DetalleVenta> items = controlador.getVentaActual().getItems();
			if (!items.isEmpty()) {
				DetalleVenta ultimo = items.get(items.size() - 1);
				txtBusquedaProducto.setText(ultimo.getProducto().getDescripcion());
				txtBusquedaProducto.selectAll();
			} else {
				txtBusquedaProducto.setText("");
			}
			System.out.println(mensaje);
		} catch (Exception e) {
			java.awt.Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			txtBusquedaProducto.selectAll();
		}
	}

	private void abrirBusquedaPorNombre() {
		String texto = JOptionPane.showInputDialog(this, "Nombre del producto:", "Buscar (F5)",
				JOptionPane.QUESTION_MESSAGE);
		if (texto == null || texto.trim().isEmpty())
			return;

		List<Producto> resultados = controlador.buscarPorNombre(texto);

		if (resultados.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No encontrado.");
			return;
		}

		Object[] opciones = resultados.toArray();
		Producto seleccionado = (Producto) JOptionPane.showInputDialog(this, "Seleccione:", "Resultados",
				JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

		if (seleccionado != null) {
			try {
				controlador.agregarPorInput(seleccionado.getCodigoBarra(), false); // Forzamos unidad
				actualizarTabla();
				txtBusquedaProducto.setText(seleccionado.getDescripcion());
				txtBusquedaProducto.selectAll();
				txtBusquedaProducto.requestFocus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void abrirConsultaPrecio() {
		String codigo = JOptionPane.showInputDialog(this, "Código de Barras:", "Consultar Precio (F6)",
				JOptionPane.QUESTION_MESSAGE);
		if (codigo == null || codigo.trim().isEmpty())
			return;

		Producto p = controlador.buscarPorCodigo(codigo);
		if (p != null) {
			String html = "<html><center><h2>" + p.getDescripcion() + "</h2>" + "<h1 style='color:green'>$ "
					+ p.calcularPrecioFinal() + "</h1>" + "<p>Unidad: " + p.getNombreUnidad() + " (x" + p.getFactor()
					+ ")</p>" + "<p>Stock: " + p.getCantidadStock() + "</p></center></html>";
			JOptionPane.showMessageDialog(this, html, "Precio", JOptionPane.INFORMATION_MESSAGE);
		} else {
			java.awt.Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, "Producto inexistente.");
		}
		enfocarBuscador();
	}

	private void eliminarProductoSeleccionado() {
		int fila = tableDetalle.getSelectedRow();
		if (fila == -1) {
			JOptionPane.showMessageDialog(this, "Seleccione un ítem de la tabla.");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "¿Borrar ítem?", "Confirmar", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			controlador.eliminarItem(fila);
			actualizarTabla();
			txtBusquedaProducto.setText("");
			txtBusquedaProducto.requestFocus();
		}
	}

	private void iniciarProcesoCobro() {
		Venta v = controlador.getVentaActual();
		if (v.getItems().isEmpty()) {
			JOptionPane.showMessageDialog(this, "No hay productos.");
			return;
		}

		String[] opciones = { "Efectivo", "Débito", "Crédito", "Transferencia" };
		int seleccion = JOptionPane.showOptionDialog(this, "Medio de Pago:", "Total: $" + v.getTotal(),
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

		if (seleccion == -1)
			return;

		boolean pagado = false;
		if (seleccion == 0)
			pagado = procesarPagoEfectivo();
		else
			pagado = true;

		if (pagado) {
			controlador.finalizarVenta();
			JOptionPane.showMessageDialog(this, "Venta Exitosa!");
			controlador.nuevaVenta();
			actualizarTabla();
			lblInfoCantidad.setText("1*");
			txtBusquedaProducto.setText("");
			txtBusquedaProducto.requestFocus();
		}
	}

	private boolean procesarPagoEfectivo() {
		while (true) {
			String input = JOptionPane.showInputDialog(this,
					"Total: $" + controlador.getVentaActual().getTotal() + "\nCon cuánto paga?");
			if (input == null)
				return false;
			try {
				BigDecimal pago = new BigDecimal(input);
				BigDecimal vuelto = controlador.calcularVuelto(pago);
				JOptionPane.showMessageDialog(this,
						"<html><h1>Vuelto: <span style='color:blue'>$ " + vuelto + "</span></h1></html>");
				return true;
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(this, "Monto insuficiente.");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Número inválido.");
			}
		}
	}

	// --- CORRECCIÓN CLAVE EN ACTUALIZAR TABLA ---
	// Usamos los datos del DETALLE (d.getNombreUnidadSnapshot) y no del Producto
	// genérico
	private void actualizarTabla() {
		modeloTabla.setRowCount(0);
		Venta v = controlador.getVentaActual();
		for (DetalleVenta d : v.getItems()) {
			modeloTabla.addRow(new Object[] { d.getProducto().getCodigoBarra(), d.getProducto().getDescripcion(),
					d.getNombreUnidadSnapshot(), // Muestra si fue CAJA o UNI
					d.getFactorSnapshot(), // Muestra 12 o 1
					"$" + d.getPrecioUnitarioSnapshot(), d.getCantidad(), "$" + d.calcularSubtotal() });
		}
		lblTotalPagar.setText("$ " + v.getTotal());
	}

	private void verificarSesionPendiente() {
		if (controlador.existeVentaPendiente()) {
			int resp = JOptionPane.showConfirmDialog(this, "Venta pendiente. ¿Recuperar?", "Aviso",
					JOptionPane.YES_NO_OPTION);
			if (resp == JOptionPane.YES_OPTION) {
				controlador.restaurarVentaPendiente();
				actualizarTabla();
			} else {
				controlador.descartarVentaPendiente();
			}
		}
	}

	public void guardarSalida() {
		controlador.guardarVentaEnEspera();
	}

	private void setupAtajo(int keyEvent, String nombre, ActionListener accion) {
		KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent, 0);
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, nombre);
		getActionMap().put(nombre, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				accion.actionPerformed(e);
			}
		});
	}
}