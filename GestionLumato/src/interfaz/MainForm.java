package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import modelo.Empresa;
import modelo.Funcion;
import modelo.Usuario;

public class MainForm extends JFrame {

    private static final long serialVersionUID = 1L;
    
    // --- REFERENCIAS ---
    private Empresa empresa;
    private Usuario usuarioLogueado;

    // --- COMPONENTES VISUALES ---
    private JPanel contentPane;
    private JPanel panelCabecera;
    private JPanel panelCuerpo; // Aquí se intercambian las pantallas

    public MainForm(Empresa empresa, Usuario usuario) {
        this.empresa = empresa;
        this.usuarioLogueado = usuario;

        // Configuración de la Ventana Principal
        setTitle("Sistema de Gestión - " + empresa.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        setLocationRelativeTo(null); // Centrar en pantalla

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // =============================================================
        // 1. CABECERA (Fija arriba)
        // =============================================================
        panelCabecera = new JPanel();
        panelCabecera.setBackground(new Color(230, 230, 250)); // Lavanda suave
        panelCabecera.setBounds(0, 0, 784, 50);
        panelCabecera.setLayout(null);
        contentPane.add(panelCabecera);

        JLabel lblInfoUser = new JLabel("Operador: " + usuario.getNombreCompleto());
        lblInfoUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfoUser.setBounds(20, 11, 400, 28);
        panelCabecera.add(lblInfoUser);

        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setBounds(630, 12, 120, 25);
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> cerrarSesion());
        panelCabecera.add(btnLogout);

        // =============================================================
        // 2. CUERPO (Dinámico)
        // =============================================================
        panelCuerpo = new JPanel();
        panelCuerpo.setBounds(0, 50, 784, 511);
        panelCuerpo.setLayout(null); 
        contentPane.add(panelCuerpo);

        // Al iniciar, mostramos el Dashboard
        mostrarMenuPrincipal();
    }

    // =============================================================
    // MÉTODOS DE NAVEGACIÓN (El Cerebro de la App)
    // =============================================================

    /**
     * PANTALLA 1: MENÚ PRINCIPAL (Dashboard)
     */
    public void mostrarMenuPrincipal() {
        panelCuerpo.removeAll();

        // Botón 1: PUNTO DE VENTA
        JButton btnVenta = crearBotonMenu("PUNTO DE VENTA", 50, 50);
        if (usuarioLogueado.puede(Funcion.REGISTRAR_VENTA)) {
            btnVenta.addActionListener(e -> abrirPanelVenta());
        } else {
            deshabilitarBoton(btnVenta);
        }
        panelCuerpo.add(btnVenta);

        // Botón 2: GESTIÓN DE STOCK (Lleva al submenú de colores)
        JButton btnStock = crearBotonMenu("GESTIÓN STOCK", 280, 50);
        if (usuarioLogueado.puede(Funcion.CARGAR_PRODUCTO)) {
            btnStock.addActionListener(e -> abrirSubmenuStock());
        } else {
            deshabilitarBoton(btnStock);
        }
        panelCuerpo.add(btnStock);

        // Botón 3: ESTADÍSTICAS
        JButton btnStats = crearBotonMenu("ESTADÍSTICAS", 510, 50);
        if (!usuarioLogueado.puede(Funcion.VER_ESTADISTICAS)) {
            deshabilitarBoton(btnStats);
        }
        panelCuerpo.add(btnStats);

        refrescarPanel();
    }

    /**
     * PANTALLA 2: SUBMENÚ STOCK (Panel de botones de colores)
     */
    private void abrirSubmenuStock() {
        panelCuerpo.removeAll();

        // Instanciamos el panel intermedio
        PanelGestionStock panelGestion = new PanelGestionStock(empresa);
        panelGestion.setBounds(0, 0, 784, 511);

        // --- CONEXIONES DE NAVEGACIÓN ---

        // 1. Botón "Volver al Inicio" (Header del panel)
        panelGestion.btnVolver.addActionListener(e -> mostrarMenuPrincipal());

        // 2. Botón "Alta de Producto" (Azul) -> Va al formulario
        panelGestion.btnAltaProducto.addActionListener(e -> abrirPanelAltaStock());

        // 3. Botón "Reposición" (Verde) -> Va al buscador/tabla en modo pantalla completa
        panelGestion.btnReposicion.addActionListener(e -> abrirPanelCargaStock());

        panelCuerpo.add(panelGestion);
        refrescarPanel();
    }

    /**
     * PANTALLA 3: FORMULARIO ALTA (Reutilizando la lógica DRY)
     */
    private void abrirPanelAltaStock() {
        panelCuerpo.removeAll();

        // Usamos el wrapper PanelAltaStock
        PanelAltaStock panelAlta = new PanelAltaStock(empresa);
        panelAlta.setBounds(0, 0, 784, 511);

        // Configuración de retorno:
        // Si cancela o vuelve, regresa al Submenú de Stock (Colores), no al principal.
        panelAlta.accionVolverExterna = () -> abrirSubmenuStock();

        panelCuerpo.add(panelAlta);
        refrescarPanel();
    }
    
    
    public void abrirPanelStockConCodigo(String codigo) {
        panelCuerpo.removeAll();

        // Usamos el Formulario Maestro directamente para mayor flexibilidad
        PanelFormularioProducto formulario = new PanelFormularioProducto(
            empresa, 
            codigo, 
            () -> abrirPanelVenta(), // Al guardar -> Volver a vender
            () -> abrirPanelVenta()  // Al cancelar -> Volver a vender
        );
        
        formulario.setBounds(0, 0, 784, 511);
        panelCuerpo.add(formulario);
        refrescarPanel();
    }
    
    // --- STUBS (Marcadores de posición) ---

   
    private void abrirPanelVenta() {
		panelCuerpo.removeAll();
		PanelVenta panelVenta = new PanelVenta(empresa, usuarioLogueado);
		panelVenta.setBounds(0, 0, 784, 501);
		panelVenta.btnVolver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelVenta.guardarSalida();
				mostrarMenuPrincipal();

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
		panelCarga.btnVolver.addActionListener(e -> abrirSubmenuStock());
		panelCuerpo.add(panelCarga);
		panelCuerpo.revalidate();
		panelCuerpo.repaint();
	}

    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro desea salir?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // new Login(empresa).setVisible(true);
        }
    }

    // --- UTILIDADES VISUALES ---

    private void refrescarPanel() {
        panelCuerpo.revalidate();
        panelCuerpo.repaint();
    }

    private JButton crearBotonMenu(String texto, int x, int y) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBounds(x, y, 200, 100);
        btn.setFocusPainted(false);
        return btn;
    }

    private void deshabilitarBoton(JButton btn) {
        btn.setEnabled(false);
        btn.setToolTipText("No tienes permisos para acceder a esta sección");
    }
}