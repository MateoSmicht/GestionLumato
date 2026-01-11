package interfaz;

import javax.swing.*;

import controlador.ControladorStock;

import java.awt.*;
import java.util.List;
import modelo.Producto;

public class DialogoUnificar extends JDialog {

    private static final long serialVersionUID = 1L;
    private ControladorStock controlador;
    
    // Componentes Fusión
    private JTextField txtPrincipal;
    private JTextField txtDuplicado;
    private JLabel lblInfoPrincipal;
    private JLabel lblInfoDuplicado;
    
    // Componentes Lista de Alias
    private JList<String> listaCodigos;
    private DefaultListModel<String> modeloLista;
    private JButton btnEliminarAlias;
    private Producto productoSeleccionado; // Para saber de quién es la lista

    public DialogoUnificar(JFrame parent, ControladorStock controlador) {
        super(parent, "Gestión de Códigos y Fusión", true);
        this.controlador = controlador;
        
        // Hacemos la ventana más ancha para que entre la lista a la derecha
        setSize(900, 450);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // =============================================================
        // SECCIÓN IZQUIERDA: FUSIÓN DE PRODUCTOS
        // =============================================================
        
        JLabel lblTitulo = new JLabel("FUSIÓN DE PRODUCTOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setBounds(20, 20, 250, 30);
        getContentPane().add(lblTitulo);

        // --- Botón Ayuda ---
        
        String ayudaFusion = "<html><body style='width: 250px; background-color: #FFFFE0; padding: 5px;'>"
                + "<b>FUSIÓN (Izquierda):</b><br><br>"
                + "Sirve para corregir cuando un mismo producto tiene dos códigos distintos cargados.<br><br>"
                + "<b>1. Stock:</b> Se suma el stock del Duplicado al Principal.<br>"
                + "<b>2. Códigos:</b> El código duplicado se guarda como 'Alias' y ambos comparten el mismo codigo interno.<br>"
                + "<b>3. Limpieza:</b> El producto duplicado se borra.<br><br>"
                + "<i>Resultado: Al escanear cualquiera de los dos, traerá al Principal.</i>"
                + "</body></html>";

        agregarBotonAyuda(280, 20, ayudaFusion);


        // --- 2. AYUDA DERECHA (ALIAS) ---
        // Ubicado cerca del título "CÓDIGOS ASOCIADOS" (ajusté X a 800 para que quede al final)
        String ayudaAlias = "<html><body style='width: 250px; background-color: #FFFFE0; padding: 5px;'>"
                + "<b>LISTA DE ALIAS (Derecha):</b><br><br>"
                + "Muestra todos los códigos de barra secundarios que también abren este producto.<br><br>"
                + "• Si ves un código incorrecto aquí, selecciónalo y dale a <b>'Borrar'</b> para liberarlo."
                + "</body></html>";

        agregarBotonAyuda(800, 20, ayudaAlias);
        
        // ... (resto del constructor: Títulos, TextFields, Listas, etc.) ...
    

        // --- 1. PRINCIPAL ---
        JLabel lblP = new JLabel("1. Producto PRINCIPAL (El que queda):");
        lblP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblP.setForeground(new Color(39, 174, 96)); // Verde
        lblP.setBounds(20, 70, 300, 20);
        getContentPane().add(lblP);

        txtPrincipal = new JTextField();
        txtPrincipal.setBounds(20, 100, 200, 30);
        txtPrincipal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPrincipal.addActionListener(e -> buscarInfoPrincipal());
        getContentPane().add(txtPrincipal);

        lblInfoPrincipal = new JLabel("-");
        lblInfoPrincipal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoPrincipal.setBounds(230, 100, 200, 30);
        getContentPane().add(lblInfoPrincipal);

        // --- 2. DUPLICADO ---
        JLabel lblD = new JLabel("2. Producto DUPLICADO (Se elimina):");
        lblD.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblD.setForeground(new Color(192, 57, 43)); // Rojo
        lblD.setBounds(20, 160, 300, 20);
        getContentPane().add(lblD);

        txtDuplicado = new JTextField();
        txtDuplicado.setBounds(20, 190, 200, 30);
        txtDuplicado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDuplicado.addActionListener(e -> buscarInfoDuplicado());
        getContentPane().add(txtDuplicado);

        lblInfoDuplicado = new JLabel("-");
        lblInfoDuplicado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoDuplicado.setBounds(230, 190, 200, 30);
        getContentPane().add(lblInfoDuplicado);

        JButton btnUnificar = new JButton("FUSIONAR (Unificar Stocks)");
        btnUnificar.setBounds(20, 303, 400, 50);
        estilizarBoton(btnUnificar, new Color(44, 62, 80), Color.WHITE);
        btnUnificar.addActionListener(e -> unificar());
        getContentPane().add(btnUnificar);

        // =============================================================
        // SECCIÓN DERECHA: LISTA DE CÓDIGOS ASOCIADOS (ALIAS)
        // =============================================================
        
        // Separador vertical visual
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setBounds(460, 20, 10, 380);
        getContentPane().add(sep);

        JLabel lblTituloLista = new JLabel("CÓDIGOS ASOCIADOS");
        lblTituloLista.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTituloLista.setBounds(500, 20, 300, 30);
        getContentPane().add(lblTituloLista);

        JLabel lblSubtituloLista = new JLabel("<html>Estos códigos también abren el<br>producto principal al escanearlos.</html>");
        lblSubtituloLista.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSubtituloLista.setForeground(Color.GRAY);
        lblSubtituloLista.setBounds(500, 50, 300, 40);
        getContentPane().add(lblSubtituloLista);

        // Modelo y Lista
        modeloLista = new DefaultListModel<>();
        listaCodigos = new JList<>(modeloLista);
        listaCodigos.setFont(new Font("Consolas", Font.PLAIN, 14));
        listaCodigos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollLista = new JScrollPane(listaCodigos);
        scrollLista.setBounds(500, 100, 350, 200);
        getContentPane().add(scrollLista);

        btnEliminarAlias = new JButton("Borrar Código Seleccionado");
        btnEliminarAlias.setBounds(500, 310, 350, 40);
        estilizarBoton(btnEliminarAlias, new Color(192, 57, 43), Color.WHITE); // Rojo
        btnEliminarAlias.setEnabled(false); // Nace deshabilitado
        btnEliminarAlias.addActionListener(e -> eliminarAliasSeleccionado());
        getContentPane().add(btnEliminarAlias);
        
        
    }

    // --- LÓGICA DE BÚSQUEDA ---

    private void buscarInfoPrincipal() {
        String cod = txtPrincipal.getText().trim();
        if (cod.isEmpty()) return;
        
        productoSeleccionado = controlador.buscarProducto(cod);
        
        if (productoSeleccionado != null) {
            lblInfoPrincipal.setText("<html>" + productoSeleccionado.getDescripcion() + "<br>Stock: <b>" + productoSeleccionado.getCantidadStock() + "</b></html>");
            lblInfoPrincipal.setForeground(Color.BLACK);
            
            // CARGAR LA LISTA DE LA DERECHA
            cargarListaAlias();
            
        } else {
            lblInfoPrincipal.setText("Producto no encontrado");
            lblInfoPrincipal.setForeground(Color.RED);
            modeloLista.clear();
            btnEliminarAlias.setEnabled(false);
            productoSeleccionado = null;
        }
    }
    
    private void cargarListaAlias() {
        modeloLista.clear();
        if (productoSeleccionado == null) return;
        
        List<String> alias = productoSeleccionado.getCodigosSecundarios();
        
        if (alias.isEmpty()) {
            modeloLista.addElement("(Sin códigos extra)");
            btnEliminarAlias.setEnabled(false);
        } else {
            for (String codigo : alias) {
                modeloLista.addElement(codigo);
            }
            btnEliminarAlias.setEnabled(true);
        }
    }

    private void buscarInfoDuplicado() {
        String cod = txtDuplicado.getText().trim();
        if (cod.isEmpty()) return;
        Producto p = controlador.buscarProducto(cod);
        if (p != null) {
            lblInfoDuplicado.setText("<html>" + p.getDescripcion() + "<br>Stock: <b>" + p.getCantidadStock() + "</b></html>");
            lblInfoDuplicado.setForeground(Color.BLACK);
        } else {
            lblInfoDuplicado.setText("Producto no encontrado");
            lblInfoDuplicado.setForeground(Color.RED);
        }
    }

    // --- LÓGICA DE ACCIONES ---

    private void unificar() {
        try {
            String codP = txtPrincipal.getText().trim();
            String codD = txtDuplicado.getText().trim();

            if(codP.isEmpty() || codD.isEmpty()) return;

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Confirmar Fusión?\nStock del duplicado se suma al principal.\nEl código duplicado pasará a la lista de asociados.",
                "Atención", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                controlador.unificarProductos(codP, codD);
                JOptionPane.showMessageDialog(this, "¡Fusión Exitosa!");
                
                // Limpiar duplicado
                txtDuplicado.setText("");
                lblInfoDuplicado.setText("-");
                
                // Recargar el principal para ver el nuevo código en la lista de la derecha
                buscarInfoPrincipal(); 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void eliminarAliasSeleccionado() {
        String codigoSeleccionado = listaCodigos.getSelectedValue();
        
        if (codigoSeleccionado == null || codigoSeleccionado.equals("(Sin códigos extra)")) {
            JOptionPane.showMessageDialog(this, "Seleccione un código de la lista.");
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this, 
                "¿Desea eliminar el código '" + codigoSeleccionado + "'?\nYa no servirá para buscar este producto.",
                "Desvincular Código", JOptionPane.YES_NO_OPTION);
        
        if (resp == JOptionPane.YES_OPTION) {
            // Llamamos al método nuevo de Empresa
            controlador.borrarCodigoSecundario(productoSeleccionado, codigoSeleccionado);
            
            // Refrescamos la lista visual
            cargarListaAlias();
        }
    }

    // --- UTILIDADES ---
    
    private void agregarBotonAyuda(int x, int y, String textoTooltip) {
        JButton btn = new JButton("?");
        btn.setBounds(x, y, 30, 30);
        
        // Estilo del botón (Igual al que tenías)
        btn.setBackground(new Color(52, 152, 219)); // Azul
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Asignamos el Tooltip
        btn.setToolTipText(textoTooltip);
        
        // Configuración de tiempos (Aparece rápido y dura 10 seg)
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        
        getContentPane().add(btn);
    }
    
    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}