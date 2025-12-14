package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List; 

import modelo.Categoria;
import modelo.Empresa;
import controlador.ControladorStock;

public class DialogoAltaProducto extends JDialog {

    private static final long serialVersionUID = 1L;
    
    // Modelos y Controladores
    private ControladorStock controlador;
    private Empresa empresa; // Variable de clase
    
    // Estado
    private boolean guardadoExitoso = false; 

    // Componentes Visuales
    private JComboBox<Categoria> cmbCategoria;
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
       
            this.empresa = empresa;
            this.controlador = new ControladorStock(empresa);
            
            // Aumentamos un poquito el alto para que entre todo cómodo
            setSize(784, 550); 
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

            // ============================================================
            // FORMULARIO ORGANIZADO POR FILAS
            // ============================================================
            
            // --- FILA 1: IDENTIFICACIÓN (Y = 80) ---
            int fila1 = 80;
            
            crearLabel("Código de Barras:", 20, fila1);
            txtCodigo = crearInput(20, fila1 + 25, 200);
            txtCodigo.setText(codigoPredefinido); 

            crearLabel("Descripción:", 240, fila1);
            txtDescripcion = crearInput(240, fila1 + 25, 500);

            // --- FILA 2: CATEGORÍA (Y = 150) --- 
            // Le damos su propio espacio para que no choque con nada
            int fila2 = 150;
            
            JLabel lblCat = new JLabel("Categoría:");
            lblCat.setBounds(20, fila2, 200, 20);
            lblCat.setFont(new Font("Segoe UI", Font.BOLD, 12));
            add(lblCat);

            cmbCategoria = new JComboBox<>();
            cmbCategoria.setBounds(20, fila2 + 25, 250, 35); // Ancho cómodo
            cmbCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cmbCategoria.setBackground(Color.WHITE);
            cargarCategorias(); 
            add(cmbCategoria);

            // --- FILA 3: COSTOS Y PRECIOS (Y = 220) ---
            // Bajamos todo este bloque para que no toque la categoría
            int fila3 = 220;
            
            crearLabel("Costo ($):", 20, fila3);
            txtCosto = crearInput(20, fila3 + 25, 120);
            
            crearLabel("IVA (%):", 160, fila3);
            cmbIVA = new JComboBox<>(new String[]{"21.0", "10.5", "0.0"});
            cmbIVA.setBounds(160, fila3 + 25, 80, 35);
            add(cmbIVA);

            // El checkbox un poquito más arriba de los inputs para que quede alineado con labels
            chkPrecioManual = new JCheckBox("Fijar Precio Final Manualmente");
            chkPrecioManual.setBounds(260, fila3, 250, 20); 
            chkPrecioManual.setBackground(new Color(245, 246, 250));
            add(chkPrecioManual);

            crearLabel("Ganancia (%):", 260, fila3 + 25); // Bajamos un poco el label para que entre el check
            txtGanancia = crearInput(260, fila3 + 50, 100); // Input más abajo
            
            JLabel lblArrow = new JLabel("➜");
            lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblArrow.setBounds(380, fila3 + 50, 30, 35);
            add(lblArrow);

            crearLabel("PRECIO FINAL ($):", 420, fila3 + 25);
            txtPrecioFinal = crearInput(420, fila3 + 50, 150);
            txtPrecioFinal.setEditable(false); 
            txtPrecioFinal.setBackground(new Color(230, 230, 230));
            txtPrecioFinal.setFont(new Font("Segoe UI", Font.BOLD, 14));
            txtPrecioFinal.setForeground(new Color(0, 100, 0));

            // --- FILA 4: LOGÍSTICA (Y = 320) ---
            int fila4 = 320;
            
            crearLabel("Unidad Medida:", 20, fila4);
            cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "PACK", "KG"});
            cmbUnidad.setBounds(20, fila4 + 25, 100, 35);
            add(cmbUnidad);
            
            crearLabel("Factor (u. x Bulto):", 140, fila4);
            txtFactor = crearInput(140, fila4 + 25, 100);
            txtFactor.setText("1"); 

            crearLabel("Stock Inicial:", 260, fila4);
            txtStockInicial = crearInput(260, fila4 + 25, 100);
            txtStockInicial.setText("0");

            // --- BOTÓN ---
            JButton btnGuardar = new JButton("GUARDAR Y AGREGAR (F12)");
            btnGuardar.setBounds(20, 410, 300, 50); // Abajo de todo
            estilizarBoton(btnGuardar, COLOR_VERDE, Color.WHITE);
            btnGuardar.addActionListener(e -> guardar());
            add(btnGuardar);

            // --- LÓGICA DE EVENTOS ---
            configurarLogicaMatematica();
            
            // Teclas Rápidas (ESC y F12)
            KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "CERRAR");
            getRootPane().getActionMap().put("CERRAR", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { dispose(); }
            });
            
            KeyStroke f12Key = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f12Key, "GUARDAR");
            getRootPane().getActionMap().put("GUARDAR", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { guardar(); }
            });

            SwingUtilities.invokeLater(() -> txtDescripcion.requestFocus());
        }

    private void guardar() {
        try {
            Categoria catSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
            
            if (catSeleccionada == null) {
                throw new Exception("Debe seleccionar una categoría.");
            }
            
            controlador.guardarProducto(
                txtCodigo.getText(), 
                txtDescripcion.getText(),
                catSeleccionada, 
                txtCosto.getText(),
                txtGanancia.getText(), 
                cmbIVA.getSelectedItem().toString(),
                cmbUnidad.getSelectedItem().toString(), 
                txtFactor.getText(),
                "0" // Stock 0 en BD, el real se retorna al panel de carga
            );
            
            this.guardadoExitoso = true;
            dispose(); 
        } catch (Exception ex) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void cargarCategorias() {
        if (empresa == null) return; // Protección extra
        
        cmbCategoria.removeAllItems();
        List<Categoria> lista = empresa.getCategorias();
        
        for (Categoria c : lista) {
            cmbCategoria.addItem(c);
        }
        
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "¡Atención! No hay categorías creadas. Vaya a Gestión de Stock para crear una.");
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

    // Helpers UI
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