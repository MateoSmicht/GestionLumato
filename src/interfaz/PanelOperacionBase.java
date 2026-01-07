package interfaz;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;

public abstract class PanelOperacionBase extends JPanel {

    // Componentes que los hijos necesitarán acceder
    protected JTextField txtBusqueda;
    protected JTable tableDetalle;
    protected DefaultTableModel modeloTabla;
    protected JLabel lblTotalInfo;
    protected JLabel lblInfoCantidad; // El famoso "1*"
    protected JButton btnVolver;
    protected JButton btnModoBulto;
    
    // Estado compartido
    protected boolean modoBulto = false;

    // Colores constantes
    protected final Color COLOR_HEADER = new Color(44, 62, 80);
    protected final Color COLOR_VERDE = new Color(39, 174, 96);
    
    public PanelOperacionBase() {
        setLayout(null);
        setBackground(new Color(245, 246, 250)); // Fondo gris suave
        setBounds(0, 0, 784, 500);
        
        inicializarUI();
        configurarAtajos();
    }

    private void inicializarUI() {
        // --- 1. HEADER COMÚN ---
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBounds(0, 0, 784, 80);
        add(panelHeader);

        JLabel lblTitulo = new JLabel(getTituloOperacion()); // <--- Abstracto
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 10, 400, 30);
        panelHeader.add(lblTitulo);

        // Buscador
        txtBusqueda = new JTextField();
        txtBusqueda.setBounds(20, 40, 450, 35);
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtBusqueda.addActionListener(e -> onEnterBuscador()); // <--- Abstracto
        panelHeader.add(txtBusqueda);
        
        // Indicador 1*
        lblInfoCantidad = new JLabel("1*");
        lblInfoCantidad.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblInfoCantidad.setForeground(new Color(241, 196, 15));
        lblInfoCantidad.setBounds(480, 40, 60, 35);
        panelHeader.add(lblInfoCantidad);

        // Botón Volver
        btnVolver = new JButton("Volver");
        btnVolver.setBounds(630, 25, 130, 35);
        estilizarBoton(btnVolver, new Color(149, 165, 166), Color.WHITE);
        panelHeader.add(btnVolver);

        // --- 2. TABLA COMÚN ---
        modeloTabla = new DefaultTableModel(getColumnasTabla(), 0) { // <--- Abstracto
            public boolean isCellEditable(int row, int col) { return isColumnaEditable(col); }
        };
        
        // Listener de edición de tabla genérico
        modeloTabla.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                onTablaEditada(e.getFirstRow(), e.getColumn()); // <--- Abstracto
            }
        });

        tableDetalle = new JTable(modeloTabla);
        configurarEstiloTabla();
        
        JScrollPane scroll = new JScrollPane(tableDetalle);
        scroll.setBounds(170, 100, 590, 300);
        add(scroll);

        // --- 3. BOTONES LATERALES ---
        crearBotonLateral("F5 - BUSCAR", 100, new Color(52, 152, 219), e -> abrirBusquedaAvanzada());
        crearBotonLateral("F6 - PRECIO", 160, new Color(230, 126, 34), e -> abrirConsultaPrecio());
        
        btnModoBulto = crearBotonLateral("F7 - BULTO", 220, Color.LIGHT_GRAY, e -> toggleModoBulto());
        
        crearBotonLateral("F8 - BORRAR", 280, new Color(231, 76, 60), e -> eliminarItemSeleccionado());

        // --- 4. FOOTER ---
        JPanel panelFooter = new JPanel(null);
        panelFooter.setBackground(Color.WHITE);
        panelFooter.setBounds(0, 420, 784, 80);
        add(panelFooter);

        lblTotalInfo = new JLabel("TOTAL:");
        lblTotalInfo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalInfo.setBounds(20, 20, 400, 40);
        panelFooter.add(lblTotalInfo);

        JButton btnConfirmar = new JButton("CONFIRMAR (F12)");
        btnConfirmar.setBounds(500, 15, 250, 50);
        estilizarBoton(btnConfirmar, COLOR_VERDE, Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnConfirmar.addActionListener(e -> confirmarOperacion()); // <--- Abstracto
        panelFooter.add(btnConfirmar);
    }

    // --- MÉTODOS QUE LOS HIJOS DEBEN IMPLEMENTAR (El "Contrato") ---
    protected abstract String getTituloOperacion();
    protected abstract String[] getColumnasTabla();
    protected abstract boolean isColumnaEditable(int col);
    protected abstract void onEnterBuscador();
    protected abstract void abrirBusquedaAvanzada(); // F5
    protected abstract void abrirConsultaPrecio();   // F6
    protected abstract void eliminarItemSeleccionado(); // F8
    protected abstract void confirmarOperacion();    // F12
    protected abstract void onTablaEditada(int fila, int col);

    // --- MÉTODOS COMUNES ---
    protected void toggleModoBulto() {
        this.modoBulto = !this.modoBulto;
        if (modoBulto) {
            btnModoBulto.setBackground(new Color(46, 204, 113));
            btnModoBulto.setText("F7 - BULTO (ON)");
        } else {
            btnModoBulto.setBackground(Color.LIGHT_GRAY);
            btnModoBulto.setText("F7 - BULTO");
        }
        enfocarBuscador();
    }
    
    protected void enfocarBuscador() {
        txtBusqueda.requestFocusInWindow();
        txtBusqueda.selectAll();
    }

    // Helpers de diseño
    private JButton crearBotonLateral(String txt, int y, Color color, ActionListener al) {
        JButton btn = new JButton(txt);
        btn.setBounds(20, y, 130, 50);
        estilizarBoton(btn, color, Color.WHITE);
        if (color == Color.LIGHT_GRAY) btn.setForeground(Color.BLACK);
        btn.addActionListener(al);
        add(btn);
        return btn;
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void configurarEstiloTabla() {
        tableDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableDetalle.setRowHeight(30);
        tableDetalle.setShowGrid(true);
        tableDetalle.setGridColor(new Color(230, 230, 230));
        
        JTableHeader header = tableDetalle.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_HEADER);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableDetalle.setDefaultRenderer(Object.class, centerRenderer);
    }

    private void configurarAtajos() {
        setupAtajo(KeyEvent.VK_F1, "F1", e -> enfocarBuscador());
        setupAtajo(KeyEvent.VK_F5, "F5", e -> abrirBusquedaAvanzada());
        setupAtajo(KeyEvent.VK_F6, "F6", e -> abrirConsultaPrecio());
        setupAtajo(KeyEvent.VK_F7, "F7", e -> toggleModoBulto());
        setupAtajo(KeyEvent.VK_F8, "F8", e -> eliminarItemSeleccionado());
        setupAtajo(KeyEvent.VK_F12, "F12", e -> confirmarOperacion());
    }
    
    private void setupAtajo(int keyEvent, String nombre, ActionListener accion) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, nombre);
        getActionMap().put(nombre, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { accion.actionPerformed(e); }
        });
    }
}