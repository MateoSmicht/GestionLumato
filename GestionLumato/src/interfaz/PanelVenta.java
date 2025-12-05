package interfaz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import controlador.ControladorVenta;
import modelo.Empresa;
import modelo.Usuario;
import modelo.Producto;
import modelo.Venta;
import modelo.DetalleVenta;

public class PanelVenta extends JPanel {

    private static final long serialVersionUID = 1L;

    // --- EL CEREBRO ---
    private ControladorVenta controlador;

    // --- COMPONENTES VISUALES ---
    private JTextField txtBusquedaProducto;
    private JTable tableDetalle;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalPagar;

    /**
     * Constructor
     */
    public PanelVenta(Empresa empresa, Usuario vendedor) {
        // Inicializamos el controlador con los datos del sistema
        this.controlador = new ControladorVenta(empresa, vendedor);

        setLayout(null);
        setBounds(0, 0, 784, 500);

        // ---------------------------------------------------------
        // 1. ZONA SUPERIOR (Buscador y Botones F)
        // ---------------------------------------------------------
        JLabel lblBuscar = new JLabel("INGRESE CÓDIGO:");
        lblBuscar.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblBuscar.setBounds(20, 30, 150, 25);
        add(lblBuscar);

        txtBusquedaProducto = new JTextField();
        txtBusquedaProducto.setBounds(160, 20, 280, 40);
        txtBusquedaProducto.setFont(new Font("Tahoma", Font.PLAIN, 14));
        add(txtBusquedaProducto);

        // Enter en el buscador
        txtBusquedaProducto.addActionListener(e -> agregarProductoPorInput());

        // Botón F5 (Buscar Nombre)
        JButton btnBuscar = crearteBotonAccion("<html><center>Buscar<br>Nombre (F5)</center></html>", 460, 20, new Color(0, 0, 128));
        btnBuscar.addActionListener(e -> abrirBusquedaPorNombre());
        add(btnBuscar);

        // Botón F6 (Consulta Precio)
        JButton btnPrecio = crearteBotonAccion("<html><center>Consultar<br>Precio (F6)</center></html>", 570, 20, new Color(255, 140, 0));
        btnPrecio.addActionListener(e -> abrirConsultaPrecio());
        add(btnPrecio);

        // ---------------------------------------------------------
        // 2. ZONA CENTRAL (Tabla y Eliminar)
        // ---------------------------------------------------------
        String[] columnas = { "Cód.", "Descripción", "Precio Unit.", "Cant.", "Subtotal" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableDetalle = new JTable(modeloTabla);
        tableDetalle.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tableDetalle.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tableDetalle);
        scrollPane.setBounds(20, 80, 640, 320);
        add(scrollPane);

        // Botón F7 (Eliminar)
        JButton btnEliminar = crearteBotonAccion("<html><center>Eliminar<br>Item (F7)</center></html>", 670, 80, Color.RED);
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        add(btnEliminar);

        // ---------------------------------------------------------
        // 3. ZONA INFERIOR (Totales y Cobrar)
        // ---------------------------------------------------------
        JLabel lblTextoTotal = new JLabel("TOTAL A PAGAR:");
        lblTextoTotal.setFont(new Font("Tahoma", Font.BOLD, 20));
        lblTextoTotal.setBounds(400, 420, 180, 30);
        add(lblTextoTotal);

        lblTotalPagar = new JLabel("$ 0.00");
        lblTotalPagar.setFont(new Font("Tahoma", Font.BOLD, 26));
        lblTotalPagar.setForeground(new Color(0, 128, 0)); // Verde
        lblTotalPagar.setBounds(600, 420, 180, 30);
        add(lblTotalPagar);

        JButton btnCobrar = new JButton("COBRAR (F12)");
        btnCobrar.setBackground(new Color(50, 205, 50));
        btnCobrar.setForeground(Color.WHITE);
        btnCobrar.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnCobrar.setBounds(20, 420, 200, 50);
        btnCobrar.addActionListener(e -> iniciarProcesoCobro());
        add(btnCobrar);

        // ---------------------------------------------------------
        // 4. CONFIGURACIÓN DE TECLAS (KeyBindings)
        // ---------------------------------------------------------
        setupAtajo(KeyEvent.VK_F5, "F5", e -> abrirBusquedaPorNombre());
        setupAtajo(KeyEvent.VK_F6, "F6", e -> abrirConsultaPrecio());
        setupAtajo(KeyEvent.VK_F7, "F7", e -> eliminarProductoSeleccionado());
        setupAtajo(KeyEvent.VK_F12, "F12", e -> iniciarProcesoCobro());
    }

    // --- MÉTODOS VISUALES Y DELEGACIÓN ---

    private void agregarProductoPorInput() {
        try {
            String entrada = txtBusquedaProducto.getText().trim();
            // Delegamos al controlador
            String mensaje = controlador.agregarPorInput(entrada);
            
            actualizarTabla();
            txtBusquedaProducto.setText("");
            System.out.println(mensaje); // Log consola
        } catch (Exception e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            txtBusquedaProducto.selectAll();
        }
    }

    private void abrirBusquedaPorNombre() {
        String texto = JOptionPane.showInputDialog(this, "Nombre del producto:", "Buscar (F5)", JOptionPane.QUESTION_MESSAGE);
        if (texto == null || texto.trim().isEmpty()) return;

        List<Producto> resultados = controlador.buscarPorNombre(texto);
        
        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No encontrado.");
            return;
        }
        
        Object[] opciones = resultados.toArray();
        Producto seleccionado = (Producto) JOptionPane.showInputDialog(this, "Seleccione:", "Resultados", 
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccionado != null) {
            try {
                // Truco: Simulamos que escribió el código exacto
                controlador.agregarPorInput(seleccionado.getCodigoBarra());
                actualizarTabla();
                txtBusquedaProducto.requestFocus();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void abrirConsultaPrecio() {
        String codigo = JOptionPane.showInputDialog(this, "Código de Barras:", "Consultar Precio (F6)", JOptionPane.QUESTION_MESSAGE);
        if (codigo == null || codigo.trim().isEmpty()) return;

        Producto p = controlador.buscarPorCodigo(codigo);
        if (p != null) {
            String html = "<html><center><h2>" + p.getDescripcion() + "</h2>" +
                          "<h1 style='color:green'>$ " + p.calcularPrecioFinal() + "</h1>" +
                          "<p>Stock: " + p.getCantidadStock() + "</p></center></html>";
            JOptionPane.showMessageDialog(this, html, "Precio", JOptionPane.INFORMATION_MESSAGE);
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Producto inexistente.");
        }
        txtBusquedaProducto.requestFocus();
    }

    private void eliminarProductoSeleccionado() {
        int fila = tableDetalle.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem de la tabla.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Borrar ítem?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controlador.eliminarItem(fila);
            actualizarTabla();
            txtBusquedaProducto.requestFocus();
        }
    }

    private void iniciarProcesoCobro() {
        Venta v = controlador.getVentaActual();
        if (v.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos.");
            return;
        }

        String[] opciones = {"Efectivo", "Débito", "Crédito", "Transferencia"};
        int seleccion = JOptionPane.showOptionDialog(this, "Medio de Pago:", "Total: $" + v.getTotal(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion == -1) return;

        boolean pagado = false;
        if (seleccion == 0) { // Efectivo
            pagado = procesarPagoEfectivo();
        } else {
            pagado = true; // Tarjetas asumimos OK
        }

        if (pagado) {
            controlador.finalizarVenta();
            JOptionPane.showMessageDialog(this, "Venta Exitosa!");
            
            controlador.nuevaVenta(); // Reseteamos
            actualizarTabla();
            txtBusquedaProducto.requestFocus();
        }
    }

    private boolean procesarPagoEfectivo() {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "Total: $" + controlador.getVentaActual().getTotal() + "\nCon cuánto paga?");
            if (input == null) return false;
            try {
                BigDecimal pago = new BigDecimal(input);
                BigDecimal vuelto = controlador.calcularVuelto(pago,controlador.getVentaActual().getTotal());
                JOptionPane.showMessageDialog(this, "<html><h1>Vuelto: <span style='color:blue'>$ " + vuelto + "</span></h1></html>");
                return true;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Monto insuficiente.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Número inválido.");
            }
        }
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        Venta v = controlador.getVentaActual();
        for (DetalleVenta d : v.getItems()) {
            modeloTabla.addRow(new Object[] {
                d.getProducto().getCodigoBarra(),
                d.getProducto().getDescripcion(),
                "$" + d.getProducto().calcularPrecioFinal(),
                d.getCantidad(),
                "$" + d.calcularSubtotal()
            });
        }
        lblTotalPagar.setText("$ " + v.getTotal());
    }

    // --- UTILIDADES ---
    private JButton crearteBotonAccion(String texto, int x, int y, Color color) {
        JButton btn = new JButton(texto);
        btn.setBounds(x, y, 100, 50);
        btn.setForeground(color);
        btn.setFont(new Font("Tahoma", Font.BOLD, 10));
        return btn;
    }

    private void setupAtajo(int keyEvent, String nombre, ActionListener accion) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, nombre);
        getActionMap().put(nombre, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { accion.actionPerformed(e); }
        });
    }
}