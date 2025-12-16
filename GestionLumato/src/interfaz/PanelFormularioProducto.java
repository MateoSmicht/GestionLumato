package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;

import modelo.Categoria;
import modelo.Empresa;
import controlador.ControladorStock;

public class PanelFormularioProducto extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // Controlador
    private ControladorStock controlador;
    private Empresa empresa;

    // Callbacks (Acciones para avisar al padre que terminó)
    private Runnable onGuardarExitoso;
    private Runnable onCancelar;

    // Componentes
    private JComboBox<Object> cmbCatMadre; 
    private JComboBox<Object> cmbSubCat;   
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

    // Constructor único
    public PanelFormularioProducto(Empresa empresa, String codigoPredefinido, Runnable onGuardar, Runnable onCancelar) {
        this.empresa = empresa;
        this.controlador = new ControladorStock(empresa);
        this.onGuardarExitoso = onGuardar;
        this.onCancelar = onCancelar;

        setLayout(null);
        setBackground(new Color(245, 246, 250));
        // Tamaño base del panel interno
        setPreferredSize(new Dimension(784, 550)); 
        setSize(784, 550);

        // --- HEADER ---
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(new Color(44, 62, 80));
        panelHeader.setBounds(0, 0, 784, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("DATOS DEL PRODUCTO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 300, 30);
        panelHeader.add(lblTitulo);
        
        JButton btnCerrar = new JButton("Cancelar / Volver");
        btnCerrar.setBounds(600, 15, 150, 30);
        estilizarBoton(btnCerrar, new Color(192, 57, 43), Color.WHITE);
        btnCerrar.addActionListener(e -> {
            if (this.onCancelar != null) this.onCancelar.run();
        });
        panelHeader.add(btnCerrar);

        // --- FORMULARIO (Copiado de tu versión final corregida) ---
        int fila1 = 80;
        crearLabel("Código de Barras:", 20, fila1);
        txtCodigo = crearInput(20, fila1 + 25, 200);
        txtCodigo.setText(codigoPredefinido);

        crearLabel("Descripción:", 240, fila1);
        txtDescripcion = crearInput(240, fila1 + 25, 500);

        // Fila 2: Categorías
        int fila2 = 150;
        crearLabel("Rubro / General:", 20, fila2);
        cmbCatMadre = new JComboBox<>();
        cmbCatMadre.setBounds(20, fila2 + 25, 220, 35);
        add(cmbCatMadre);

        crearLabel("Subcategoría:", 260, fila2);
        cmbSubCat = new JComboBox<>();
        cmbSubCat.setBounds(260, fila2 + 25, 220, 35);
        cmbSubCat.setEnabled(false);
        add(cmbSubCat);
        
        cargarCategoriasMadre();
        configurarEventosCategoria();

        // Fila 3: Precios
        int fila3 = 220;
        crearLabel("Costo ($):", 20, fila3);
        txtCosto = crearInput(20, fila3 + 25, 120);
        
        crearLabel("IVA (%):", 160, fila3);
        cmbIVA = new JComboBox<>(new String[]{"21.0", "10.5", "0.0"});
        cmbIVA.setBounds(160, fila3 + 25, 80, 35);
        add(cmbIVA);

        chkPrecioManual = new JCheckBox("Fijar Precio Final Manualmente");
        chkPrecioManual.setBounds(304, 190, 250, 20);
        chkPrecioManual.setBackground(new Color(245, 246, 250));
        add(chkPrecioManual);

        crearLabel("Ganancia (%):", 260, fila3);
        txtGanancia = crearInput(260, fila3 + 25, 100);
        
        JLabel lblArrow = new JLabel("➜");
        lblArrow.setBounds(380, fila3 + 25, 30, 35);
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lblArrow);

        crearLabel("PRECIO FINAL ($):", 420, fila3);
        txtPrecioFinal = crearInput(420, fila3 + 25, 150);
        txtPrecioFinal.setEditable(false);
        txtPrecioFinal.setBackground(new Color(230, 230, 230));
        txtPrecioFinal.setForeground(new Color(0, 100, 0));
        txtPrecioFinal.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Fila 4: Logística
        int fila4 = 320;
        crearLabel("Unidad:", 20, fila4);
        cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "PACK", "KG"});
        cmbUnidad.setBounds(20, fila4 + 25, 100, 35);
        add(cmbUnidad);
        
        crearLabel("Factor:", 140, fila4);
        txtFactor = crearInput(140, fila4 + 25, 100);
        txtFactor.setText("1");

        crearLabel("Stock Inicial:", 260, fila4);
        txtStockInicial = crearInput(260, fila4 + 25, 100);
        txtStockInicial.setText("0");

        // Botón Guardar
        JButton btnGuardar = new JButton("GUARDAR PRODUCTO (F12)");
        btnGuardar.setBounds(20, 400, 300, 50);
        estilizarBoton(btnGuardar, new Color(39, 174, 96), Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());
        add(btnGuardar);

        // Lógica
        configurarLogicaPrecios();
        
        SwingUtilities.invokeLater(() -> {
            if(txtCodigo.getText().isEmpty()) txtCodigo.requestFocus();
            else txtDescripcion.requestFocus();
        });
    }

    // --- MÉTODOS DE LÓGICA (Idénticos a lo que ya tenías) ---
    
    private void guardar() {
        try {
            Categoria catFinal = null;
            Object itemSub = cmbSubCat.getSelectedItem();
            if (itemSub instanceof Categoria) catFinal = (Categoria) itemSub;
            else catFinal = (Categoria) cmbCatMadre.getSelectedItem();

            if (catFinal == null) throw new Exception("Seleccione una Categoría.");

            controlador.guardarProducto(
                txtCodigo.getText(), txtDescripcion.getText(), catFinal,
                txtCosto.getText(), txtGanancia.getText(), cmbIVA.getSelectedItem().toString(),
                cmbUnidad.getSelectedItem().toString(), txtFactor.getText(), txtStockInicial.getText()
            );

            JOptionPane.showMessageDialog(this, "¡Guardado con éxito!");
            
            // Ejecutamos la acción que nos pasó el padre (Cerrar o Limpiar)
            if (onGuardarExitoso != null) onGuardarExitoso.run();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configurarLogicaPrecios() {
        chkPrecioManual.addActionListener(e -> {
            boolean manual = chkPrecioManual.isSelected();
            txtPrecioFinal.setEditable(manual);
            txtGanancia.setEditable(!manual);
            if(manual) {
                txtPrecioFinal.setBackground(Color.WHITE);
                txtGanancia.setBackground(new Color(230,230,230));
                txtGanancia.setText("");
                txtPrecioFinal.requestFocus();
                txtPrecioFinal.selectAll();
            } else {
                txtPrecioFinal.setBackground(new Color(230,230,230));
                txtGanancia.setBackground(Color.WHITE);
                actualizarCalculos();
            }
        });
        KeyAdapter k = new KeyAdapter() { public void keyReleased(KeyEvent e) { actualizarCalculos(); }};
        txtCosto.addKeyListener(k); txtGanancia.addKeyListener(k); txtPrecioFinal.addKeyListener(k);
    }
    
    private void actualizarCalculos() {
        String costo = txtCosto.getText();
        String iva = cmbIVA.getSelectedItem().toString();
        if (chkPrecioManual.isSelected()) 
            txtGanancia.setText(controlador.calcularGanancia(costo, txtPrecioFinal.getText(), iva).toPlainString());
        else 
            txtPrecioFinal.setText(controlador.calcularPrecioFinal(costo, txtGanancia.getText(), iva).toString());
    }

    private void cargarCategoriasMadre() {
        cmbCatMadre.removeAllItems();
        for (Categoria c : empresa.getCategoriasMadre()) cmbCatMadre.addItem(c);
        if (cmbCatMadre.getItemCount() > 0) cmbCatMadre.setSelectedIndex(0);
    }
    
    private void configurarEventosCategoria() {
        cmbCatMadre.addActionListener(e -> {
            Object sel = cmbCatMadre.getSelectedItem();
            cmbSubCat.removeAllItems();
            if (sel instanceof Categoria) {
                cmbSubCat.setEnabled(true);
                cmbSubCat.addItem("--- GENERAL ---");
                for (Categoria h : empresa.getSubcategorias(((Categoria)sel).getId())) cmbSubCat.addItem(h);
            } else cmbSubCat.setEnabled(false);
        });
    }

    // Helpers UI reducidos
    private void crearLabel(String t, int x, int y) {
        JLabel l = new JLabel(t); l.setBounds(x,y,150,20); add(l);
    }
    private JTextField crearInput(int x, int y, int w) {
        JTextField t = new JTextField(); t.setBounds(x,y,w,35); add(t); return t;
    }
    private void estilizarBoton(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
    
    // Método para obtener stock ingresado (útil para el Dialogo)
    public int getStockIngresado() {
        try { return Integer.parseInt(txtStockInicial.getText()); } catch(Exception e) { return 0; }
    }
}