package interfaz;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import modelo.Empresa;
import controlador.ControladorStock;

public class PanelAltaStock extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // --- CONTROLADOR ---
    private ControladorStock controlador;

    // --- COMPONENTES VISUALES ---
    private JTextField txtCodigo;
    private JTextField txtDescripcion;
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
        // Inicializamos el controlador
        this.controlador = new ControladorStock(empresa);
        
        setLayout(null);
        setBackground(COLOR_FONDO);
        setBounds(0, 0, 784, 500);

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
        
        crearLabel("Código de Barras:", 20, 80);
        txtCodigo = crearInput(20, 105, 200);

        crearLabel("Descripción:", 240, 80);
        txtDescripcion = crearInput(240, 105, 500);

        // --- COSTOS ---
        crearLabel("Costo ($):", 20, 160);
        txtCosto = crearInput(20, 185, 120);
        
        crearLabel("IVA (%):", 160, 160);
        cmbIVA = new JComboBox<>(new String[]{"21.0", "10.5", "0.0"});
        cmbIVA.setBounds(160, 185, 80, 35);
        add(cmbIVA);

        // --- PRECIOS ---
        chkPrecioManual = new JCheckBox("Fijar Precio Final Manualmente");
        chkPrecioManual.setBackground(COLOR_FONDO);
        chkPrecioManual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkPrecioManual.setBounds(260, 155, 250, 30);
        add(chkPrecioManual);

        crearLabel("Ganancia (%):", 260, 160);
        txtGanancia = crearInput(260, 185, 100);
        
        JLabel lblArrow = new JLabel("➜");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblArrow.setBounds(380, 185, 30, 35);
        add(lblArrow);

        crearLabel("PRECIO FINAL ($):", 420, 160);
        txtPrecioFinal = crearInput(420, 185, 150);
        txtPrecioFinal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtPrecioFinal.setForeground(new Color(0, 100, 0)); 
        txtPrecioFinal.setEditable(false); 
        txtPrecioFinal.setBackground(new Color(230, 230, 230)); // Gris al inicio

        // --- STOCK Y UNIDAD ---
        crearLabel("Unidad Medida:", 20, 240);
        cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "PACK", "KG"});
        cmbUnidad.setBounds(20, 265, 100, 35);
        add(cmbUnidad);
        
        crearLabel("Factor (u. x Bulto):", 140, 240);
        txtFactor = crearInput(140, 265, 100);
        txtFactor.setText("1"); 

        crearLabel("Stock Inicial:", 260, 240);
        txtStockInicial = crearInput(260, 265, 100);
        txtStockInicial.setText("0");

        // BOTÓN GUARDAR
        JButton btnGuardar = new JButton("GUARDAR PRODUCTO");
        btnGuardar.setBounds(20, 350, 250, 50);
        estilizarBoton(btnGuardar, COLOR_VERDE, Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(btnGuardar);

        // =========================================================
        // 3. EVENTOS
        // =========================================================

        // Checkbox: Activa/Desactiva campos pero NO toca el IVA
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

        // Listener genérico para calcular mientras se escribe
        KeyAdapter calculador = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarCalculos();
            }
        };

        txtCosto.addKeyListener(calculador);
        txtGanancia.addKeyListener(calculador);
        txtPrecioFinal.addKeyListener(calculador);
        
        // Al cambiar IVA recalculamos (en cualquier modo)
        cmbIVA.addActionListener(e -> actualizarCalculos());

        // Guardar
        btnGuardar.addActionListener(e -> {
            try {
                controlador.guardarProducto(
                    txtCodigo.getText(),
                    txtDescripcion.getText(),
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
        });
    }

    // --- MÉTODOS DELEGADOS ---
    private void actualizarCalculos() {
        String costo = txtCosto.getText();
        String iva = cmbIVA.getSelectedItem().toString();

        if (chkPrecioManual.isSelected()) {
            // MODO MANUAL: Final -> Ganancia (Respetando el IVA seleccionado)
            String precioFinal = txtPrecioFinal.getText();
            BigDecimal gananciaCalculada = controlador.calcularGanancia(costo, precioFinal, iva);
            
            txtGanancia.setText(gananciaCalculada.toPlainString());
            
        } else {
            // MODO AUTO: Ganancia -> Final
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
        txtDescripcion.requestFocus(); // Ponemos el foco en descripción para seguir rápido
    }
}
