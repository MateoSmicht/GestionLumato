package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import modelo.Categoria;
import modelo.Empresa;
import controlador.ControladorStock;

public class PanelFormularioProducto extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private ControladorStock controlador;
    private Empresa empresa;

    private Runnable onGuardarExitoso;
    private Runnable onCancelar;

    // Componentes Generales
    private JComboBox<Object> cmbCatMadre; 
    private JComboBox<Object> cmbSubCat;   
    private JTextField txtCodigo;
    private JTextField txtDescripcion;
    
    // COMPONENTE REUTILIZABLE DE PRECIOS (¡AQUÍ ESTÁ LA MAGIA!)
    private PanelPrecios panelPrecios;

    // Componentes Logística
    private JTextField txtStockInicial;
    private JComboBox<String> cmbUnidad;
    private JTextField txtFactor;

    public PanelFormularioProducto(Empresa empresa, String codigoPredefinido, Runnable onGuardar, Runnable onCancelar) {
        this.empresa = empresa;
        this.controlador = new ControladorStock(empresa);
        this.onGuardarExitoso = onGuardar;
        this.onCancelar = onCancelar;

        setLayout(null);
        setBackground(new Color(245, 246, 250));
        setSize(784, 600); // Un poco más alto para que entre todo cómodo

        // --- HEADER ---
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(new Color(44, 62, 80));
        panelHeader.setBounds(0, 0, 784, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("NUEVO PRODUCTO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 300, 30);
        panelHeader.add(lblTitulo);
        
        JButton btnCerrar = new JButton("Cancelar (Esc)");
        btnCerrar.setBounds(600, 15, 150, 30);
        estilizarBoton(btnCerrar, new Color(192, 57, 43), Color.WHITE);
        btnCerrar.addActionListener(e -> { if(onCancelar != null) onCancelar.run(); });
        panelHeader.add(btnCerrar);

        // ============================================================
        // FILA 1: DATOS BÁSICOS
        // ============================================================
        int fila1 = 80;
        crearLabel("1. Código (Enter):", 20, fila1);
        txtCodigo = crearInput(20, fila1 + 25, 200);
        txtCodigo.setText(codigoPredefinido);

        crearLabel("2. Descripción (Enter):", 240, fila1);
        txtDescripcion = crearInput(240, fila1 + 25, 500);

        // ============================================================
        // FILA 2: CATEGORÍAS
        // ============================================================
        int fila2 = 150;
        crearLabel("3. Rubro (Enter abre):", 20, fila2);
        cmbCatMadre = new JComboBox<>();
        cmbCatMadre.setBounds(20, fila2 + 25, 220, 35);
        add(cmbCatMadre);

        crearLabel("4. Subcategoría (Enter):", 260, fila2);
        cmbSubCat = new JComboBox<>();
        cmbSubCat.setBounds(260, fila2 + 25, 220, 35);
        cmbSubCat.setEnabled(false);
        add(cmbSubCat);
        
        cargarCategoriasMadre();

        // ============================================================
        // FILA 3: PANEL DE PRECIOS (REUTILIZADO)
        // ============================================================
        int fila3 = 210;
        
        // Instanciamos el panel pasándole el controlador
        panelPrecios = new PanelPrecios(controlador);
        // Lo ubicamos en el formulario (coordenadas relativas a este panel)
        panelPrecios.setBounds(20, fila3, 500, 150); 
        add(panelPrecios);

        // ============================================================
        // FILA 4: LOGÍSTICA (Bajamos un poco la coordenada Y)
        // ============================================================
        int fila4 = 370;
        crearLabel("7. Unidad:", 20, fila4);
        cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "KG"});
        cmbUnidad.setBounds(20, fila4 + 25, 100, 35);
        add(cmbUnidad);
        
        crearLabel("8. Factor:", 140, fila4);
        txtFactor = crearInput(140, fila4 + 25, 100);
        txtFactor.setText("1");

        crearLabel("9. Stock Inicial:", 260, fila4);
        txtStockInicial = crearInput(260, fila4 + 25, 100);
        txtStockInicial.setText("0");

        // BOTÓN GUARDAR
        JButton btnGuardar = new JButton("GUARDAR PRODUCTO (F12)");
        btnGuardar.setBounds(20, 450, 300, 50);
        estilizarBoton(btnGuardar, new Color(39, 174, 96), Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());
        add(btnGuardar);

        // ============================================================
        // LÓGICA DE NAVEGACIÓN (CONECTANDO LOS CABLES)
        // ============================================================
        configurarNavegacionTeclado();
        configurarLogicaCategorias();
        
        // Foco inicial
        SwingUtilities.invokeLater(() -> {
            if(txtCodigo.getText().isEmpty()) txtCodigo.requestFocus();
            else txtDescripcion.requestFocus();
        });
    }

    private void configurarNavegacionTeclado() {
        // 1. CODIGO -> Enter -> DESCRIPCION
        txtCodigo.addActionListener(e -> txtDescripcion.requestFocus());

        // 2. DESCRIPCION -> Enter -> RUBRO
        txtDescripcion.addActionListener(e -> {
            cmbCatMadre.requestFocus();
            try { cmbCatMadre.showPopup(); } catch(Exception ex) {}
        });

        // 3. RUBRO -> Enter -> SUBCAT
        cmbCatMadre.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !cmbCatMadre.isPopupVisible()) {
                    cmbSubCat.requestFocus();
                    try { cmbSubCat.showPopup(); } catch(Exception ex) {}
                }
            }
        });

        // 4. SUBCAT -> Enter -> AL PANEL DE PRECIOS (COSTO)
        cmbSubCat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !cmbSubCat.isPopupVisible()) {
                    // ¡AQUÍ SALTAMOS ADENTRO DEL PANEL HIJO!
                    panelPrecios.darFocoInicial();
                }
            }
        });

        // 5. SALIDA DEL PANEL DE PRECIOS -> UNIDAD
        // Configuramos el "Callback" del panel hijo
        panelPrecios.setOnEnterAlFinal(e -> {
            cmbUnidad.requestFocus();
        });

        // 6. UNIDAD -> FACTOR
        cmbUnidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtFactor.requestFocus();
                    txtFactor.selectAll();
                }
            }
        });
        
        // 7. FACTOR -> STOCK
        txtFactor.addActionListener(e -> { txtStockInicial.requestFocus(); txtStockInicial.selectAll(); });

        // 8. STOCK -> GUARDAR
        txtStockInicial.addActionListener(e -> guardar());
        
        // ATAJOS F12 y ESC
        KeyStroke f12Key = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f12Key, "GUARDAR");
        getActionMap().put("GUARDAR", new AbstractAction() { public void actionPerformed(ActionEvent e) { guardar(); } });
        
        KeyStroke escKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKey, "CANCELAR");
        getActionMap().put("CANCELAR", new AbstractAction() { public void actionPerformed(ActionEvent e) { if(onCancelar != null) onCancelar.run(); } });
    }

    private void guardar() {
        try {
            // Validaciones básicas
            Categoria catFinal = null;
            Object itemSub = cmbSubCat.getSelectedItem();
            if (itemSub instanceof Categoria) catFinal = (Categoria) itemSub;
            else catFinal = (Categoria) cmbCatMadre.getSelectedItem(); 

            if (catFinal == null) throw new Exception("Seleccione un Rubro.");
            
            // OBTENEMOS DATOS DEL PANEL DE PRECIOS
            String costo = panelPrecios.getCosto();
            String ganancia = panelPrecios.getGanancia();
            // Nota: PrecioFinal no hace falta pasarlo al controlador porque se calcula solo con Costo+Ganancia+IVA,
            // pero si tu método guardarProducto lo pide, lo sacas así:
            // String precio = panelPrecios.getPrecioFinal(); 

            if(costo.trim().isEmpty()) {
                panelPrecios.darFocoInicial();
                throw new Exception("El Costo es obligatorio.");
            }

            controlador.guardarProducto(
                txtCodigo.getText(), 
                txtDescripcion.getText(), 
                catFinal,
                costo,          // Viene del Panel
                ganancia,       // Viene del Panel
                panelPrecios.getIVA(), // Viene del Panel
                cmbUnidad.getSelectedItem().toString(), 
                txtFactor.getText(), 
                txtStockInicial.getText()
            );

            JOptionPane.showMessageDialog(this, "¡Guardado con éxito!");
            if (onGuardarExitoso != null) onGuardarExitoso.run();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void configurarLogicaCategorias() {
        cmbCatMadre.addActionListener(e -> {
            Object sel = cmbCatMadre.getSelectedItem();
            cmbSubCat.removeAllItems();
            if (sel instanceof Categoria) {
                cmbSubCat.setEnabled(true);
                List<Categoria> hijas = empresa.getSubcategorias(((Categoria)sel).getId());
                for (Categoria h : hijas) cmbSubCat.addItem(h);
            } else {
                cmbSubCat.setEnabled(false);
            }
        });
    }

    private void cargarCategoriasMadre() {
        cmbCatMadre.removeAllItems();
        for (Categoria c : controlador.obtenerCategoriasMadre()) cmbCatMadre.addItem(c);
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
    public int getStockIngresado() {
        try { return Integer.parseInt(txtStockInicial.getText()); } catch(Exception e) { return 0; }
    }
}