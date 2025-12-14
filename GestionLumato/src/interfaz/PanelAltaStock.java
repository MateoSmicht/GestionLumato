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
    private Empresa empresa; // Necesario para listar categorías

    // --- COMPONENTES VISUALES ---
    private JTextField txtCodigo;
    private JTextField txtDescripcion;
    private JComboBox<Categoria> cmbCategoria; // <--- NUEVO
    
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
        setBounds(0, 0, 784, 600); // Un poco más alto para que entre todo

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

        // --- FILA 2: Categoría (NUEVO) ---
        int yFila2 = 145;
        crearLabel("Categoría:", 20, yFila2);
        
        cmbCategoria = new JComboBox<>();
        cmbCategoria.setBounds(20, yFila2 + 25, 250, 35);
        cmbCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCategoria.setBackground(Color.WHITE);
        cargarCategorias(); // Llenamos el combo
        add(cmbCategoria);

        // --- FILA 3: Costos y Precios ---
        int yFila3 = 210;
        crearLabel("Costo ($):", 20, yFila3);
        txtCosto = crearInput(20, yFila3 + 25, 120);
        
        crearLabel("IVA (%):", 160, yFila3);
        cmbIVA = new JComboBox<>(new String[]{"21.0", "10.5", "0.0"});
        cmbIVA.setBounds(160, yFila3 + 25, 80, 35);
        add(cmbIVA);

        // Checkbox manual (arriba de ganancia)
        chkPrecioManual = new JCheckBox("Fijar Precio Final Manualmente");
        chkPrecioManual.setBackground(COLOR_FONDO);
        chkPrecioManual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkPrecioManual.setBounds(260, yFila3, 250, 20); // Ajusté altura
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

        // Checkbox: Activa/Desactiva campos
        chkPrecioManual.addActionListener(e -> {
            boolean manual = chkPrecioManual.isSelected();
            
            txtPrecioFinal.setEditable(manual); 
            txtGanancia.setEditable(!manual);   
            
            if (manual) {
                txtPrecioFinal.setBackground(Color.WHITE);
                txtGanancia.setBackground(new Color(230, 230, 230)); 
                txtGanancia.setText(""); 
            } else {
                txtPrecioFinal.setBackground(new Color(230, 230, 230)); 
                txtGanancia.setBackground(Color.WHITE);
                actualizarCalculos(); 
            }
        });

        // Calculadora en tiempo real
        KeyAdapter calculador = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarCalculos();
            }
        };

        txtCosto.addKeyListener(calculador);
        txtGanancia.addKeyListener(calculador);
        txtPrecioFinal.addKeyListener(calculador);
        
        cmbIVA.addActionListener(e -> actualizarCalculos());

        // ACCIÓN DE GUARDAR (Aquí estaba tu error antes)
        btnGuardar.addActionListener(e -> guardar());
    }

    // --- MÉTODOS AUXILIARES ---

    private void cargarCategorias() {
        cmbCategoria.removeAllItems();
        List<Categoria> lista = empresa.getCategorias();
        for (Categoria c : lista) {
            cmbCategoria.addItem(c);
        }
        if (lista.isEmpty()) {
            // Opcional: Agregar una categoría dummy o avisar
        }
    }

    private void guardar() {
        try {
            // 1. Obtener Categoría
            Categoria catSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
            if (catSeleccionada == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una categoría (Cree una en Gestión si no hay).");
                return;
            }

            // 2. Llamar al controlador con TODOS los parámetros
            controlador.guardarProducto(
                txtCodigo.getText(),
                txtDescripcion.getText(),
                catSeleccionada,  
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
        
        // Resetear categoría al primero si hay
        if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
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