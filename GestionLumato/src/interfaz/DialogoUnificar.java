package interfaz;

import javax.swing.*;
import java.awt.*;
import modelo.Empresa;
import modelo.Producto;

public class DialogoUnificar extends JDialog {

    private static final long serialVersionUID = 1L;
    private Empresa empresa;
    private JTextField txtPrincipal;
    private JTextField txtDuplicado;
    private JLabel lblInfoPrincipal;
    private JLabel lblInfoDuplicado;
    
    public DialogoUnificar(JFrame parent, Empresa empresa) {
        super(parent, "Unificar Códigos de Barra", true);
        this.empresa = empresa;
        
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("FUSIÓN DE PRODUCTOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setBounds(20, 20, 250, 30);
        add(lblTitulo);

        // --- BOTÓN DE AYUDA (?) ---
        JButton btnAyuda = new JButton("?");
        btnAyuda.setBounds(250, 20, 30, 30); // Al lado del título
        btnAyuda.setBackground(new Color(52, 152, 219)); // Azul info
        btnAyuda.setForeground(Color.WHITE);
        btnAyuda.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAyuda.setBorder(BorderFactory.createEmptyBorder()); // Sin borde para que parezca ícono
        btnAyuda.setFocusPainted(false);
        btnAyuda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // --- EL TEXTO DE AYUDA (TOOLTIP HTML) ---
        // Usamos HTML para formatear el texto, poner negritas y saltos de linea.
        String textoAyuda = "<html><body style='width: 250px; background-color: #FFFFE0; padding: 5px;'>"
                + "<b>¿Qué hace esta función?</b><br><br>"
                + "Sirve para corregir cuando un mismo producto tiene dos códigos distintos cargados en el sistema.<br><br>"
                + "<b>1. Stock:</b> Se suma el stock del Duplicado al Principal.<br>"
                + "<b>2. Códigos:</b> El código del duplicado se guarda como un 'Alias' dentro del Principal.<br>"
                + "<b>3. Limpieza:</b> El producto duplicado se borra.<br><br>"
                + "<i>Resultado: Al escanear cualquiera de los dos códigos, el sistema traerá siempre al Principal.</i>"
                + "</body></html>";
        
        // Seteamos el tooltip al botón
        btnAyuda.setToolTipText(textoAyuda);
        
        // Truco opcional: Hacer que el tooltip aparezca más rápido
        ToolTipManager.sharedInstance().setInitialDelay(100); 
        ToolTipManager.sharedInstance().setDismissDelay(10000); // Que dure 10 seg visible
        
        add(btnAyuda);

        // --- PRODUCTO PRINCIPAL (EL QUE QUEDA) ---
        JLabel lblP = new JLabel("1. Producto PRINCIPAL (El que queda):");
        lblP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblP.setForeground(new Color(39, 174, 96)); // Verde
        lblP.setBounds(20, 70, 400, 20);
        add(lblP);

        txtPrincipal = new JTextField();
        txtPrincipal.setBounds(20, 100, 200, 30);
        txtPrincipal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPrincipal.addActionListener(e -> buscarInfo(txtPrincipal, lblInfoPrincipal));
        add(txtPrincipal);

        lblInfoPrincipal = new JLabel("-");
        lblInfoPrincipal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoPrincipal.setBounds(230, 100, 340, 30);
        add(lblInfoPrincipal);

        // --- PRODUCTO DUPLICADO (EL QUE SE BORRA) ---
        JLabel lblD = new JLabel("2. Producto DUPLICADO (Se elimina y suma stock):");
        lblD.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblD.setForeground(new Color(192, 57, 43)); // Rojo
        lblD.setBounds(20, 160, 400, 20);
        add(lblD);

        txtDuplicado = new JTextField();
        txtDuplicado.setBounds(20, 190, 200, 30);
        txtDuplicado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDuplicado.addActionListener(e -> buscarInfo(txtDuplicado, lblInfoDuplicado));
        add(txtDuplicado);

        lblInfoDuplicado = new JLabel("-");
        lblInfoDuplicado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoDuplicado.setBounds(230, 190, 340, 30);
        add(lblInfoDuplicado);

        // --- BOTÓN ACCIÓN ---
        JButton btnUnificar = new JButton("FUSIONAR PRODUCTOS");
        btnUnificar.setBounds(150, 280, 300, 50);
        btnUnificar.setBackground(new Color(44, 62, 80));
        btnUnificar.setForeground(Color.WHITE);
        btnUnificar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnUnificar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUnificar.addActionListener(e -> unificar());
        add(btnUnificar);
    }

    private void buscarInfo(JTextField txt, JLabel lbl) {
        String cod = txt.getText().trim();
        if (cod.isEmpty()) return;
        
        Producto p = empresa.buscarProducto(cod);
        if (p != null) {
            lbl.setText("<html>" + p.getDescripcion() + " | Stock Actual: <b>" + p.getCantidadStock() + "</b></html>");
            lbl.setForeground(Color.BLACK);
        } else {
            lbl.setText("Producto no encontrado");
            lbl.setForeground(Color.RED);
        }
    }

    private void unificar() {
        try {
            String codP = txtPrincipal.getText().trim();
            String codD = txtDuplicado.getText().trim();

            if(codP.isEmpty() || codD.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete ambos campos.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Estás seguro?\nSe eliminará el producto duplicado y su stock pasará al principal.",
                "Confirmar Fusión", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                empresa.unificarProductos(codP, codD);
                JOptionPane.showMessageDialog(this, "¡Fusión Exitosa! Stock unificado.");
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}