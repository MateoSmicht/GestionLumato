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
    
    // Componentes
    private JComboBox<Object> cmbCatMadre; 
    private JComboBox<Object> cmbSubCat;   
    private JTextField txtCodigo;
    private JTextField txtDescripcion;
    
    // COMPONENTE REUTILIZABLE DE PRECIOS
    private PanelPrecios panelPrecios;

    // Componentes Logística
    private JTextField txtStockInicial;
    private JComboBox<String> cmbUnidad;
    private JTextField txtFactor;

    private Runnable onGuardarExitoso;
    private Runnable onCancelar;
 

    public PanelFormularioProducto(ControladorStock cs, String codigoPredefinido, Runnable onGuardar, Runnable onCancelar) {
        this.controlador = cs;
        this.onGuardarExitoso = onGuardar;
        this.onCancelar = onCancelar;

        setLayout(null);
        setBackground(new Color(245, 246, 250));
        // AJUSTE HD: Tamaño completo
        setBounds(0, 0, 1000, 680); 

        // ============================================================
        // HEADER (Ancho 1000px)
        // ============================================================
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(new Color(44, 62, 80));
        panelHeader.setBounds(0, 0, 1000, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("NUEVO PRODUCTO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(40, 15, 300, 30);
        panelHeader.add(lblTitulo);
        
        JButton btnCerrar = new JButton("Cancelar (Esc)");
        btnCerrar.setBounds(820, 15, 140, 30); // Pegado a la derecha
        estilizarBoton(btnCerrar, new Color(192, 57, 43), Color.WHITE);
        btnCerrar.addActionListener(e -> { if(onCancelar != null) onCancelar.run(); });
        panelHeader.add(btnCerrar);

        // ============================================================
        // FILA 1: DATOS BÁSICOS (Más anchos)
        // ============================================================
        int fila1 = 90;
        int margenIzq = 50; // Margen más amplio para que respire
        
        crearLabel("1. Código (Enter):", margenIzq, fila1);
        txtCodigo = crearInput(margenIzq, fila1 + 25, 220); // Un poco más ancho
        txtCodigo.setText(codigoPredefinido);

        // La descripción ahora ocupa el resto del ancho disponible
        crearLabel("2. Descripción (Enter):", 300, fila1);
        txtDescripcion = crearInput(300, fila1 + 25, 630); // MUCHO más ancho

        // ============================================================
        // FILA 2: CATEGORÍAS
        // ============================================================
        int fila2 = 170;
        
        crearLabel("3. Rubro (Enter abre):", margenIzq, fila2);
        cmbCatMadre = new JComboBox<>();
        cmbCatMadre.setBounds(margenIzq, fila2 + 25, 300, 35); // Combo más ancho
        add(cmbCatMadre);

        crearLabel("4. Subcategoría (Enter):", 380, fila2);
        cmbSubCat = new JComboBox<>();
        cmbSubCat.setBounds(380, fila2 + 25, 300, 35); // Combo más ancho
        cmbSubCat.setEnabled(false);
        add(cmbSubCat);
        
        cargarCategoriasMadre();

        // ============================================================
        // FILA 3: PANEL DE PRECIOS (Ubicación)
        // ============================================================
        int fila3 = 240;
        
        // Instanciamos el panel
        panelPrecios = new PanelPrecios(controlador);
        // Lo movemos un poco para que quede alineado con el margen izquierdo nuevo
        panelPrecios.setBounds(margenIzq, fila3, 600, 150); 
        add(panelPrecios);

        // ============================================================
        // FILA 4: LOGÍSTICA
        // ============================================================
        int fila4 = 400; // Bajamos un poco más
        
        crearLabel("7. Unidad:", margenIzq, fila4);
        cmbUnidad = new JComboBox<>(new String[]{"UNI", "BULTO", "CAJA", "KG"});
        cmbUnidad.setBounds(margenIzq, fila4 + 25, 120, 35);
        add(cmbUnidad);
        
        crearLabel("8. Factor:", 200, fila4);
        txtFactor = crearInput(200, fila4 + 25, 100);
        txtFactor.setText("1");

        crearLabel("9. Stock Inicial:", 330, fila4);
        txtStockInicial = crearInput(330, fila4 + 25, 120);
        txtStockInicial.setText("0");

        // BOTÓN GUARDAR (Más grande y llamativo)
        JButton btnGuardar = new JButton("GUARDAR PRODUCTO (F12)");
        btnGuardar.setBounds(margenIzq, 480, 350, 60); // Botón grande
        estilizarBoton(btnGuardar, new Color(39, 174, 96), Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.addActionListener(e -> guardar());
        add(btnGuardar);

        // ============================================================
        // LÓGICA DE NAVEGACIÓN
        // ============================================================
        configurarNavegacionTeclado();
        configurarLogicaCategorias();
        
        SwingUtilities.invokeLater(() -> {
            if(txtCodigo.getText().isEmpty()) txtCodigo.requestFocus();
            else txtDescripcion.requestFocus();
        });
    }

    // --- (EL RESTO DE LOS MÉTODOS SIGUE IGUAL, SOLO CAMBIÓ EL DISEÑO ARRIBA) ---

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

        // 4. SUBCAT -> Enter -> PANEL PRECIOS
        cmbSubCat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !cmbSubCat.isPopupVisible()) {
                    panelPrecios.darFocoInicial();
                }
            }
        });

        // 5. SALIDA DEL PANEL PRECIOS -> UNIDAD
        panelPrecios.setOnEnterAlFinal(e -> cmbUnidad.requestFocus());

        // 6. UNIDAD -> FACTOR
        cmbUnidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtFactor.requestFocus(); txtFactor.selectAll();
                }
            }
        });
        
        txtFactor.addActionListener(e -> { txtStockInicial.requestFocus(); txtStockInicial.selectAll(); });
        txtStockInicial.addActionListener(e -> guardar());
        
        // Atajos F12 y ESC
        KeyStroke f12Key = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f12Key, "GUARDAR");
        getActionMap().put("GUARDAR", new AbstractAction() { public void actionPerformed(ActionEvent e) { guardar(); } });
        
        KeyStroke escKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKey, "CANCELAR");
        getActionMap().put("CANCELAR", new AbstractAction() { public void actionPerformed(ActionEvent e) { if(onCancelar != null) onCancelar.run(); } });
    }

    private void guardar() {
        try {
            Categoria catFinal = null;
            Object itemSub = cmbSubCat.getSelectedItem();
            if (itemSub instanceof Categoria) catFinal = (Categoria) itemSub;
            else catFinal = (Categoria) cmbCatMadre.getSelectedItem(); 

            if (catFinal == null) throw new Exception("Seleccione un Rubro.");
            
            String costo = panelPrecios.getCosto();
            String ganancia = panelPrecios.getGanancia();

            if(costo.trim().isEmpty()) {
                panelPrecios.darFocoInicial();
                throw new Exception("El Costo es obligatorio.");
            }

            controlador.guardarProducto(
                txtCodigo.getText(), 
                txtDescripcion.getText(), 
                catFinal,
                costo,
                ganancia,
                panelPrecios.getIVA(),
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
                
                int idMadre = ((Categoria)sel).getId();
                List<Categoria> hijas = controlador.obtenerSubCategorias(idMadre);
                
                for (Categoria h : hijas) {
                    cmbSubCat.addItem(h);
                }
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
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(t); return t;
    }
    private void estilizarBoton(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    public int getStockIngresado() {
        try { return Integer.parseInt(txtStockInicial.getText()); } catch(Exception e) { return 0; }
    }
}