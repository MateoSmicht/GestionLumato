package interfaz;

import javax.swing.*;
import java.awt.*;

import modelo.Empresa;

public class PanelGestionStock extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // --- BOTONES PÚBLICOS PARA QUE MAINFORM LOS ESCUCHE ---
    public JButton btnAltaProducto;
    public JButton btnReposicion;
    public JButton btnVolver;

    // --- COLORES ---
    private final Color COLOR_FONDO = new Color(245, 246, 250);
    private final Color COLOR_HEADER = new Color(44, 62, 80);
    private final Color COLOR_VERDE = new Color(39, 174, 96);
    private final Color COLOR_AZUL = new Color(52, 152, 219);

    public PanelGestionStock(Empresa empresa) {
        setLayout(null);
        setBackground(COLOR_FONDO);
        setBounds(0, 0, 784, 500);

        // =========================================================
        // 1. HEADER
        // =========================================================
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(null);
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBounds(0, 0, 784, 80);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("MENÚ DE STOCK");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 20, 400, 40);
        panelHeader.add(lblTitulo);

        btnVolver = new JButton("Volver al Inicio");
        btnVolver.setBounds(630, 25, 130, 35);
        estilizarBoton(btnVolver, new Color(149, 165, 166), Color.WHITE);
        panelHeader.add(btnVolver);

        // =========================================================
        // 2. BOTONES DE OPCIONES (Grandes y Centrados)
        // =========================================================
        
        JLabel lblInfo = new JLabel("Seleccione una operación:");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBounds(50, 120, 300, 30);
        add(lblInfo);

        // Opción 1: ALTA DE PRODUCTO
        btnAltaProducto = new JButton("<html><center><h2>ALTA DE PRODUCTO</h2><br>Crear artículos nuevos</center></html>");
        btnAltaProducto.setBounds(50, 170, 300, 150);
        estilizarBoton(btnAltaProducto, COLOR_AZUL, Color.WHITE);
        add(btnAltaProducto);

        // Opción 2: REPOSICIÓN (Carga de Stock)
        btnReposicion = new JButton("<html><center><h2>REPOSICIÓN</h2><br>Cargar mercadería existente</center></html>");
        btnReposicion.setBounds(400, 170, 300, 150);
        estilizarBoton(btnReposicion, COLOR_VERDE, Color.WHITE);
        add(btnReposicion);
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