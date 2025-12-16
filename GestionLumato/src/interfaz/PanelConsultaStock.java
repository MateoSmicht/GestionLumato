package interfaz;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelo.Categoria;
import modelo.Empresa;
import modelo.Producto;

public class PanelConsultaStock extends JPanel {

    private static final long serialVersionUID = 1L;
    private Empresa empresa;
    private Set<String> codigosOcultos = new HashSet<>(); 

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    
    // --- NUEVOS FILTROS EN CASCADA ---
    private JTextField txtBuscador;
    private JComboBox<Object> cmbCatMadre; // Combo Padre
    private JComboBox<Object> cmbSubCat;   // Combo Hijo
    private JTextField txtStockMaximo;
    private JLabel lblContador;
    
    private int limiteAlertaRojo = 10;

    public PanelConsultaStock(Empresa empresa) {
        this.empresa = empresa;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        // =================================================
        // 1. PANEL SUPERIOR (FILTROS)
        // =================================================
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15)); // Menos espacio horizontal (10)
        panelFiltros.setBackground(new Color(44, 62, 80));
        panelFiltros.setPreferredSize(new Dimension(800, 70));
        
        // --- A. Buscador Texto ---
        agregarLabel(panelFiltros, "Buscar:");
        txtBuscador = new JTextField(12); // Un poco más chico para hacer lugar
        txtBuscador.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscador.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { aplicarFiltros(); }
        });
        panelFiltros.add(txtBuscador);

        // --- B. CATEGORÍAS EN CASCADA ---
        
        // 1. COMBO MADRE
        agregarLabel(panelFiltros, "Rubro:");
        cmbCatMadre = new JComboBox<>();
        cmbCatMadre.setPreferredSize(new Dimension(130, 25));
        
        // 2. COMBO HIJA (Subcategoría)
        agregarLabel(panelFiltros, "Sub:");
        cmbSubCat = new JComboBox<>();
        cmbSubCat.setPreferredSize(new Dimension(130, 25));
        cmbSubCat.setEnabled(false); // Nace deshabilitado

        // Cargar las madres al iniciar
        cargarCategoriasMadre();

        // EVENTO: Al elegir Madre -> Cargar Hijas
        cmbCatMadre.addActionListener(e -> {
            Object seleccionado = cmbCatMadre.getSelectedItem();
            
            // Limpiamos subcategorías
            cmbSubCat.removeAllItems();
            cmbSubCat.addItem("TODAS"); // Opción por defecto
            
            if (seleccionado instanceof Categoria) {
                Categoria madre = (Categoria) seleccionado;
                
                // Llenamos con las hijas de esa madre
                List<Categoria> hijas = empresa.getSubcategorias(madre.getId());
                for (Categoria hija : hijas) {
                    cmbSubCat.addItem(hija);
                }
                
                cmbSubCat.setEnabled(true);
            } else {
                // Si eligió "TODAS" en el padre, deshabilitamos el hijo
                cmbSubCat.setEnabled(false);
            }
            
            aplicarFiltros(); // Disparamos la búsqueda
        });

        // EVENTO: Al elegir Hija -> Solo filtrar
        cmbSubCat.addActionListener(e -> aplicarFiltros());

        panelFiltros.add(cmbCatMadre);
        panelFiltros.add(cmbSubCat);

        // --- C. Stock ---
        agregarLabel(panelFiltros, "Stock menor a:"); // Texto corto
        txtStockMaximo = new JTextField(4);
        txtStockMaximo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtStockMaximo.setHorizontalAlignment(JTextField.CENTER);
        txtStockMaximo.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { aplicarFiltros(); }
        });
        panelFiltros.add(txtStockMaximo);
        
        // --- D. Reset y Contador ---
        JButton btnReset = new JButton("↻");
        btnReset.setPreferredSize(new Dimension(50, 40)); // Más compacto
        btnReset.setBackground(new Color(127, 140, 141));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> resetearTodo());
        panelFiltros.add(btnReset);

        lblContador = new JLabel("0");
        lblContador.setForeground(Color.LIGHT_GRAY);
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelFiltros.add(lblContador);

        add(panelFiltros, BorderLayout.NORTH);

        // =================================================
        // 2. TABLA CENTRAL (Igual que antes)
        // =================================================
        String[] columnas = {"Cód.", "Descripción", "Categoría", "Stock", "Costo", "Precio"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(30);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setBackground(new Color(52, 152, 219));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Renderizado Rojo por stock bajo
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    int stockActual = Integer.parseInt(table.getValueAt(row, 3).toString());
                    if (stockActual <= limiteAlertaRojo) { 
                        c.setForeground(new Color(231, 76, 60));
                        c.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                } catch (Exception e) { c.setForeground(Color.BLACK); }
                
                if (isSelected) c.setBackground(new Color(189, 195, 199));
                else c.setBackground(Color.WHITE);
                return c;
            }
        });

        // Selección clic derecho
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tabla.rowAtPoint(e.getPoint());
                    if (row >= 0) tabla.setRowSelectionInterval(row, row);
                }
            }
        });

        // Menú Ocultar
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem itemOcultar = new JMenuItem("Ocultar de esta lista");
        itemOcultar.addActionListener(e -> ocultarFilaSeleccionada());
        popupMenu.add(itemOcultar);
        tabla.setComponentPopupMenu(popupMenu);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Carga inicial
        aplicarFiltros();
    }

    // =================================================
    // LÓGICA INTERNA
    // =================================================

    private void cargarCategoriasMadre() {
        cmbCatMadre.removeAllItems();
        cmbCatMadre.addItem("TODAS");
        // Usamos el método de Empresa que solo trae madres
        for (Categoria c : empresa.getCategoriasMadre()) {
            cmbCatMadre.addItem(c);
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscador.getText();
        
        // --- LOGICA DE SELECCIÓN JERÁRQUICA ---
        Categoria categoriaParaFiltrar = null;

        Object seleccionMadre = cmbCatMadre.getSelectedItem();
        Object seleccionHija = cmbSubCat.getSelectedItem();

        // 1. Si hay una HIJA específica seleccionada, esa tiene prioridad
        if (seleccionHija instanceof Categoria) {
            categoriaParaFiltrar = (Categoria) seleccionHija;
        } 
        // 2. Si no hay hija, pero hay MADRE seleccionada, usamos la madre
        else if (seleccionMadre instanceof Categoria) {
            categoriaParaFiltrar = (Categoria) seleccionMadre;
        }
        // 3. Si ambas dicen "TODAS", categoriaParaFiltrar queda null (muestra todo)

        
        Integer stockMax = null;
        try {
            String txt = txtStockMaximo.getText().trim();
            if (!txt.isEmpty()) {
                stockMax = Integer.parseInt(txt);
                limiteAlertaRojo = stockMax;
            } else {
                limiteAlertaRojo = 10;
            }
        } catch (Exception e) {}

        // Llamamos al buscador inteligente
        List<Producto> resultados = empresa.buscarConFiltros(texto, categoriaParaFiltrar, stockMax);

        modeloTabla.setRowCount(0);
        for (Producto p : resultados) {
            if (codigosOcultos.contains(p.getCodigoBarra())) continue;
            
            modeloTabla.addRow(new Object[]{
                p.getCodigoBarra(),
                p.getDescripcion(),
                (p.getCategoria() != null ? p.getCategoria().getNombre() : "-"),
                p.getCantidadStock(),
                "$" + p.getPrecioCosto(),
                "$" + p.calcularPrecioFinal()
            });
        }
        lblContador.setText(modeloTabla.getRowCount() + "");
        tabla.repaint();
    }
    
    private void ocultarFilaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            codigosOcultos.add(tabla.getValueAt(fila, 0).toString());
            aplicarFiltros();
        }
    }
    
    private void resetearTodo() {
        txtBuscador.setText("");
        if (cmbCatMadre.getItemCount() > 0) cmbCatMadre.setSelectedIndex(0);
        // cmbSubCat se limpiará solo por el evento del padre
        txtStockMaximo.setText("");
        limiteAlertaRojo = 10;
        codigosOcultos.clear();
        aplicarFiltros();
    }
    
    private void agregarLabel(JPanel p, String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l);
    }
}