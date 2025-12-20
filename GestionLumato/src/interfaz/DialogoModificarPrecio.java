package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import modelo.Empresa;
import modelo.Producto;
import controlador.ControladorStock;

public class DialogoModificarPrecio extends JDialog {

    private static final long serialVersionUID = 1L;
    private Producto producto;
    
    // AHORA USAMOS EL COMPONENTE REUTILIZABLE
    private PanelPrecios panelPrecios; 

    public DialogoModificarPrecio(JFrame parent, Empresa empresa, Producto producto) {
        super(parent, "Modificar Precio - " + producto.getDescripcion(), true);
        this.producto = producto;

        setSize(500, 420);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // HEADER
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(new Color(44, 62, 80));
        panelHeader.setBounds(0, 0, 500, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("ACTUALIZAR PRECIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 300, 30);
        panelHeader.add(lblTitulo);

        // INFO
        JLabel lblInfo = new JLabel("Producto: " + producto.getDescripcion());
        lblInfo.setBounds(20, 80, 450, 20);
        add(lblInfo);

        // --- AQUÍ ESTÁ EL CAMBIO: INSERTAMOS EL PANEL ---
        panelPrecios = new PanelPrecios(new ControladorStock(empresa));
        panelPrecios.setBounds(20, 110, 460, 150); // Lo ubicamos
        
        // Le pasamos los datos del producto actual
        panelPrecios.setValores(
            producto.getPrecioCosto(), 
            producto.getPorcentajeGanancia(), 
            producto.getAlicuotaIVA()
        );
        add(panelPrecios);
        // ------------------------------------------------

        // BOTONES
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS (F12)");
        btnGuardar.setBounds(140, 300, 300, 40);
        btnGuardar.setBackground(new Color(39, 174, 96));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardarCambios());
        add(btnGuardar);
        
        // CONEXIÓN DE EVENTOS
        // Cuando den Enter en el último campo del panel, que dispare guardar
        panelPrecios.setOnEnterAlFinal(e -> guardarCambios());

        // Atajo F12
        getRootPane().registerKeyboardAction(e -> guardarCambios(), 
            KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Foco inicial
        SwingUtilities.invokeLater(() -> panelPrecios.darFocoInicial());
    }

    private void guardarCambios() {
        try {
            // OBTENEMOS DATOS DEL PANEL
            String costoStr = panelPrecios.getCosto();
            String gananciaStr = panelPrecios.getGanancia();
            String ivaStr = panelPrecios.getIVA();

            if (costoStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El costo es obligatorio.");
                return;
            }

            // Convertimos
            BigDecimal nuevoCosto = new BigDecimal(costoStr.replace(",", "."));
            BigDecimal nuevaGanancia = new BigDecimal(gananciaStr.replace(",", ".")).divide(new BigDecimal(100));
            BigDecimal nuevoIVA = new BigDecimal(ivaStr).divide(new BigDecimal(100));

            // Guardamos
            producto.setPrecioCosto(nuevoCosto);
            producto.setPorcentajeGanancia(nuevaGanancia);
            producto.setAlicuotaIVA(nuevoIVA);
            
            JOptionPane.showMessageDialog(this, "Precio actualizado correctamente.");
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en los datos: " + e.getMessage());
        }
    }
}