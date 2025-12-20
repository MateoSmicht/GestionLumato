package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import controlador.ControladorStock;

public class PanelPrecios extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // Componentes
    private JTextField txtCosto;
    private JTextField txtGanancia;
    private JTextField txtPrecioFinal;
    private JComboBox<String> cmbIVA;
    private JCheckBox chkPrecioManual;
    
    private ControladorStock controlador;

    // Listener para avisar al padre cuando dan Enter al final
    private ActionListener onEnterEnPrecio;

    public PanelPrecios(ControladorStock controlador) {
        this.controlador = controlador;
        setLayout(null);
        setBackground(new Color(245, 246, 250));
        setSize(500, 180); // Tamaño fijo del bloque de precios

        // 1. COSTO
        crearLabel("Costo ($):", 0, 0);
        txtCosto = crearInput(0, 25, 120);

        // 2. IVA
        crearLabel("IVA (%):", 140, 0);
        cmbIVA = new JComboBox<>(new String[]{"0.0", "21.0", "10.5"});
        cmbIVA.setBounds(140, 25, 80, 35);
        add(cmbIVA);

        // 3. CHECKBOX
        chkPrecioManual = new JCheckBox("Fijar Precio Manualmente");
        chkPrecioManual.setBounds(250, 25, 220, 35);
        chkPrecioManual.setBackground(new Color(245, 246, 250));
        chkPrecioManual.setSelected(true); // Default Manual
        add(chkPrecioManual);

        // 4. GANANCIA
        crearLabel("Ganancia (%):", 0, 80);
        txtGanancia = crearInput(0, 105, 100);
        
        JLabel lblArrow = new JLabel("➜");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblArrow.setBounds(120, 105, 30, 35);
        add(lblArrow);

        // 5. PRECIO FINAL
        crearLabel("PRECIO FINAL ($):", 160, 80);
        txtPrecioFinal = crearInput(160, 105, 150);
        txtPrecioFinal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPrecioFinal.setForeground(new Color(0, 100, 0));
        
        // Configurar Lógica Interna
        configurarEventos();
        actualizarEstadoManual();
    }

    // --- LÓGICA DE NEGOCIO INTERNA ---
    
    private void configurarEventos() {
        // NAVEGACIÓN INTERNA (Enter pasa al siguiente)
        txtCosto.addActionListener(e -> { cmbIVA.requestFocus(); cmbIVA.showPopup(); });
        
        // Enter en IVA -> Pasa a Ganancia o Precio
        cmbIVA.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !cmbIVA.isPopupVisible()) {
                    if(chkPrecioManual.isSelected()) txtPrecioFinal.requestFocus();
                    else txtGanancia.requestFocus();
                }
            }
        });

        // LÓGICA INTELIGENTE DEL PRECIO (El famoso 50%)
        txtPrecioFinal.addActionListener(e -> {
            String texto = txtPrecioFinal.getText().trim();
            if (texto.isEmpty()) {
                // Auto 50%
                chkPrecioManual.setSelected(false);
                txtGanancia.setText("50");
                actualizarEstadoManual();
                actualizarCalculos();
                txtGanancia.requestFocus();
            } else {
                actualizarCalculos();
                // AVISAR AL PADRE (Para que salte a Guardar o Unidad)
                if(onEnterEnPrecio != null) onEnterEnPrecio.actionPerformed(e);
            }
        });

        // Evento Ganancia
        txtGanancia.addActionListener(e -> {
            actualizarCalculos();
            if(onEnterEnPrecio != null) onEnterEnPrecio.actionPerformed(e);
        });

        // Cálculos en tiempo real
        KeyAdapter calculador = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() != KeyEvent.VK_ENTER) actualizarCalculos();
            }
        };
        txtCosto.addKeyListener(calculador);
        txtGanancia.addKeyListener(calculador);
        txtPrecioFinal.addKeyListener(calculador);
        
        chkPrecioManual.addActionListener(e -> actualizarEstadoManual());
        cmbIVA.addActionListener(e -> actualizarCalculos());
    }

    public void actualizarCalculos() {
        String costo = txtCosto.getText();
        String iva = cmbIVA.getSelectedItem().toString();

        if (chkPrecioManual.isSelected()) {
            // Manual: Calculamos Ganancia
            String ganancia = controlador.calcularPorcentajeGanancia(costo, txtPrecioFinal.getText());
            txtGanancia.setText(ganancia);
        } else {
            // Auto: Calculamos Precio
            String precio = controlador.calcularPrecioVenta(costo, txtGanancia.getText(), iva);
            txtPrecioFinal.setText(precio);
        }
    }

    private void actualizarEstadoManual() {
        boolean manual = chkPrecioManual.isSelected();
        txtPrecioFinal.setEditable(manual);
        txtGanancia.setEditable(!manual);
        txtPrecioFinal.setBackground(manual ? Color.WHITE : new Color(230,230,230));
        txtGanancia.setBackground(!manual ? Color.WHITE : new Color(230,230,230));
        if(manual) actualizarCalculos();
    }

    // --- GETTERS Y SETTERS (Para sacar y meter datos) ---

    public void setValores(BigDecimal costo, BigDecimal ganancia, BigDecimal iva) {
txtCosto.setText(costo != null ? costo.toString() : "0");
        
        // Delegamos el formato al controlador
        txtGanancia.setText(controlador.formatearGananciaParaVista(ganancia));
        
        // Delegamos el formato del IVA
        String ivaStr = controlador.formatearIVAParaVista(iva);
        cmbIVA.setSelectedItem(ivaStr);
        
        actualizarCalculos();
    }

    public String getCosto() { return txtCosto.getText(); }
    public String getGanancia() { return txtGanancia.getText(); } // Devuelve %
    public String getPrecioFinal() { return txtPrecioFinal.getText(); }
    public String getIVA() { return cmbIVA.getSelectedItem().toString(); }

    public void setOnEnterAlFinal(ActionListener action) {
        this.onEnterEnPrecio = action;
    }
    
    public void darFocoInicial() {
        txtCosto.requestFocus();
    }

    // Helpers UI
    private void crearLabel(String t, int x, int y) {
        JLabel l = new JLabel(t); l.setBounds(x,y,150,20); add(l);
    }
    private JTextField crearInput(int x, int y, int w) {
        JTextField t = new JTextField(); t.setBounds(x,y,w,35); 
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)), 
            BorderFactory.createEmptyBorder(5,5,5,5)));
        add(t); return t;
    }
    
   
}