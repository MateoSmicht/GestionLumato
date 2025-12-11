package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import modelo.Empresa;
import controlador.ControladorStock;

public class DialogoAltaProducto extends JDialog {

    private static final long serialVersionUID = 1L;
    private ControladorStock controlador;
    private boolean guardadoExitoso = false; 

    // Componentes
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

    private final Color COLOR_HEADER = new Color(44, 62, 80);
    private final Color COLOR_VERDE = new Color(39, 174, 96);

    public DialogoAltaProducto(JFrame parent, Empresa empresa, String codigoPredefinido) {
        super(parent, "Nuevo Producto", true); 
        this.controlador = new ControladorStock(empresa);
        
        setSize(784, 500);
        setLocationRelativeTo(parent); 
        setLayout(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // --- HEADER ---
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBounds(0, 0, 784, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("NUEVO PRODUCTO RÁPIDO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 300, 30);
        panelHeader.add(lblTitulo);
        
        JButton btnCerrar = new JButton("Cancelar (Esc)");
        btnCerrar.setBounds(630, 15, 130, 30);
        estilizarBoton(btnCerrar, new Color(192, 57, 43), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        panelHeader.add(btnCerrar);

        // --- FORMULARIO ---
        
        crearLabel("Código de Barras:", 20, 80);
        txtCodigo = crearInput(20, 105, 200);
        txtCodigo.setText(codigoPredefinido); 

        crearLabel("Descripción:", 240, 80);
        txtDescripcion = crearInput(240, 105, 500);

        crearLabel("Costo ($):", 20, 160);
        txtCosto = crearInput(20, 185, 120);
        
        crearLabel("IVA (%):", 160, 160);
        cmbIVA = new JComboBox<>(new String[]{"21.0", "10.5", "0.0"});
        cmbIVA.setBounds(160, 185, 80, 35);
        add(cmbIVA);

        chkPrecioManual = new JCheckBox("Fijar Precio Final Manualmente");
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
        txtPrecioFinal.setEditable(false); 
        txtPrecioFinal.setBackground(new Color(230, 230, 230));

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

        JButton btnGuardar = new JButton("GUARDAR Y AGREGAR (F12)");
        btnGuardar.setBounds(20, 350, 300, 50);
        estilizarBoton(btnGuardar, COLOR_VERDE, Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());
        add(btnGuardar);

        // --- LÓGICA DE EVENTOS ---
        configurarLogicaMatematica();
        
        // --- AQUÍ ESTÁ EL ARREGLO PARA LA TECLA ESC ---
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "CERRAR_VENTANA");
        getRootPane().getActionMap().put("CERRAR_VENTANA", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Configurar F12 para guardar también desde aquí
        KeyStroke f12Key = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f12Key, "GUARDAR");
        getRootPane().getActionMap().put("GUARDAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardar();
            }
        });

        // Foco inicial
        txtDescripcion.requestFocus();
    }

    private void guardar() {
        try {
            // --- CAMBIO AQUÍ: Pasamos "0" como stock inicial a la base de datos ---
            // El stock real lo devolveremos al panel padre para que lo cargue en la lista
            controlador.guardarProducto(
                txtCodigo.getText(), txtDescripcion.getText(), txtCosto.getText(),
                txtGanancia.getText(), cmbIVA.getSelectedItem().toString(),
                cmbUnidad.getSelectedItem().toString(), txtFactor.getText(),
                "0" // <--- FORZAMOS 0 EN LA BD
            );
            
            this.guardadoExitoso = true;
            dispose(); 
        } catch (Exception ex) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }
    public int getStockIngresado() {
        try {
            String texto = txtStockInicial.getText().trim();
            if (texto.isEmpty()) return 0;
            return Integer.parseInt(texto);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public boolean isGuardadoExitoso() { return guardadoExitoso; }

    private void configurarLogicaMatematica() {
        chkPrecioManual.addActionListener(e -> {
            boolean manual = chkPrecioManual.isSelected();
            txtPrecioFinal.setEditable(manual);
            txtGanancia.setEditable(!manual);
            if(manual) { 
                txtPrecioFinal.setBackground(Color.WHITE); 
                txtGanancia.setBackground(new Color(230, 230, 230)); 
                txtGanancia.setText("");
            }
            else { 
                txtPrecioFinal.setBackground(new Color(230, 230, 230)); 
                txtGanancia.setBackground(Color.WHITE); 
                actualizarCalculos(); 
            }
        });
        
        KeyAdapter k = new KeyAdapter() { public void keyReleased(KeyEvent e) { actualizarCalculos(); } };
        txtCosto.addKeyListener(k); txtGanancia.addKeyListener(k); txtPrecioFinal.addKeyListener(k);
        cmbIVA.addActionListener(e -> actualizarCalculos());
    }

    private void actualizarCalculos() {
        String costo = txtCosto.getText();
        String iva = cmbIVA.getSelectedItem().toString();
        if (chkPrecioManual.isSelected()) {
            txtGanancia.setText(controlador.calcularGanancia(costo, txtPrecioFinal.getText(), iva).toPlainString());
        } else {
            txtPrecioFinal.setText(controlador.calcularPrecioFinal(costo, txtGanancia.getText(), iva).toString());
        }
    }

    // Helpers
    private void crearLabel(String t, int x, int y) {
        JLabel l = new JLabel(t); l.setBounds(x,y,150,20); add(l);
    }
    private JTextField crearInput(int x, int y, int w) {
        JTextField t = new JTextField(); t.setBounds(x,y,w,35); 
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(t); return t;
    }
    private void estilizarBoton(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}