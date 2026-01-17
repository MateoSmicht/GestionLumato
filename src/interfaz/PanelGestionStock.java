package interfaz;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

import modelo.Empresa;
import modelo.Producto;
import controlador.ControladorCategoria;
import controlador.ControladorStock;
import interfaz.dialogos.DialogoEditarProducto;
import interfaz.dialogos.DialogoNuevaCategoria;
import interfaz.dialogos.DialogoUnificar;

public class PanelGestionStock extends JPanel {

    private static final long serialVersionUID = 1L;
    private ControladorStock controlador;
    private ControladorCategoria controladorCat;
    
    // --- BOTONES PÚBLICOS ---
    public JButton btnAltaProducto;
    public JButton btnReposicion;
    public JButton btnVolver;

    // --- COLORES ---
    private final Color COLOR_FONDO = new Color(245, 246, 250);
    private final Color COLOR_HEADER = new Color(44, 62, 80);
    
    private final Color COL_AZUL    = new Color(52, 152, 219); 
    private final Color COL_VERDE   = new Color(39, 174, 96);  
    private final Color COL_NARANJA = new Color(230, 126, 34); 
    private final Color COL_AMARILLO= new Color(241, 196, 15); 
    private final Color COL_VIOLETA = new Color(142, 68, 173); 

    public PanelGestionStock(ControladorStock cStock, ControladorCategoria controladorCat) {
    	
        this.controlador = cStock; 
        this.controladorCat= controladorCat;
        
        // 1. LAYOUT PRINCIPAL: BorderLayout
        // Esto asegura que el Header se quede arriba y el resto ocupe TODO el espacio
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);

        // =========================================================
        // A. HEADER (FIJO ARRIBA)
        // =========================================================
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setPreferredSize(new Dimension(0, 80)); // Alto fijo
        panelHeader.setBorder(new EmptyBorder(0, 30, 0, 30));
        add(panelHeader, BorderLayout.NORTH);

        JLabel lblTitulo = new JLabel("MENÚ DE STOCK");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        panelHeader.add(lblTitulo, BorderLayout.WEST);

        btnVolver = new JButton("Volver al Inicio");
        btnVolver.setPreferredSize(new Dimension(150, 40));
        estilizarBoton(btnVolver, new Color(149, 165, 166), Color.WHITE);
        
        JPanel panelBtnVolver = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 20));
        panelBtnVolver.setOpaque(false);
        panelBtnVolver.add(btnVolver);
        panelHeader.add(panelBtnVolver, BorderLayout.EAST);

        // =========================================================
        // B. GRID DE BOTONES (ELÁSTICO)
        // =========================================================
        JPanel panelGrid = new JPanel(new GridBagLayout()); // <--- LA CLAVE
        panelGrid.setBackground(COLOR_FONDO);
        panelGrid.setBorder(new EmptyBorder(20, 20, 20, 20)); // Margen externo
        add(panelGrid, BorderLayout.CENTER);

        // --- FILA 1 ---
        btnAltaProducto = crearBoton("ALTA DE PRODUCTO", "Crear artículos nuevos", COL_AZUL, true);
        btnReposicion = crearBoton("REPOSICIÓN", "Cargar mercadería existente", COL_VERDE, true);
        
        // (x, y, ancho, alto) -> El ancho es relativo en la grilla
        // Fila 1 ocupa 2 espacios cada botón (para alinearse con la fila 3 que tiene 3 botones)
        agregarBoton(panelGrid, btnAltaProducto, 0, 0, 3, 1); 
        agregarBoton(panelGrid, btnReposicion,   3, 0, 3, 1);

        // --- FILA 2 ---
        JButton btnUnificar = crearBoton("FUSIONAR DUPLICADOS", "Corregir códigos", COL_NARANJA, false);
        btnUnificar.addActionListener(e -> new DialogoUnificar((JFrame)SwingUtilities.getWindowAncestor(this), cStock).setVisible(true));
        
        JButton btnCategoria = crearBoton("GESTIONAR CATEGORÍAS", "Familias y Rubros", COL_AMARILLO, false);
        btnCategoria.setForeground(new Color(44, 62, 80));
        btnCategoria.addActionListener(e -> new DialogoNuevaCategoria((JFrame)SwingUtilities.getWindowAncestor(this), this.controladorCat).setVisible(true));
        

        agregarBoton(panelGrid, btnUnificar,  0, 1, 3, 1);
        agregarBoton(panelGrid, btnCategoria, 3, 1, 3, 1);

        // --- FILA 3 (3 Botones) ---
        JButton btnConsulta = crearBoton("CONSULTA", "Buscador", COL_VIOLETA, true);
        btnConsulta.addActionListener(e -> {
            JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Buscador", true);
            d.setSize(1000, 700); d.setLocationRelativeTo(null);
            d.add(new PanelConsultaStock(cStock, this.controladorCat)); d.setVisible(true);
        });

        JButton btnEditar = crearBoton("EDITAR", "Producto", COL_NARANJA, false);
        btnEditar.addActionListener(e -> {
            String cod = JOptionPane.showInputDialog(this, "Código a editar:");
            if(cod != null && !cod.isEmpty()) {
                Producto p = cStock.buscarProducto(cod);
                if(p!=null) new DialogoEditarProducto((JFrame)SwingUtilities.getWindowAncestor(this),cStock, p, null).setVisible(true);
                else JOptionPane.showMessageDialog(this, "No encontrado");
            }
        });

        JButton btnImportar = crearBoton("IMPORTAR", "Excel / CSV", COL_AZUL, false);
        btnImportar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, controlador.importarProductosDesdeCSV(fc.getSelectedFile()));
            }
        });

        // Aquí usamos ancho 2 para cada botón, total 6 columnas
        agregarBoton(panelGrid, btnConsulta, 0, 2, 2, 1);
        agregarBoton(panelGrid, btnEditar,   2, 2, 2, 1);
        agregarBoton(panelGrid, btnImportar, 4, 2, 2, 1);
    }

    // --- MAGIA DE GRIDBAGLAYOUT ---
    // Este método es el que obliga a los botones a crecer
    private void agregarBoton(JPanel panel, JComponent componente, int x, int y, int ancho, int alto) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = ancho;
        gbc.gridheight = alto;
        
        gbc.fill = GridBagConstraints.BOTH; // ESTO HACE QUE CREZCA EN ANCHO Y ALTO
        gbc.weightx = 1.0; // ESTO PIDE "DAME TODO EL ESPACIO HORIZONTAL SOBRANTE"
        gbc.weighty = 1.0; // ESTO PIDE "DAME TODO EL ESPACIO VERTICAL SOBRANTE"
        
        gbc.insets = new Insets(10, 10, 10, 10); // Márgenes entre botones (gap)
        
        panel.add(componente, gbc);
    }

    // --- ESTILOS ---

    private JButton crearBoton(String titulo, String subtitulo, Color bg, boolean grande) {
        String html = grande 
            ? "<html><center><h2>" + titulo + "</h2><font size=5>" + subtitulo + "</font></center></html>"
            : "<html><center><h3>" + titulo + "</h3>" + subtitulo + "</center></html>";
            
        JButton btn = new JButton(html);
        estilizarBoton(btn, bg, Color.WHITE);
        return btn;
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}