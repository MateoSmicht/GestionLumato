package interfaz.dialogos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import modelo.Categoria;
import modelo.Producto;
import controlador.ControladorCategoria;
import controlador.ControladorStock;
import interfaz.PanelPrecios;

public class DialogoEditarProducto extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private ControladorStock controlador;
    private ControladorCategoria controladorCategoria;
    private Producto productoOriginal; 
    private Runnable onGuardarExitoso;

    // Componentes
    private JTextField txtCodigo;
    private JTextField txtDescripcion;
    private JComboBox<Object> cmbCatMadre; 
    private JComboBox<Object> cmbSubCat;
    
    private JComboBox<Producto> cmbTodasEliminar;
    private JPanel panelEditar; // Panel contenedor para la pestaña 1
    
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

        // ============================================================
        // CREACIÓN DE PESTAÑAS (TABS)
        // ============================================================
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setBounds(0, 0, 768, 560);
        
        // Inicializamos el panel que contendrá todo tu código original
        panelEditar = new JPanel(null);
        panelEditar.setBackground(new Color(245, 246, 250));

        // --- HEADER ---
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(new Color(230, 126, 34)); // Naranja
        panelHeader.setBounds(0, 0, 784, 60);
        panelEditar.add(panelHeader); // Lo agregamos al panelEditar en vez de al JDialog directo

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
        panelEditar.add(cmbCatMadre);

        crearLabel("4. Subcategoría:", 260, fila2);
        cmbSubCat = new JComboBox<>();
        cmbSubCat.setBounds(260, fila2 + 25, 220, 35);
        panelEditar.add(cmbSubCat);

        cargarCategorias(); 

        // ============================================================
        // FILA 3: PANEL DE PRECIOS
        // ============================================================
        int fila3 = 210;
        panelPrecios = new PanelPrecios(controlador);
        panelPrecios.setBounds(20, fila3, 500, 150);
        panelEditar.add(panelPrecios);

        // ============================================================
        // FILA 4: LOGÍSTICA
        // ============================================================
        int fila4 = 370;
        crearLabel("7. Unidad:", 20, fila4);
        cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "KG"});
        cmbUnidad.setBounds(20, fila4 + 25, 100, 35);
        panelEditar.add(cmbUnidad);
        
        crearLabel("8. Factor:", 140, fila4);
        txtFactor = crearInput(140, fila4 + 25, 100);

        crearLabel("9. Stock Actual:", 260, fila4);
        txtStock = crearInput(260, fila4 + 25, 100);

        // BOTÓN GUARDAR
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS (F12)");
        btnGuardar.setBounds(20, 450, 300, 50);
        estilizarBoton(btnGuardar, new Color(39, 174, 96), Color.WHITE);
        btnGuardar.addActionListener(e -> guardarCambios());
        panelEditar.add(btnGuardar);

        // ============================================================
        // AGREGAR PESTAÑAS AL DIALOGO
        // ============================================================
        pestañas.addTab("Editar Datos", panelEditar);
        pestañas.addTab("Eliminar Producto", crearPanelEliminar());
        
        add(pestañas); // Agregamos el contenedor de pestañas a la ventana

        // ============================================================
        // CARGA DE DATOS Y LÓGICA
        // ============================================================
        cargarDatosDelProducto();
        cargarListas(); // Carga los productos en el combo de eliminar
        configurarNavegacion();
        configurarLogicaCategorias();
    }

    private void cargarDatosDelProducto() {
        txtCodigo.setText(productoOriginal.getCodigoBarra());
        txtDescripcion.setText(productoOriginal.getDescripcion());
        
        panelPrecios.setValores(
            productoOriginal.getPrecioCosto(),
            productoOriginal.getPorcentajeGanancia(),
            productoOriginal.getAlicuotaIVA()
        );

        cmbUnidad.setSelectedItem(productoOriginal.getNombreUnidad());
        txtFactor.setText(String.valueOf(productoOriginal.getFactor()));
        txtStock.setText(String.valueOf(productoOriginal.getCantidadStock()));

        seleccionarCategoriaEnCombo(productoOriginal.getCategoria());
    }

    private void seleccionarCategoriaEnCombo(Categoria catProducto) {
        if (catProducto == null) return;
        
        if (catProducto.getIdPadre() != null) {
            for (int i=0; i<cmbCatMadre.getItemCount(); i++) {
                Categoria item = (Categoria) cmbCatMadre.getItemAt(i);
                if (item.getId() == catProducto.getIdPadre()) {
                    cmbCatMadre.setSelectedIndex(i);
                    cargarSubCategorias(item);
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
        txtCodigo.addActionListener(e -> txtDescripcion.requestFocus());
        txtDescripcion.addActionListener(e -> {
            cmbCatMadre.requestFocus();
            try { cmbCatMadre.showPopup(); } catch(Exception ex) {}
        });

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
                    panelPrecios.darFocoInicial(); 
                }
            }
        });

        panelPrecios.setOnEnterAlFinal(e -> cmbUnidad.requestFocus());

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

        getRootPane().registerKeyboardAction(e -> guardarCambios(), 
            KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispose(), 
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void guardarCambios() {
        try {
            Categoria catFinal = null;
            Object itemSub = cmbSubCat.getSelectedItem();
            if (itemSub instanceof Categoria) catFinal = (Categoria) itemSub;
            else catFinal = (Categoria) cmbCatMadre.getSelectedItem();
            if (catFinal == null) throw new Exception("El rubro es obligatorio.");

            String costo = panelPrecios.getCosto();
            if (costo.isEmpty()) { panelPrecios.darFocoInicial(); throw new Exception("El costo es obligatorio."); }

            controlador.modificarProductoCompleto(
                productoOriginal.getCodigoBarra(), 
                txtCodigo.getText(),          
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
        JLabel l = new JLabel(t); l.setBounds(x,y,200,20); panelEditar.add(l);
    }
    
    private JTextField crearInput(int x, int y, int w) {
        JTextField t = new JTextField(); t.setBounds(x,y,w,35); 
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)), 
            BorderFactory.createEmptyBorder(5,5,5,5)));
        panelEditar.add(t); return t;
    }
    
    private void estilizarBoton(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    // ==========================================
    // PESTAÑA 3: ELIMINAR (AGREGADO Y CORREGIDO)
    // ==========================================
    private JPanel crearPanelEliminar() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 246, 250));

        JLabel lblTitulo = new JLabel("ELIMINAR PRODUCTO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(192, 57, 43));
        lblTitulo.setBounds(30, 20, 300, 25);
        panel.add(lblTitulo);

        // Texto corregido para Productos
        JLabel lblInfo = new JLabel("<html><body style='width: 380px'>Seleccione el producto que desea eliminar del sistema.<br>Si el producto tiene movimientos de stock registrados, la eliminación podría afectar el historial.</body></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBounds(30, 50, 420, 50);
        panel.add(lblInfo);

        JLabel lblSel = new JLabel("Seleccione producto a borrar:");
        lblSel.setBounds(30, 110, 250, 20);
        panel.add(lblSel);

        cmbTodasEliminar = new JComboBox<>();
        cmbTodasEliminar.setBounds(30, 135, 420, 35);
        panel.add(cmbTodasEliminar);

        JButton btnEliminar = new JButton("ELIMINAR DEFINITIVAMENTE");
        btnEliminar.setBounds(100, 200, 300, 40);
        estilizarBoton(btnEliminar, new Color(192, 57, 43), Color.WHITE); 
        
        btnEliminar.addActionListener(e -> {
            Producto prod = (Producto) cmbTodasEliminar.getSelectedItem();
            if (prod == null) return;

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Estás seguro de eliminar el producto '" + prod.getDescripcion() + "'?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    this.controlador.eliminarProducto(prod.getCodigoBarra()); 
                    JOptionPane.showMessageDialog(this, "Producto eliminado con éxito.");
                    
                    if (onGuardarExitoso != null) onGuardarExitoso.run(); // Refresca la tabla principal
                    dispose(); // Cierra el diálogo tras eliminar
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo eliminar", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(btnEliminar);

        return panel;
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================
    private void cargarListas() {
        cmbTodasEliminar.removeAllItems();
        for (Producto p : controlador.obtenerTodosLosProductos()) {
            cmbTodasEliminar.addItem(p);
        }
        // Para que por defecto aparezca seleccionado el producto que estamos editando
        if(productoOriginal != null) {
            cmbTodasEliminar.setSelectedItem(productoOriginal);
        }
    }
}