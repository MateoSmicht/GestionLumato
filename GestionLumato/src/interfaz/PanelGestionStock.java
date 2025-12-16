package interfaz;


import javax.swing.*;
import java.awt.*;
import modelo.Empresa;

public class PanelGestionStock extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // --- BOTONES PÚBLICOS (Para que el MainForm les asigne acciones de navegación) ---
    public JButton btnAltaProducto;
    public JButton btnReposicion;
    public JButton btnVolver;

    // --- COLORES FLAT UI ---
    private final Color COLOR_FONDO = new Color(245, 246, 250);
    private final Color COLOR_HEADER = new Color(44, 62, 80);
    
    // Colores de Botones
    private final Color COL_AZUL    = new Color(52, 152, 219); // Alta
    private final Color COL_VERDE   = new Color(39, 174, 96);  // Reposición
    private final Color COL_NARANJA = new Color(230, 126, 34); // Unificar
    private final Color COL_AMARILLO= new Color(241, 196, 15); // Categoría
    private final Color COL_VIOLETA = new Color(142, 68, 173); // Consulta

    public PanelGestionStock(Empresa empresa) {
        setLayout(null);
        setBackground(COLOR_FONDO);
        setBounds(0, 0, 784, 500);

        // =========================================================
        // 1. HEADER
        // =========================================================
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBounds(0, 0, 784, 80);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("MENÚ DE STOCK");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(30, 20, 300, 40);
        panelHeader.add(lblTitulo);

        // Botón Volver (Arriba a la derecha)
        btnVolver = new JButton("Volver al Inicio");
        btnVolver.setBounds(600, 25, 150, 35);
        estilizarBoton(btnVolver, new Color(149, 165, 166), Color.WHITE);
        panelHeader.add(btnVolver);

        // =========================================================
        // 2. GRID DE BOTONES
        // =========================================================
        
        // Coordenadas base
        int margenIzq = 40;
        int anchoBtnMedio = 340; // Para botones que van de a 2
        int anchoBtnFull = 700;  // Para botones que ocupan todo
        int gap = 20;            // Espacio entre botones

        // --- FILA 1: ACCIONES PRINCIPALES (y = 100) ---
        int fila1 = 100;
        int altoFila1 = 100;

        // 1. ALTA DE PRODUCTO (Izquierda)
        btnAltaProducto = new JButton("<html><center><h2>ALTA DE PRODUCTO</h2><br>Crear artículos nuevos</center></html>");
        btnAltaProducto.setBounds(margenIzq, fila1, anchoBtnMedio, altoFila1);
        estilizarBoton(btnAltaProducto, COL_AZUL, Color.WHITE);
        add(btnAltaProducto);

        // 2. REPOSICIÓN (Derecha)
        btnReposicion = new JButton("<html><center><h2>REPOSICIÓN</h2><br>Cargar mercadería existente</center></html>");
        btnReposicion.setBounds(margenIzq + anchoBtnMedio + gap, fila1, anchoBtnMedio, altoFila1);
        estilizarBoton(btnReposicion, COL_VERDE, Color.WHITE);
        add(btnReposicion);


        // --- FILA 2: HERRAMIENTAS DE GESTIÓN (y = 220) ---
        int fila2 = 220;
        int altoFila2 = 90;

        // 3. UNIFICAR CÓDIGOS (Izquierda)
        JButton btnUnificar = new JButton("<html><center><h3>FUSIONAR DUPLICADOS</h3><br>Corregir códigos dobles</center></html>");
        btnUnificar.setBounds(margenIzq, fila2, anchoBtnMedio, altoFila2);
        estilizarBoton(btnUnificar, COL_NARANJA, Color.WHITE);
        btnUnificar.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            DialogoUnificar dialog = new DialogoUnificar(parent, empresa);
            dialog.setVisible(true);
        });
        add(btnUnificar);

        // 4. NUEVA CATEGORÍA (Derecha)
        JButton btnCategoria = new JButton("<html><center><h3>GESTIONAR CATEGORÍAS</h3><br>Crear y editar familias</center></html>");
        btnCategoria.setBounds(margenIzq + anchoBtnMedio + gap, fila2, anchoBtnMedio, altoFila2);
        estilizarBoton(btnCategoria, COL_AMARILLO, Color.WHITE);
        btnCategoria.setForeground(new Color(44, 62, 80)); // Texto oscuro para el amarillo
        btnCategoria.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            DialogoNuevaCategoria dialog = new DialogoNuevaCategoria(parent, empresa);
            dialog.setVisible(true);
        });
        add(btnCategoria);


        // --- FILA 3: INFORMES Y BÚSQUEDA (y = 330) ---
        int fila3 = 330;
        int altoFila3 = 90;

        // 5. CONSULTAR STOCK (Full Ancho)
        JButton btnConsulta = new JButton("<html><center><h2>CONSULTA Y BUSCADOR</h2><br>Filtrar por nombre, categoría y stock bajo</center></html>");
        btnConsulta.setBounds(margenIzq, fila3, anchoBtnFull, altoFila3);
        estilizarBoton(btnConsulta, COL_VIOLETA, Color.WHITE);
        
        btnConsulta.addActionListener(e -> {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Buscador de Stock", true);
            dialog.setSize(950, 600); // Un poco más ancho
            dialog.setLocationRelativeTo(null);
            dialog.add(new PanelConsultaStock(empresa)); 
            dialog.setVisible(true);
        });
        add(btnConsulta);
    }
    

    // Método auxiliar para dar estilo a los botones
    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // Estilo Flat total
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}