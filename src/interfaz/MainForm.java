package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import controlador.ControladorCategoria;
import controlador.ControladorStock;
import controlador.ControladorVenta;
import modelo.Empresa;
import modelo.Funcion;
import modelo.Usuario;

public class MainForm extends JFrame {

    private static final long serialVersionUID = 1L;
    
    // --- REFERENCIAS ---
    private Empresa empresa;
    private Usuario usuarioLogueado;
    private ControladorStock controlador;
    private ControladorCategoria controladorCat;
    private ControladorVenta controladorVenta;
    // --- COMPONENTES VISUALES ---
    private JPanel contentPane;
    private JPanel panelCabecera;
    private JPanel panelCuerpo; // Aquí se intercambian las pantallas

    public MainForm(Empresa empresa, Usuario usuario, ControladorStock cs, ControladorCategoria cCategoria, ControladorVenta cVenta) {
        this.empresa = empresa;
        this.usuarioLogueado = usuario;
        this.controlador= cs;
        this.controladorCat = cCategoria;
        this.controladorVenta = cVenta;

        // Configuración de la Ventana Principal
        setTitle("Sistema de Gestión - " + empresa.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 1. TAMAÑO DEFINITIVO Y BLOQUEO
        setSize(1000, 680); 
        setResizable(false); // Bloqueamos para que el diseño no se rompa
        setLocationRelativeTo(null); // Centrado en monitor

        // 2. LAYOUT PRINCIPAL (BorderLayout)
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new BorderLayout()); 
        setContentPane(contentPane);

        // =============================================================
        // A. CABECERA (NORTE)
        // =============================================================
        panelCabecera = new JPanel();
        panelCabecera.setBackground(new Color(230, 230, 250)); // Lavanda suave
        panelCabecera.setPreferredSize(new Dimension(1000, 60)); // Alto fijo
        panelCabecera.setLayout(null); // Layout null solo para la cabecera simple
        contentPane.add(panelCabecera, BorderLayout.NORTH);

        JLabel lblInfoUser = new JLabel("Operador: " + usuario.getNombreCompleto());
        lblInfoUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInfoUser.setBounds(30, 15, 400, 30);
        panelCabecera.add(lblInfoUser);

        JButton btnLogout = new JButton("Cerrar Sesión");
        // Ajustamos la posición X para la nueva resolución (1000 - 150 aprox)
        btnLogout.setBounds(850, 15, 120, 30); 
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> cerrarSesion());
        panelCabecera.add(btnLogout);

        // =============================================================
        // B. CUERPO (CENTRO)
        // =============================================================
        panelCuerpo = new JPanel();
        // IMPORTANTE: Usamos BorderLayout aquí también.
        // Esto permite que cuando agreguemos 'PanelGestionStock', se estire al 100%.
        panelCuerpo.setLayout(new BorderLayout()); 
        contentPane.add(panelCuerpo, BorderLayout.CENTER);

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

        // Creamos un panel contenedor para los botones del menú
        // Usamos null layout aquí para ubicar los 3 botones manualmente en el centro
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(null);
        panelMenu.setBackground(new Color(245, 246, 250)); // Color de fondo gris claro

        // Coordenadas calculadas para ancho 1000px
        // Ancho Botón: 220px | Espacio: 60px | Total ancho bloque: ~800px
        // Inicio X aprox: 110
        int yBtn = 150; // Altura vertical
        int wBtn = 220;
        int hBtn = 120;
        int gap = 60;
        int xInicio = 110;

        // Botón 1: PUNTO DE VENTA
        JButton btnVenta = crearBotonMenu("PUNTO DE VENTA", xInicio, yBtn, wBtn, hBtn);
        if (usuarioLogueado.puede(Funcion.REGISTRAR_VENTA)) {
            btnVenta.addActionListener(e -> abrirPanelVenta());
        } else {
            deshabilitarBoton(btnVenta);
        }
        panelMenu.add(btnVenta);

        // Botón 2: GESTIÓN DE STOCK
        JButton btnStock = crearBotonMenu("GESTIÓN STOCK", xInicio + wBtn + gap, yBtn, wBtn, hBtn);
        if (usuarioLogueado.puede(Funcion.CARGAR_PRODUCTO)) {
            btnStock.addActionListener(e -> abrirSubmenuStock());
        } else {
            deshabilitarBoton(btnStock);
        }
        panelMenu.add(btnStock);

        // Botón 3: ESTADÍSTICAS
        JButton btnStats = crearBotonMenu("ESTADÍSTICAS", xInicio + (wBtn + gap) * 2, yBtn, wBtn, hBtn);
        if (!usuarioLogueado.puede(Funcion.VER_ESTADISTICAS)) {
            deshabilitarBoton(btnStats);
        }
        panelMenu.add(btnStats);

        // Agregamos el panelMenu al centro del cuerpo
        panelCuerpo.add(panelMenu, BorderLayout.CENTER);
        refrescarPanel();
    }

   
    private void abrirSubmenuStock() {
        panelCuerpo.removeAll();

        // Instanciamos el panel intermedio
        PanelGestionStock panelGestion = new PanelGestionStock(this.controlador, this.controladorCat);
      
        panelGestion.btnVolver.addActionListener(e -> mostrarMenuPrincipal());
        panelGestion.btnAltaProducto.addActionListener(e -> abrirPanelAltaStock());
        panelGestion.btnReposicion.addActionListener(e -> abrirPanelCargaStock());

        panelCuerpo.add(panelGestion, BorderLayout.CENTER);
        refrescarPanel();
    }

    /**
     * PANTALLA 3: FORMULARIO ALTA
     */
    private void abrirPanelAltaStock() {
        panelCuerpo.removeAll();

        PanelAltaStock panelAlta = new PanelAltaStock(empresa, () -> abrirSubmenuStock(), this.controlador,this.controladorCat);
        // panelAlta debería adaptarse también si usa layouts correctos, si no, se verá arriba a la izq.
        // Asumiremos que PanelAltaStock maneja su layout o usaremos un wrapper si es necesario.
        panelCuerpo.add(panelAlta, BorderLayout.CENTER);
        
        refrescarPanel();
    }
    
    public void abrirPanelStockConCodigo(String codigo) {
        panelCuerpo.removeAll();

        PanelFormularioProducto formulario = new PanelFormularioProducto(
            controlador,
            controladorCat,
            codigo, 
            () -> abrirPanelVenta(), 
            () -> abrirPanelVenta()  
        );
        
        panelCuerpo.add(formulario, BorderLayout.CENTER);
        refrescarPanel();
    }
    
    // --- PANTALLAS SECUNDARIAS ---

    private void abrirPanelVenta() {
        panelCuerpo.removeAll();
        PanelVenta panelVenta = new PanelVenta(this.controladorVenta);
        
        panelVenta.btnVolver.addActionListener(e -> {
            panelVenta.guardarSalida();
            mostrarMenuPrincipal();
        });

        panelCuerpo.add(panelVenta, BorderLayout.CENTER);
        refrescarPanel();
    }
    
    private void abrirPanelCargaStock() {
        panelCuerpo.removeAll();

        PanelCargaStock panelCarga = new PanelCargaStock(empresa, controlador,this.controladorCat);
        panelCarga.btnVolver.addActionListener(e -> abrirSubmenuStock());
        
        panelCuerpo.add(panelCarga, BorderLayout.CENTER);
        refrescarPanel();
    }

    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro desea salir?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Aquí llamarías a tu Login nuevamente
            // new Login(empresa).setVisible(true);
        }
    }

    // --- UTILIDADES VISUALES ---

    private void refrescarPanel() {
        panelCuerpo.revalidate();
        panelCuerpo.repaint();
    }

    private JButton crearBotonMenu(String texto, int x, int y, int w, int h) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Letra más grande
        btn.setBounds(x, y, w, h);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void deshabilitarBoton(JButton btn) {
        btn.setEnabled(false);
        btn.setToolTipText("No tienes permisos para acceder a esta sección");
    }
}