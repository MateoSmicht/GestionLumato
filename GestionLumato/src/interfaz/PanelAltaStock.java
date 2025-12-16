package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;

import modelo.Categoria;
import modelo.Empresa;
import controlador.ControladorStock;

public class PanelAltaStock extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // --- CONTROLADOR Y MODELO ---
    private ControladorStock controlador;
    private Empresa empresa;

    // --- COMPONENTES VISUALES ---
    private JTextField txtCodigo;
    private JTextField txtDescripcion;
    
    // --- CAMBIO IMPORTANTE: JComboBox<Object> para mezclar Texto y Categorías ---
    private JComboBox<Object> cmbCatMadre; // Rubro General
    private JComboBox<Object> cmbSubCat;   // Subcategoría
    
    private JTextField txtCosto;
    private JTextField txtGanancia;
    private JTextField txtPrecioFinal;
    private JTextField txtStockInicial;
    private JComboBox<String> cmbIVA;
    
    private JComboBox<String> cmbUnidad;
    private JTextField txtFactor;
    private JCheckBox chkPrecioManual;
    
    public JButton btnVolver;

    // --- COLORES ---
    private final Color COLOR_FONDO = new Color(245, 246, 250);
    private final Color COLOR_HEADER = new Color(44, 62, 80);
    private final Color COLOR_VERDE = new Color(39, 174, 96);

    public PanelAltaStock(Empresa empresa) {
        this.empresa = empresa;
        this.controlador = new ControladorStock(empresa);
        
        setLayout(null);
        setBackground(COLOR_FONDO);
        setBounds(0, 0, 784, 600);

        // =========================================================
        // 1. HEADER
        // =========================================================
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(null);
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBounds(0, 0, 784, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("ALTA DE PRODUCTOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 300, 30);
        panelHeader.add(lblTitulo);
        
        btnVolver = new JButton("Volver al Menú");
        btnVolver.setBounds(630, 15, 130, 30);
        estilizarBoton(btnVolver, new Color(149, 165, 166), Color.WHITE);
        panelHeader.add(btnVolver);

        // =========================================================
        // 2. FORMULARIO
        // =========================================================
        
        // --- FILA 1: Identificación ---
        int yFila1 = 80;
        crearLabel("Código de Barras:", 20, yFila1);
        txtCodigo = crearInput(20, yFila1 + 25, 200);

        crearLabel("Descripción:", 240, yFila1);
        txtDescripcion = crearInput(240, yFila1 + 25, 500);

        // ============================================================
        // --- FILA 2: SELECCIÓN DE CATEGORÍA EN CASCADA ---
        // ============================================================
        int yFila2 = 145;
        
        // 1. COMBO MADRE (Rubro)
        crearLabel("Rubro / General:", 20, yFila2);
        cmbCatMadre = new JComboBox<>();
        cmbCatMadre.setBounds(20, yFila2 + 25, 220, 35);
        cmbCatMadre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCatMadre.setBackground(Color.WHITE);
        add(cmbCatMadre);

        // 2. COMBO HIJA (Subcategoría)
        crearLabel("Subcategoría (Opcional):", 260, yFila2);
        cmbSubCat = new JComboBox<>();
        cmbSubCat.setBounds(260, yFila2 + 25, 220, 35);
        cmbSubCat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbSubCat.setBackground(Color.WHITE);
        cmbSubCat.setEnabled(false); // Nace deshabilitado
        add(cmbSubCat);

        // LOGICA DE CARGA
        cargarCategoriasMadre();

        // EVENTO: Al cambiar el Padre, cargar las Hijas
        cmbCatMadre.addActionListener(e -> {
            Object itemSeleccionado = cmbCatMadre.getSelectedItem();
            
            cmbSubCat.removeAllItems(); // Limpiar
            
            if (itemSeleccionado instanceof Categoria) {
                Categoria madre = (Categoria) itemSeleccionado;
                List<Categoria> hijas = empresa.getSubcategorias(madre.getId());
                
                // Siempre habilitamos para mostrar la opción general
                cmbSubCat.setEnabled(true);
                
                // 1. Agregamos la opción General (String)
                cmbSubCat.addItem("--- GENERAL (Solo Rubro) ---");
                
                // 2. Agregamos las hijas (Objetos Categoria)
                for (Categoria h : hijas) {
                    cmbSubCat.addItem(h);
                }
            } else {
                cmbSubCat.setEnabled(false);
            }
        });

        // --- FILA 3: Costos y Precios ---
        int yFila3 = 210;
        crearLabel("Costo ($):", 20, yFila3);
        txtCosto = crearInput(20, yFila3 + 25, 120);
        
        crearLabel("IVA (%):", 160, yFila3);
        cmbIVA = new JComboBox<>(new String[]{"21.0", "10.5", "0.0"});
        cmbIVA.setBounds(160, yFila3 + 25, 80, 35);
        add(cmbIVA);

        chkPrecioManual = new JCheckBox("Fijar Precio Final Manualmente");
        chkPrecioManual.setBackground(COLOR_FONDO);
        chkPrecioManual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkPrecioManual.setBounds(260, yFila3, 250, 20);
        add(chkPrecioManual);

        crearLabel("Ganancia (%):", 260, yFila3);
        txtGanancia = crearInput(260, yFila3 + 25, 100);
        
        JLabel lblArrow = new JLabel("➜");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblArrow.setBounds(380, yFila3 + 25, 30, 35);
        add(lblArrow);

        crearLabel("PRECIO FINAL ($):", 420, yFila3);
        txtPrecioFinal = crearInput(420, yFila3 + 25, 150);
        txtPrecioFinal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtPrecioFinal.setForeground(new Color(0, 100, 0)); 
        txtPrecioFinal.setEditable(false); 
        txtPrecioFinal.setBackground(new Color(230, 230, 230));

        // --- FILA 4: Stock y Unidad ---
        int yFila4 = 290;
        crearLabel("Unidad Medida:", 20, yFila4);
        cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "PACK", "KG"});
        cmbUnidad.setBounds(20, yFila4 + 25, 100, 35);
        add(cmbUnidad);
        
        crearLabel("Factor (u. x Bulto):", 140, yFila4);
        txtFactor = crearInput(140, yFila4 + 25, 100);
        txtFactor.setText("1"); 

        crearLabel("Stock Inicial:", 260, yFila4);
        txtStockInicial = crearInput(260, yFila4 + 25, 100);
        txtStockInicial.setText("0");

        // --- BOTÓN GUARDAR ---
        JButton btnGuardar = new JButton("GUARDAR PRODUCTO (F12)");
        btnGuardar.setBounds(20, 380, 300, 50);
        estilizarBoton(btnGuardar, COLOR_VERDE, Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(btnGuardar);

        // =========================================================
        // 3. EVENTOS
        // =========================================================

        // Checkbox: Activa/Desactiva campos y maneja el FOCO
        chkPrecioManual.addActionListener(e -> {
            boolean manual = chkPrecioManual.isSelected();
            txtPrecioFinal.setEditable(manual); 
            txtGanancia.setEditable(!manual);   
            
            if (manual) {
                txtPrecioFinal.setBackground(Color.WHITE);
                txtGanancia.setBackground(new Color(230, 230, 230)); 
                txtGanancia.setText(""); 
                // ENFOCAR AUTOMÁTICAMENTE
                txtPrecioFinal.requestFocus();
                txtPrecioFinal.selectAll();
            } else {
                txtPrecioFinal.setBackground(new Color(230, 230, 230)); 
                txtGanancia.setBackground(Color.WHITE);
                actualizarCalculos(); 
                txtGanancia.requestFocus();
            }
        });

        KeyAdapter calculador = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { actualizarCalculos(); }
        };

        txtCosto.addKeyListener(calculador);
        txtGanancia.addKeyListener(calculador);
        txtPrecioFinal.addKeyListener(calculador);
        cmbIVA.addActionListener(e -> actualizarCalculos());

        btnGuardar.addActionListener(e -> guardar());
    }

    // --- MÉTODOS AUXILIARES ---

    private void cargarCategoriasMadre() {
        cmbCatMadre.removeAllItems();
        // Cargamos solo las categorías principales
        List<Categoria> lista = empresa.getCategoriasMadre();
        for (Categoria c : lista) {
            cmbCatMadre.addItem(c);
        }
        // Disparamos el evento para cargar subcategorías del primero (si hay)
        if (cmbCatMadre.getItemCount() > 0) {
            cmbCatMadre.setSelectedIndex(0);
        }
    }

    private void guardar() {
        try {
            // 1. DETERMINAR LA CATEGORÍA FINAL
            Categoria catFinal = null;
            
            // Verificamos qué eligió en el segundo combo
            Object itemSub = cmbSubCat.getSelectedItem();
            
            if (itemSub instanceof Categoria) {
                // Si eligió una hija específica, usamos esa
                catFinal = (Categoria) itemSub;
            } else {
                // Si eligió "--- GENERAL ---" (String) o es null, usamos la Madre
                catFinal = (Categoria) cmbCatMadre.getSelectedItem();
            }

            if (catFinal == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Rubro/Categoría.");
                return;
            }

            // 2. Llamar al controlador
            controlador.guardarProducto(
                txtCodigo.getText(),
                txtDescripcion.getText(),
                catFinal,  // <--- Categoría calculada
                txtCosto.getText(),
                txtGanancia.getText(),
                cmbIVA.getSelectedItem().toString(),
                cmbUnidad.getSelectedItem().toString(),
                txtFactor.getText(),
                txtStockInicial.getText()
            );
            
            JOptionPane.showMessageDialog(this, "Producto guardado correctamente!");
            limpiarFormulario();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCalculos() {
        String costo = txtCosto.getText();
        String iva = cmbIVA.getSelectedItem().toString();

        if (chkPrecioManual.isSelected()) {
            String precioFinal = txtPrecioFinal.getText();
            BigDecimal gananciaCalculada = controlador.calcularGanancia(costo, precioFinal, iva);
            txtGanancia.setText(gananciaCalculada.toPlainString());
        } else {
            String ganancia = txtGanancia.getText();
            BigDecimal finalCalculado = controlador.calcularPrecioFinal(costo, ganancia, iva);
            txtPrecioFinal.setText(finalCalculado.toString());
        }
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtDescripcion.setText("");
        txtCosto.setText("");
        txtGanancia.setText("");
        txtPrecioFinal.setText("");
        txtStockInicial.setText("0");
        txtCodigo.requestFocus();
        
        // Resetear controles
        chkPrecioManual.setSelected(false);
        txtPrecioFinal.setEditable(false);
        txtPrecioFinal.setBackground(new Color(230, 230, 230));
        txtGanancia.setEditable(true);
        txtGanancia.setBackground(Color.WHITE);
        cmbIVA.setSelectedItem("21.0");
        
        // Resetear combos
        cargarCategoriasMadre();
    }

    private void crearLabel(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setBounds(x, y, 150, 20);
        add(lbl);
    }

    private JTextField crearInput(int x, int y, int ancho) {
        JTextField txt = new JTextField();
        txt.setBounds(x, y, ancho, 35);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(txt);
        return txt;
    }
    
    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public void setCodigoPredefinido(String codigo) {
        txtCodigo.setText(codigo);
        txtDescripcion.requestFocus(); 
    }
}