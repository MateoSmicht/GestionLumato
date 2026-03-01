package interfaz.dialogos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import modelo.Categoria;
import modelo.Empresa;
import modelo.Producto;
import controlador.ControladorCategoria;
import controlador.ControladorStock;
import interfaz.PanelPrecios;

public class DialogoEditarProducto extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private ControladorStock controlador;
    private ControladorCategoria controladorCategoria;
    private Producto productoOriginal; // Para recordar el código viejo
    private Runnable onGuardarExitoso;

    // Componentes
    private JTextField txtCodigo;
    private JTextField txtDescripcion;
    private JComboBox<Object> cmbCatMadre; 
    private JComboBox<Object> cmbSubCat;
    
    // Nuestro Panel Reutilizable
    private PanelPrecios panelPrecios;

    // Logística
    private JComboBox<String> cmbUnidad;
    private JTextField txtFactor;
    private JTextField txtStock;

    public DialogoEditarProducto(JFrame parent,ControladorStock cs, ControladorCategoria controladorCategoria, Producto producto, Runnable onGuardar) {
        super(parent, "Editar Producto: " + producto.getDescripcion(), true);
        this.controlador = cs;
        this.controladorCategoria= controladorCategoria;
        this.productoOriginal = producto;
        this.onGuardarExitoso = onGuardar;

        setSize(784, 600);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // --- HEADER ---
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(new Color(230, 126, 34)); // Naranja para "Edición"
        panelHeader.setBounds(0, 0, 784, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("EDITAR PRODUCTO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 300, 30);
        panelHeader.add(lblTitulo);

        JButton btnCancelar = new JButton("Cancelar (Esc)");
        btnCancelar.setBounds(600, 15, 150, 30);
        estilizarBoton(btnCancelar, new Color(192, 57, 43), Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());
        panelHeader.add(btnCancelar);

        // ============================================================
        // FILA 1: DATOS BÁSICOS
        // ============================================================
        int fila1 = 80;
        crearLabel("1. Código:", 20, fila1);
        txtCodigo = crearInput(20, fila1 + 25, 200);
        
        crearLabel("2. Descripción:", 240, fila1);
        txtDescripcion = crearInput(240, fila1 + 25, 500);

        // ============================================================
        // FILA 2: CATEGORÍAS
        // ============================================================
        int fila2 = 150;
        crearLabel("3. Rubro:", 20, fila2);
        cmbCatMadre = new JComboBox<>();
        cmbCatMadre.setBounds(20, fila2 + 25, 220, 35);
        add(cmbCatMadre);

        crearLabel("4. Subcategoría:", 260, fila2);
        cmbSubCat = new JComboBox<>();
        cmbSubCat.setBounds(260, fila2 + 25, 220, 35);
        add(cmbSubCat);

        cargarCategorias(); // Llenamos los combos

        // ============================================================
        // FILA 3: PANEL DE PRECIOS (REUTILIZADO)
        // ============================================================
        int fila3 = 210;
        panelPrecios = new PanelPrecios(controlador);
        panelPrecios.setBounds(20, fila3, 500, 150);
        add(panelPrecios);

        // ============================================================
        // FILA 4: LOGÍSTICA
        // ============================================================
        int fila4 = 370;
        crearLabel("7. Unidad:", 20, fila4);
        cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "KG"});
        cmbUnidad.setBounds(20, fila4 + 25, 100, 35);
        add(cmbUnidad);
        
        crearLabel("8. Factor:", 140, fila4);
        txtFactor = crearInput(140, fila4 + 25, 100);

        crearLabel("9. Stock Actual:", 260, fila4);
        txtStock = crearInput(260, fila4 + 25, 100);

        // BOTÓN GUARDAR
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS (F12)");
        btnGuardar.setBounds(20, 450, 300, 50);
        estilizarBoton(btnGuardar, new Color(39, 174, 96), Color.WHITE);
        btnGuardar.addActionListener(e -> guardarCambios());
        add(btnGuardar);

        // ============================================================
        // CARGA DE DATOS EXISTENTES
        // ============================================================
        cargarDatosDelProducto();

        // ============================================================
        // LÓGICA DE NAVEGACIÓN (IDÉNTICA AL ALTA)
        // ============================================================
        configurarNavegacion();
        configurarLogicaCategorias();
    }

    private void cargarDatosDelProducto() {
        txtCodigo.setText(productoOriginal.getCodigoBarra());
        txtDescripcion.setText(productoOriginal.getDescripcion());
        
        // Cargar Precios en el Panel Hijo
        panelPrecios.setValores(
            productoOriginal.getPrecioCosto(),
            productoOriginal.getPorcentajeGanancia(),
            productoOriginal.getAlicuotaIVA()
        );

        // Cargar Logística
        cmbUnidad.setSelectedItem(productoOriginal.getNombreUnidad());
        txtFactor.setText(String.valueOf(productoOriginal.getFactor()));
        txtStock.setText(String.valueOf(productoOriginal.getCantidadStock()));

        // SELECCIONAR CATEGORÍA CORRECTA (Es un poco truculento con objetos)
        seleccionarCategoriaEnCombo(productoOriginal.getCategoria());
    }

    // Método auxiliar para encontrar la categoría en los combos
    private void seleccionarCategoriaEnCombo(Categoria catProducto) {
        if (catProducto == null) return;
        
        // Si tiene padre, seleccionamos madre e hija
        if (catProducto.getIdPadre() != null) {
            // Buscamos la madre por ID
            for (int i=0; i<cmbCatMadre.getItemCount(); i++) {
                Categoria item = (Categoria) cmbCatMadre.getItemAt(i);
                if (item.getId() == catProducto.getIdPadre()) {
                    cmbCatMadre.setSelectedIndex(i);
                    // Forzamos la carga de hijas
                    cargarSubCategorias(item);
                    // Buscamos la hija
                    for (int j=0; j<cmbSubCat.getItemCount(); j++) {
                        Categoria sub = (Categoria) cmbSubCat.getItemAt(j);
                        if (sub.getId() == catProducto.getId()) {
                            cmbSubCat.setSelectedIndex(j);
                            return;
                        }
                    }
                }
            }
        } else {
            // Es una categoría madre
            for (int i=0; i<cmbCatMadre.getItemCount(); i++) {
                Categoria item = (Categoria) cmbCatMadre.getItemAt(i);
                if (item.getId() == catProducto.getId()) {
                    cmbCatMadre.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void configurarNavegacion() {
        // Mismo flujo que en Alta
        txtCodigo.addActionListener(e -> txtDescripcion.requestFocus());
        txtDescripcion.addActionListener(e -> {
            cmbCatMadre.requestFocus();
            try { cmbCatMadre.showPopup(); } catch(Exception ex) {}
        });

        // Combos con Enter
        cmbCatMadre.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !cmbCatMadre.isPopupVisible()) {
                    cmbSubCat.requestFocus();
                    try { cmbSubCat.showPopup(); } catch(Exception ex) {}
                }
            }
        });

        cmbSubCat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !cmbSubCat.isPopupVisible()) {
                    panelPrecios.darFocoInicial(); // SALTA AL PANEL
                }
            }
        });

        // Callback del PanelPrecios
        panelPrecios.setOnEnterAlFinal(e -> cmbUnidad.requestFocus());

        // Logística
        cmbUnidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtFactor.requestFocus(); txtFactor.selectAll();
                }
            }
        });
        txtFactor.addActionListener(e -> { txtStock.requestFocus(); txtStock.selectAll(); });
        txtStock.addActionListener(e -> guardarCambios());

        // Atajos
        getRootPane().registerKeyboardAction(e -> guardarCambios(), 
            KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispose(), 
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void guardarCambios() {
        try {
            // Validación Rubro
            Categoria catFinal = null;
            Object itemSub = cmbSubCat.getSelectedItem();
            if (itemSub instanceof Categoria) catFinal = (Categoria) itemSub;
            else catFinal = (Categoria) cmbCatMadre.getSelectedItem();
            if (catFinal == null) throw new Exception("El rubro es obligatorio.");

            // Validación Panel Precios
            String costo = panelPrecios.getCosto();
            if (costo.isEmpty()) { panelPrecios.darFocoInicial(); throw new Exception("El costo es obligatorio."); }

            // LLAMADA AL NUEVO MÉTODO DEL CONTROLADOR
            controlador.modificarProductoCompleto(
                productoOriginal.getCodigoBarra(), // Código VIEJO (para saber cuál borrar si cambió)
                txtCodigo.getText(),          // Código NUEVO
                txtDescripcion.getText(),
                catFinal,
                costo,
                panelPrecios.getGanancia(),
                panelPrecios.getIVA(),
                cmbUnidad.getSelectedItem().toString(),
                txtFactor.getText(),
                txtStock.getText()
            );

            JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
            if (onGuardarExitoso != null) onGuardarExitoso.run();
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCategorias() {
        cmbCatMadre.removeAllItems();
        for (Categoria c : controladorCategoria.obtenerCategoriasMadre()) cmbCatMadre.addItem(c);
    }
    
    private void cargarSubCategorias(Categoria madre) {
        cmbSubCat.removeAllItems();
        if (madre != null) {
            cmbSubCat.setEnabled(true);
            List<Categoria> hijas = controladorCategoria.obtenerCategoriasMadre();
            for (Categoria h : hijas) cmbSubCat.addItem(h);
        } else {
            cmbSubCat.setEnabled(false);
        }
    }

    private void configurarLogicaCategorias() {
        cmbCatMadre.addActionListener(e -> {
            Object sel = cmbCatMadre.getSelectedItem();
            if (sel instanceof Categoria) {
                cargarSubCategorias((Categoria) sel);
            } else {
                cmbSubCat.setEnabled(false);
            }
        });
    }

    // Helpers UI
    private void crearLabel(String t, int x, int y) {
        JLabel l = new JLabel(t); l.setBounds(x,y,200,20); add(l);
    }
    private JTextField crearInput(int x, int y, int w) {
        JTextField t = new JTextField(); t.setBounds(x,y,w,35); 
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)), 
            BorderFactory.createEmptyBorder(5,5,5,5)));
        add(t); return t;
    }
    private void estilizarBoton(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}