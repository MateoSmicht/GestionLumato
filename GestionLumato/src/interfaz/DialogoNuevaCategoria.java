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
    
    // 1. Pestaña Crear
    private JTextField txtNombreNuevo;
    private JCheckBox chkEsSubcategoria;
    private JComboBox<Categoria> cmbMadresCrear;
    private JLabel lblPerteneceA;
    
    // 2. Pestaña Editar
    private JComboBox<Categoria> cmbTodasEditar;
    private JTextField txtNombreEditar;
    private JComboBox<Object> cmbPadreEditar;
    
    // 3. Pestaña Eliminar (NUEVO)
    private JComboBox<Categoria> cmbTodasEliminar;

    public DialogoNuevaCategoria(JFrame parent, Empresa empresa) {
        super(parent, "Gestión de Categorías", true);
        
        this.controlador = new ControladorCategoria(empresa);

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Pestañas existentes
        tabs.addTab("Crear Nueva", crearPanelCrear());
        tabs.addTab("Editar / Mover", crearPanelEditar());
        
        // --- NUEVA PESTAÑA AGREGADA ---
        tabs.addTab("Eliminar", crearPanelEliminar());
        
        tabs.addChangeListener(e -> cargarListas());

        add(tabs, BorderLayout.CENTER);
        
        cargarListas();
    }

    // ==========================================
    // PESTAÑA 1: CREAR (SIN CAMBIOS)
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

        chkEsSubcategoria.addActionListener(e -> {
            boolean esHija = chkEsSubcategoria.isSelected();
            lblPerteneceA.setEnabled(esHija);
            cmbMadresCrear.setEnabled(esHija);
        });

        JButton btnGuardar = new JButton("GUARDAR NUEVA");
        btnGuardar.setBounds(130, 230, 230, 40);
        estilizarBoton(btnGuardar, new Color(39, 174, 96), Color.WHITE);
        
        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombreNuevo.getText();
                boolean esSub = chkEsSubcategoria.isSelected();
                Categoria madre = (Categoria) cmbMadresCrear.getSelectedItem();

                controlador.guardarNuevaCategoria(nombre, esSub, madre);

                JOptionPane.showMessageDialog(this, "Categoría creada con éxito.");
                limpiarCrear();
                cargarListas(); 

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnGuardar);
        return panel;
    }

    // ==========================================
    // PESTAÑA 2: EDITAR (SIN CAMBIOS)
    // ==========================================
    private JPanel crearPanelEditar() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 246, 250));

        JLabel lblSel = new JLabel("Seleccione Categoría a editar:");
        lblSel.setBounds(30, 20, 250, 20);
        panel.add(lblSel);

        cmbTodasEditar = new JComboBox<>();
        cmbTodasEditar.setBounds(30, 45, 420, 30);
        panel.add(cmbTodasEditar);

        JLabel lblNom = new JLabel("Nombre:");
        lblNom.setBounds(30, 90, 200, 20);
        panel.add(lblNom);

        txtNombreEditar = new JTextField();
        txtNombreEditar.setBounds(30, 115, 200, 35); 
        panel.add(txtNombreEditar);

        JLabel lblPadre = new JLabel("Mover a (Rubro Padre):");
        lblPadre.setBounds(250, 90, 200, 20);
        panel.add(lblPadre);

        cmbPadreEditar = new JComboBox<>();
        cmbPadreEditar.setBounds(250, 115, 200, 35);
        panel.add(cmbPadreEditar);

        cmbTodasEditar.addActionListener(e -> {
            Categoria seleccionada = (Categoria) cmbTodasEditar.getSelectedItem();
            if (seleccionada != null) {
                txtNombreEditar.setText(seleccionada.getNombre());
                actualizarComboPadres(seleccionada);
            }
        });

        JButton btnEditar = new JButton("GUARDAR CAMBIOS");
        btnEditar.setBounds(130, 200, 230, 40);
        estilizarBoton(btnEditar, new Color(230, 126, 34), Color.WHITE);
        
        btnEditar.addActionListener(e -> {
            try {
                Categoria cat = (Categoria) cmbTodasEditar.getSelectedItem();
                String nuevoNombre = txtNombreEditar.getText();
                
                Object itemPadre = cmbPadreEditar.getSelectedItem();
                Categoria nuevaMadre = null;
                if (itemPadre instanceof Categoria) {
                    nuevaMadre = (Categoria) itemPadre;
                }
                
                controlador.modificarCategoria(cat, nuevoNombre, nuevaMadre);
                
                JOptionPane.showMessageDialog(this, "Categoría actualizada correctamente.");
                cargarListas();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnEditar);
        return panel;
    }

    // ==========================================
    // PESTAÑA 3: ELIMINAR (AGREGADO NUEVO)
    // ==========================================
    private JPanel crearPanelEliminar() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 246, 250));

        JLabel lblTitulo = new JLabel("ELIMINAR CATEGORÍA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(192, 57, 43));
        lblTitulo.setBounds(30, 20, 300, 25);
        panel.add(lblTitulo);

        JLabel lblInfo = new JLabel("<html><body style='width: 380px'>Solo se pueden eliminar categorías que no tengan productos asociados ni subcategorías.<br>Si desea borrar una con productos, primero muévalos o bórrelos.</body></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBounds(30, 50, 420, 50);
        panel.add(lblInfo);

        JLabel lblSel = new JLabel("Seleccione Categoría a borrar:");
        lblSel.setBounds(30, 110, 250, 20);
        panel.add(lblSel);

        cmbTodasEliminar = new JComboBox<>();
        cmbTodasEliminar.setBounds(30, 135, 420, 35);
        panel.add(cmbTodasEliminar);

        JButton btnEliminar = new JButton("ELIMINAR DEFINITIVAMENTE");
        btnEliminar.setBounds(100, 200, 300, 40);
        estilizarBoton(btnEliminar, new Color(192, 57, 43), Color.WHITE); // Rojo
        
        btnEliminar.addActionListener(e -> {
            Categoria cat = (Categoria) cmbTodasEliminar.getSelectedItem();
            if (cat == null) return;

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Estás seguro de eliminar '" + cat.getNombre() + "'?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    controlador.eliminarCategoria(cat); // Asegurate que este método exista en tu controlador
                    JOptionPane.showMessageDialog(this, "Categoría eliminada.");
                    cargarListas();
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
        cmbMadresCrear.removeAllItems();
        cmbTodasEditar.removeAllItems();
        
        // --- NUEVO: Limpiamos combo eliminar ---
        cmbTodasEliminar.removeAllItems();
        
        for (Categoria c : controlador.obtenerCategoriasMadre()) {
            cmbMadresCrear.addItem(c);
        }

        for (Categoria c : controlador.obtenerTodas()) {
            cmbTodasEditar.addItem(c);
            // --- NUEVO: Cargamos combo eliminar ---
            cmbTodasEliminar.addItem(c);
        }
    }
    
    private void actualizarComboPadres(Categoria catActual) {
        cmbPadreEditar.removeAllItems();
        cmbPadreEditar.addItem("--- Es Principal ---");
        
        for (Categoria posibleMadre : controlador.obtenerCategoriasMadre()) {
            if (posibleMadre.getId() != catActual.getId()) {
                cmbPadreEditar.addItem(posibleMadre);
            }
        }
        
        if (catActual.getIdPadre() == null) {
            cmbPadreEditar.setSelectedIndex(0); 
        } else {
            for (int i = 1; i < cmbPadreEditar.getItemCount(); i++) {
                Categoria c = (Categoria) cmbPadreEditar.getItemAt(i);
                if (c.getId() == catActual.getIdPadre()) {
                    cmbPadreEditar.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void limpiarCrear() {
        txtNombreNuevo.setText("");
        chkEsSubcategoria.setSelected(false);
        lblPerteneceA.setEnabled(false);
        cmbMadresCrear.setEnabled(false);
        if (cmbMadresCrear.getItemCount() > 0) cmbMadresCrear.setSelectedIndex(0);
    }
    
    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}