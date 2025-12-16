package interfaz;

import javax.swing.*;
import java.awt.*;
import modelo.Categoria;
import modelo.Empresa;
import controlador.ControladorCategoria;

public class DialogoNuevaCategoria extends JDialog {

    private static final long serialVersionUID = 1L;
    
    // --- CONTROLADOR ---
    private ControladorCategoria controlador;
    
    // --- COMPONENTES VISUALES ---
    // Pestaña Crear
    private JTextField txtNombreNuevo;
    private JCheckBox chkEsSubcategoria;
    private JComboBox<Categoria> cmbMadresCrear;
    private JLabel lblPerteneceA;
    
    // Pestaña Editar
    private JComboBox<Categoria> cmbTodasEditar;
    private JTextField txtNombreEditar;

    public DialogoNuevaCategoria(JFrame parent, Empresa empresa) {
        super(parent, "Gestión de Categorías", true);
        
        // 1. Inicializamos el controlador
        this.controlador = new ControladorCategoria(empresa);

        setSize(500, 350); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // 2. Sistema de Pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabs.addTab("Crear Nueva", crearPanelCrear());
        tabs.addTab("Editar / Renombrar", crearPanelEditar());
        
        // Evento: Recargar listas al cambiar de pestaña
        tabs.addChangeListener(e -> cargarListas());

        add(tabs, BorderLayout.CENTER);
        
        // Carga inicial
        cargarListas();
    }

    // ==========================================
    // PESTAÑA 1: CREAR
    // ==========================================
    private JPanel crearPanelCrear() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 246, 250));

        JLabel lblInfo = new JLabel("Nombre de la Categoría:");
        lblInfo.setBounds(30, 20, 300, 20);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lblInfo);

        txtNombreNuevo = new JTextField();
        txtNombreNuevo.setBounds(30, 45, 420, 35);
        txtNombreNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtNombreNuevo);

        chkEsSubcategoria = new JCheckBox("Es una Subcategoría (Hija)");
        chkEsSubcategoria.setBounds(30, 90, 300, 25);
        chkEsSubcategoria.setBackground(new Color(245, 246, 250));
        chkEsSubcategoria.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(chkEsSubcategoria);

        lblPerteneceA = new JLabel("Pertenece a (Categoría Madre):");
        lblPerteneceA.setBounds(50, 125, 300, 20);
        lblPerteneceA.setEnabled(false);
        panel.add(lblPerteneceA);

        cmbMadresCrear = new JComboBox<>();
        cmbMadresCrear.setBounds(50, 150, 400, 30);
        cmbMadresCrear.setEnabled(false);
        panel.add(cmbMadresCrear);

        // Lógica visual del Checkbox
        chkEsSubcategoria.addActionListener(e -> {
            boolean esHija = chkEsSubcategoria.isSelected();
            lblPerteneceA.setEnabled(esHija);
            cmbMadresCrear.setEnabled(esHija);
        });

        JButton btnGuardar = new JButton("GUARDAR NUEVA");
        btnGuardar.setBounds(130, 210, 230, 40);
        estilizarBoton(btnGuardar, new Color(39, 174, 96)); // Verde
        
        // ACCIÓN: Delegamos al controlador
        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombreNuevo.getText();
                boolean esSub = chkEsSubcategoria.isSelected();
                Categoria madre = (Categoria) cmbMadresCrear.getSelectedItem();

                controlador.guardarNuevaCategoria(nombre, esSub, madre);

                JOptionPane.showMessageDialog(this, "Categoría creada con éxito.");
                limpiarCrear();
                cargarListas(); // Refrescar combos

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnGuardar);

        return panel;
    }

    // ==========================================
    // PESTAÑA 2: EDITAR
    // ==========================================
    private JPanel crearPanelEditar() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 246, 250));

        JLabel lblSel = new JLabel("Seleccione Categoría:");
        lblSel.setBounds(30, 30, 200, 20);
        panel.add(lblSel);

        cmbTodasEditar = new JComboBox<>();
        cmbTodasEditar.setBounds(30, 55, 420, 30);
        
        // Al seleccionar del combo, llenamos el campo de texto
        cmbTodasEditar.addActionListener(e -> {
            Categoria c = (Categoria) cmbTodasEditar.getSelectedItem();
            if (c != null) txtNombreEditar.setText(c.getNombre());
        });
        panel.add(cmbTodasEditar);

        JLabel lblNom = new JLabel("Nuevo Nombre:");
        lblNom.setBounds(30, 100, 200, 20);
        panel.add(lblNom);

        txtNombreEditar = new JTextField();
        txtNombreEditar.setBounds(30, 125, 420, 35);
        panel.add(txtNombreEditar);

        JButton btnEditar = new JButton("GUARDAR CAMBIOS");
        btnEditar.setBounds(130, 190, 230, 40);
        estilizarBoton(btnEditar, new Color(230, 126, 34)); // Naranja
        
        // ACCIÓN: Delegamos al controlador
        btnEditar.addActionListener(e -> {
            try {
                Categoria cat = (Categoria) cmbTodasEditar.getSelectedItem();
                String nuevoNombre = txtNombreEditar.getText();
                
                controlador.modificarNombre(cat, nuevoNombre);
                
                JOptionPane.showMessageDialog(this, "Actualizado correctamente.");
                cargarListas();
                txtNombreEditar.setText("");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnEditar);

        return panel;
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private void cargarListas() {
        // Guardamos selección actual para intentar restaurarla luego
        Object seleccionMadre = cmbMadresCrear.getSelectedItem();
        Object seleccionTodas = cmbTodasEditar.getSelectedItem();

        cmbMadresCrear.removeAllItems();
        for (Categoria c : controlador.obtenerCategoriasMadre()) {
            cmbMadresCrear.addItem(c);
        }

        cmbTodasEditar.removeAllItems();
        for (Categoria c : controlador.obtenerTodas()) {
            cmbTodasEditar.addItem(c);
        }
        
        // Restaurar selección si sigue existiendo (Mejora UX)
        if (seleccionMadre != null) cmbMadresCrear.setSelectedItem(seleccionMadre);
        if (seleccionTodas != null) cmbTodasEditar.setSelectedItem(seleccionTodas);
    }

    private void limpiarCrear() {
        txtNombreNuevo.setText("");
        chkEsSubcategoria.setSelected(false);
        lblPerteneceA.setEnabled(false);
        cmbMadresCrear.setEnabled(false);
        if (cmbMadresCrear.getItemCount() > 0) cmbMadresCrear.setSelectedIndex(0);
    }
    
    private void estilizarBoton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}