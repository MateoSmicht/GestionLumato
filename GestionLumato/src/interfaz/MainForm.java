package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import modelo.Empresa;
import modelo.Funcion; // Para verificar permisos
import modelo.Usuario;

public class MainForm extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	// Referencias
	private Empresa empresa;
	private Usuario usuarioLogueado;

	// Paneles
	private JPanel panelCabecera;
	private JPanel panelCuerpo; // Aquí cambiaremos entre Menú y Venta

	public MainForm(Empresa empresa, Usuario usuario) {
		this.empresa = empresa;
		this.usuarioLogueado = usuario;

		setTitle("Sistema de Gestión - " + empresa.getNombre());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// =============================================================
		// 1. CABECERA (Siempre visible)
		// =============================================================
		panelCabecera = new JPanel();
		panelCabecera.setBackground(new Color(230, 230, 250)); // Color Lavanda suave
		panelCabecera.setBounds(0, 0, 784, 50);
		panelCabecera.setLayout(null);
		contentPane.add(panelCabecera);

		JLabel lblInfoUser = new JLabel("Operador:" + usuario.getNombreCompleto());
		lblInfoUser.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblInfoUser.setBounds(20, 11, 400, 38);
		panelCabecera.add(lblInfoUser);

		JButton btnLogout = new JButton("Cerrar Sesión");
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cerrarSesion();
			}
		});
		btnLogout.setBounds(662, 21, 97, 23);
		panelCabecera.add(btnLogout);

		// =============================================================
		// 2. CUERPO (Donde ocurre la magia)
		// =============================================================
		panelCuerpo = new JPanel();
		panelCuerpo.setBounds(0, 50, 784, 511);
		panelCuerpo.setLayout(null); // Layout absoluto para WindowBuilder
		contentPane.add(panelCuerpo);

		// Al iniciar, mostramos el menú de botones
		mostrarMenu();
	}

	/**
	 * Dibuja los botones del menú principal en el panelCuerpo
	 */
	public void mostrarMenu() {
		// 1. Limpiamos lo que haya (por si veníamos de Venta)
		panelCuerpo.removeAll();

		// 2. Botón IR A VENTAS
		JButton btnVenta = new JButton("PUNTO DE VENTA");
		btnVenta.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnVenta.setBounds(50, 50, 200, 100);

		// Lógica de Permisos: Si no puede vender, deshabilitamos el botón
		if (usuarioLogueado.puede(Funcion.REGISTRAR_VENTA)) {
			btnVenta.setEnabled(true);
			btnVenta.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					abrirPanelVenta();
				}
			});
		} else {
			btnVenta.setEnabled(false);
			btnVenta.setToolTipText("No tienes permisos para vender");
		}
		panelCuerpo.add(btnVenta);

		// 3. Botón STOCK (Ejemplo)
		JButton btnStock = new JButton("GESTIÓN STOCK");
		btnStock.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnStock.setBounds(280, 50, 200, 100);
		// Lógica: Solo si puede cargar productos
		if (usuarioLogueado.puede(Funcion.CARGAR_PRODUCTO)) {
			btnStock.setEnabled(true);
			btnStock.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					abrirMenuStock();
				}
			});
		} else {
			btnStock.setEnabled(false);
		}
		panelCuerpo.add(btnStock);

		// 4. Botón ESTADÍSTICAS (Ejemplo)
		JButton btnStats = new JButton("ESTADÍSTICAS");
		btnStats.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnStats.setBounds(510, 50, 200, 100);

		if (usuarioLogueado.puede(Funcion.VER_ESTADISTICAS)) {
			btnStats.setEnabled(true);
		} else {
			btnStats.setEnabled(false);
		}
		panelCuerpo.add(btnStats);

		// Refrescamos la visual
		panelCuerpo.revalidate();
		panelCuerpo.repaint();
	}

	/**
	 * Carga el PanelStock dentro del cuerpo
	 */
	private void abrirPanelStock() {
		// 1. Limpiamos el panel actual
		panelCuerpo.removeAll();

		// 2. Instanciamos el Panel de Stock
		// Nota: PanelStock no necesita usuarioLogueado, solo la empresa
		PanelAltaStock panelStock = new PanelAltaStock(empresa);
		panelStock.setBounds(0, 0, 784, 501);

		// 3. Conectamos el botón "Volver" del panel con el método mostrarMenu()
		panelStock.btnVolver.addActionListener(e -> abrirMenuStock());

		// 4. Agregamos y refrescamos
		panelCuerpo.add(panelStock);
		panelCuerpo.revalidate();
		panelCuerpo.repaint();
	}

	// En interfaz/MainForm.java

	// Hacé este método PÚBLICO para que los paneles hijos lo puedan llamar
	public void abrirPanelStockConCodigo(String codigo) {
		panelCuerpo.removeAll();

		PanelAltaStock panelStock = new PanelAltaStock(empresa);
		panelStock.setBounds(0, 0, 784, 501);

		// --- ESTO ES LO NUEVO ---
		panelStock.setCodigoPredefinido(codigo);
		// ------------------------

		// Conectamos el botón volver (usando tu lógica de navegación)
		panelStock.btnVolver.addActionListener(e -> mostrarMenu()); // O abrirMenuStock()

		panelCuerpo.add(panelStock);
		panelCuerpo.revalidate();
		panelCuerpo.repaint();
	}

	/**
	 * Carga el PanelVenta dentro del cuerpo
	 */
	// En interfaz/MainForm.java -> método abrirPanelVenta()

	private void abrirPanelVenta() {
		panelCuerpo.removeAll();

		PanelVenta panelVenta = new PanelVenta(empresa, usuarioLogueado);
		panelVenta.setBounds(0, 0, 784, 501);

		// --- LÓGICA DEL BOTÓN VOLVER ---
		panelVenta.btnVolver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// 1. Guardamos el borrador antes de irnos
				panelVenta.guardarSalida();

				// 2. Volvemos al menú
				mostrarMenu();
			}
		});

		panelCuerpo.add(panelVenta);
		panelCuerpo.revalidate();
		panelCuerpo.repaint();
	}

	private void abrirPanelCargaStock() {
		panelCuerpo.removeAll();

		PanelCargaStock panelCarga = new PanelCargaStock(empresa);
		panelCarga.setBounds(0, 0, 784, 501);

		// Conectar botón Volver
		panelCarga.btnVolver.addActionListener(e -> abrirMenuStock());
		panelCuerpo.add(panelCarga);
		panelCuerpo.revalidate();
		panelCuerpo.repaint();
	}

	private void abrirMenuStock() {
		panelCuerpo.removeAll();

		PanelGestionStock panelMenuStock = new PanelGestionStock(empresa);
		panelMenuStock.setBounds(0, 0, 784, 501);

		// --- CONECTAMOS LOS CABLES (LISTENERS) ---

		// Botón VOLVER -> Va al menú principal
		panelMenuStock.btnVolver.addActionListener(e -> mostrarMenu());

		// Botón ALTA -> Abre el PanelStock (Crear)
		panelMenuStock.btnAltaProducto.addActionListener(e -> abrirPanelStock());

		// Botón REPOSICIÓN -> Abre el PanelCargaStock (Sumar)
		panelMenuStock.btnReposicion.addActionListener(e -> abrirPanelCargaStock());

		panelCuerpo.add(panelMenuStock);
		panelCuerpo.revalidate();
		panelCuerpo.repaint();
	}

	private void cerrarSesion() {
		this.dispose();
		// Abrimos Login de nuevo
		LoginWindow login = new LoginWindow(empresa);
		login.setVisible(true);
	}
}