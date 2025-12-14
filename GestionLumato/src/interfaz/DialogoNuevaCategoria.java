package interfaz;

import javax.swing.*;
import java.awt.*;
import modelo.Categoria;
import modelo.Empresa;

public class DialogoNuevaCategoria extends JDialog {

    private static final long serialVersionUID = 1L;
    private Empresa empresa;
    
    // Componentes Pestaña Crear
    private JTextField txtNombreNuevo;
    
    // Componentes Pestaña Editar
    private JComboBox<Categoria> cmbCategorias;
    private JTextField txtNombreEditar;

    public DialogoNuevaCategoria(JFrame parent, Empresa empresa) {
        super(parent, "Gestión de Categorías", true);
        this.empresa = empresa;

        setSize(450, 300); // Un poco más grande para las pestañas
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // --- SISTEMA DE PESTAÑAS ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Pestaña 1: CREAR
        tabs.addTab("Crear Nueva", crearPanelCrear());
        
        // Pestaña 2: EDITAR
        tabs.addTab("Editar Existente", crearPanelEditar());
        
        // Evento: Al cambiar a la pestaña Editar, recargar el Combo
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                cargarComboCategorias();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }

    // ==========================================
    // PESTAÑA 1: CREAR
    // ==========================================
    private JPanel crearPanelCrear() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 246, 250));

        JLabel lblInfo = new JLabel("Ingrese el nombre de la nueva categoría:");
        lblInfo.setBounds(30, 30, 300, 20);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lblInfo);

        txtNombreNuevo = new JTextField();
        txtNombreNuevo.setBounds(30, 60, 370, 35);
        txtNombreNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtNombreNuevo);

        JButton btnGuardar = new JButton("GUARDAR NUEVA");
        btnGuardar.setBounds(100, 130, 230, 40);
        estilizarBoton(btnGuardar, new Color(39, 174, 96)); // Verde
        
        btnGuardar.addActionListener(e -> accionCrear());
        panel.add(btnGuardar);

        return panel;
    }

    // ==========================================
    // PESTAÑA 2: EDITAR
    // ==========================================
    private JPanel crearPanelEditar() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 246, 250));

        JLabel lblSel = new JLabel("1. Seleccione Categoría:");
        lblSel.setBounds(30, 20, 200, 20);
        lblSel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblSel);

        cmbCategorias = new JComboBox<>();
        cmbCategorias.setBounds(30, 45, 370, 30);
        // Al seleccionar una categoría, ponemos su nombre en la caja de texto
        cmbCategorias.addActionListener(e -> {
            Categoria c = (Categoria) cmbCategorias.getSelectedItem();
            if (c != null) txtNombreEditar.setText(c.getNombre());
        });
        panel.add(cmbCategorias);

        JLabel lblNom = new JLabel("2. Corregir Nombre:");
        lblNom.setBounds(30, 90, 200, 20);
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblNom);

        txtNombreEditar = new JTextField();
        txtNombreEditar.setBounds(30, 115, 370, 35);
        txtNombreEditar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtNombreEditar);

        JButton btnEditar = new JButton("GUARDAR CAMBIOS");
        btnEditar.setBounds(100, 170, 230, 40);
        estilizarBoton(btnEditar, new Color(230, 126, 34)); // Naranja
        
        btnEditar.addActionListener(e -> accionEditar());
        panel.add(btnEditar);

        return panel;
    }

    // ==========================================
    // LÓGICA
    // ==========================================
    
    private void accionCrear() {
        String nombre = txtNombreNuevo.getText().trim();
        if (nombre.isEmpty()) return;

        try {
            empresa.crearCategoria(nombre);
            JOptionPane.showMessageDialog(this, "¡Categoría creada!");
            txtNombreNuevo.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEditar() {
        Categoria cat = (Categoria) cmbCategorias.getSelectedItem();
        String nuevoNombre = txtNombreEditar.getText().trim();

        if (cat == null || nuevoNombre.isEmpty()) return;

        try {
            empresa.modificarCategoria(cat, nuevoNombre);
            JOptionPane.showMessageDialog(this, "¡Nombre actualizado correctamente!");
            cargarComboCategorias(); // Refrescar la lista para ver el cambio
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarComboCategorias() {
        cmbCategorias.removeAllItems();
        for (Categoria c : empresa.getCategorias()) {
            cmbCategorias.addItem(c);
        }
    }

    private void estilizarBoton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}