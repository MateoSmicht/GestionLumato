package interfaz;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import modelo.Empresa;
import modelo.Producto;
import controlador.ControladorStock;

public class DialogoModificarPrecio extends JDialog {

    private static final long serialVersionUID = 1L;
    private Producto producto;
    private ControladorStock controladorMath; // Usamos este controlador solo para los cálculos

    // Componentes
    private JTextField txtCosto;
    private JTextField txtGanancia;
    private JTextField txtPrecioFinal;
    private JComboBox<String> cmbIVA;
    private JCheckBox chkPrecioManual;
    
    // Colores (Mismo tema)
    private final Color COLOR_HEADER = new Color(44, 62, 80);
    private final Color COLOR_VERDE = new Color(39, 174, 96);

    public DialogoModificarPrecio(JFrame parent, Empresa empresa, Producto producto) {
        super(parent, "Modificar Precio - " + producto.getDescripcion(), true); // true = Modal (bloquea la de atrás)
        this.producto = producto;
        this.controladorMath = new ControladorStock(empresa); // Reutilizamos lógica matemática

        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // --- HEADER ---
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBounds(0, 0, 500, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("ACTUALIZAR PRECIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 300, 30);
        panelHeader.add(lblTitulo);

        // --- PRODUCTO INFO ---
        JLabel lblInfo = new JLabel("Producto: " + producto.getDescripcion());
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBounds(20, 80, 450, 20);
        add(lblInfo);

        // --- CAMPOS ---
        crearLabel("Costo ($):", 20, 120);
        txtCosto = crearInput(20, 145, 120);
        
        crearLabel("IVA (%):", 160, 120);
        cmbIVA = new JComboBox<>(new String[]{"21.0", "10.5", "0.0"});
        cmbIVA.setBounds(160, 145, 80, 35);
        add(cmbIVA);

        // Checkbox Manual
        chkPrecioManual = new JCheckBox("Fijar Precio Final Manualmente");
        chkPrecioManual.setBackground(new Color(245, 246, 250));
        chkPrecioManual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkPrecioManual.setBounds(20, 200, 250, 30);
        add(chkPrecioManual);

        crearLabel("Ganancia (%):", 20, 240);
        txtGanancia = crearInput(20, 265, 100);
        
        JLabel lblArrow = new JLabel("➜");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblArrow.setBounds(140, 265, 30, 35);
        add(lblArrow);

        crearLabel("PRECIO FINAL ($):", 180, 240);
        txtPrecioFinal = crearInput(180, 265, 150);
        txtPrecioFinal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtPrecioFinal.setForeground(new Color(0, 100, 0));
        txtPrecioFinal.setEditable(false);
        txtPrecioFinal.setBackground(new Color(230, 230, 230));

        // --- BOTONES ---
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(20, 340, 100, 40);
        estilizarBoton(btnCancelar, Color.GRAY, Color.WHITE);
        btnCancelar.addActionListener(e -> dispose()); // Cerrar
        add(btnCancelar);

        JButton btnGuardar = new JButton("GUARDAR CAMBIOS (F12)");
        btnGuardar.setBounds(140, 340, 320, 40);
        estilizarBoton(btnGuardar, COLOR_VERDE, Color.WHITE);
        btnGuardar.addActionListener(e -> guardarCambios());
        add(btnGuardar);

        // --- CARGAR DATOS ACTUALES ---
        txtCosto.setText(producto.getPrecioCosto().toString());
        // Convertimos 0.30 a 30 para visual
        txtGanancia.setText(producto.getPorcentajeGanancia().multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString());
        // El IVA lo buscamos en el combo (simple)
        String ivaActual = producto.getAlicuotaIVA().multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString();
        // Ajuste rapido para formateo (si es 21.00 lo deja en 21.0)
        if(ivaActual.equals("21")) ivaActual = "21.0";
        cmbIVA.setSelectedItem(ivaActual);
        
        // Calcular inicial
        calcularPrecioFinalAutomatico();

        // --- LÓGICA DE EVENTOS (Copia de PanelStock) ---
        configurarLogicaMatematica();
    }

    private void configurarLogicaMatematica() {
        chkPrecioManual.addActionListener(e -> {
            boolean manual = chkPrecioManual.isSelected();
            txtPrecioFinal.setEditable(manual);
            txtGanancia.setEditable(!manual);
            
            if (manual) {
                txtPrecioFinal.setBackground(Color.WHITE);
                txtGanancia.setBackground(new Color(230, 230, 230));
            } else {
                txtPrecioFinal.setBackground(new Color(230, 230, 230));
                txtGanancia.setBackground(Color.WHITE);
                calcularPrecioFinalAutomatico();
            }
        });

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
    }

    private void actualizarCalculos() {
        String costo = txtCosto.getText();
        String iva = cmbIVA.getSelectedItem().toString();

        if (chkPrecioManual.isSelected()) {
            String precioFinal = txtPrecioFinal.getText();
            BigDecimal ganancia = controladorMath.calcularGanancia(costo, precioFinal, iva);
            txtGanancia.setText(ganancia.toPlainString());
        } else {
            String ganancia = txtGanancia.getText();
            BigDecimal finalCalc = controladorMath.calcularPrecioFinal(costo, ganancia, iva);
            txtPrecioFinal.setText(finalCalc.toString());
        }
    }
    
    private void calcularPrecioFinalAutomatico() {
        // Forzamos actualización visual
        actualizarCalculos(); 
    }

    private void guardarCambios() {
        try {
            BigDecimal nuevoCosto = new BigDecimal(txtCosto.getText());
            BigDecimal nuevaGanancia = new BigDecimal(txtGanancia.getText()).divide(new BigDecimal(100));
            BigDecimal nuevoIVA = new BigDecimal(cmbIVA.getSelectedItem().toString()).divide(new BigDecimal(100));

            // Actualizamos el objeto Producto
            producto.setPrecioCosto(nuevoCosto);
            producto.setPorcentajeGanancia(nuevaGanancia);
            producto.setAlicuotaIVA(nuevoIVA);
            
            JOptionPane.showMessageDialog(this, "Precio actualizado correctamente.");
            dispose(); // Cerramos la ventana

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en los datos: " + e.getMessage());
        }
    }

    // --- Helpers Visuales ---
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
}
