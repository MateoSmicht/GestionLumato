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
    private JComboBox<Object> cmbPadreEditar;

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

        // 1. SELECCIONAR CATEGORÍA A CORREGIR
        JLabel lblSel = new JLabel("Seleccione Categoría a editar:");
        lblSel.setBounds(30, 20, 250, 20);
        panel.add(lblSel);

        cmbTodasEditar = new JComboBox<>();
        cmbTodasEditar.setBounds(30, 45, 420, 30);
        panel.add(cmbTodasEditar);

        // 2. CAMPO NOMBRE (Izquierda)
        JLabel lblNom = new JLabel("Nombre:");
        lblNom.setBounds(30, 90, 200, 20);
        panel.add(lblNom);

        txtNombreEditar = new JTextField();
        txtNombreEditar.setBounds(30, 115, 200, 35); 
        panel.add(txtNombreEditar);

        // 3. CAMPO PADRE (Derecha) - AQUÍ ESTÁ LA MAGIA
        JLabel lblPadre = new JLabel("Mover a (Rubro Padre):");
        lblPadre.setBounds(250, 90, 200, 20);
        panel.add(lblPadre);

        cmbPadreEditar = new JComboBox<>();
        cmbPadreEditar.setBounds(250, 115, 200, 35); 
        panel.add(cmbPadreEditar);

        // --- LÓGICA DE SELECCIÓN ---
        // Cuando eliges una categoría arriba, se llenan los campos de abajo automáticamente
        cmbTodasEditar.addActionListener(e -> {
            Categoria seleccionada = (Categoria) cmbTodasEditar.getSelectedItem();
            if (seleccionada != null) {
                // A. Poner nombre actual
                txtNombreEditar.setText(seleccionada.getNombre());
                
                // B. Poner el padre actual en el combo de la derecha
                actualizarComboPadres(seleccionada);
            }
        });

        // BOTÓN GUARDAR
        JButton btnEditar = new JButton("GUARDAR CAMBIOS");
        btnEditar.setBounds(130, 200, 230, 40);
        
        
        btnEditar.addActionListener(e -> {
            try {
                Categoria cat = (Categoria) cmbTodasEditar.getSelectedItem();
                String nuevoNombre = txtNombreEditar.getText();
                
                // Obtenemos el nuevo padre (puede ser "Sin Padre" o una Categoria)
                Object itemPadre = cmbPadreEditar.getSelectedItem();
                Categoria nuevaMadre = null;
                
                if (itemPadre instanceof Categoria) {
                    nuevaMadre = (Categoria) itemPadre;
                }
                // Si seleccionó el String "--- Es Principal ---", nuevaMadre queda null (correcto)

                controlador.modificarCategoria(cat, nuevoNombre, nuevaMadre);
                
                JOptionPane.showMessageDialog(this, "¡Categoría movida y actualizada!");
                cargarListas(); // Refrescar todo para ver cambios
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnEditar);

        return panel;
    }

    // --- MÉTODO AUXILIAR CRÍTICO ---
    // Llena el combo de la derecha filtrando para que no te elijas a ti mismo
    private void actualizarComboPadres(Categoria catActual) {
        cmbPadreEditar.removeAllItems();
        
        // Opción 1: Que sea principal (sin padre)
        cmbPadreEditar.addItem("--- Es Principal ---");
        
        // Opción 2: Listar todas las posibles madres
        for (Categoria posibleMadre : controlador.obtenerCategoriasMadre()) {
            // REGLA DE ORO: No puedes ser tu propio padre
            if (posibleMadre.getId() != catActual.getId()) {
                cmbPadreEditar.addItem(posibleMadre);
            }
        }
        
        // PRE-SELECCIONAR EL PADRE ACTUAL
        if (catActual.getIdPadre() == null) {
            cmbPadreEditar.setSelectedIndex(0); // "Es Principal"
        } else {
            // Buscar la categoría padre en el combo y seleccionarla visualmente
            for (int i = 1; i < cmbPadreEditar.getItemCount(); i++) {
                Categoria c = (Categoria) cmbPadreEditar.getItemAt(i);
                if (c.getId() == catActual.getIdPadre()) {
                    cmbPadreEditar.setSelectedIndex(i);
                    break;
                }
            }
        }
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