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

    // --- ESTADO ---
    private boolean modoBulto = false; // Por defecto carga unidades

    // --- COMPONENTES VISUALES ---
    private JTextField txtBusquedaProducto;
    private JTable tableDetalle;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalPagar;
    private JLabel lblInfoCantidad;
    
    // Botones con acceso público o de clase para cambiarles propiedades
    public JButton btnVolver;
    private JButton btnModoBulto;

    /**
     * Constructor
     */
    public PanelVenta(Empresa empresa, Usuario vendedor) {
        // Inicializamos el controlador
        this.controlador = new ControladorVenta(empresa, vendedor);

        setLayout(null);
        setBounds(0, 0, 784, 500);

        // ---------------------------------------------------------
        // 1. ZONA SUPERIOR (Buscador y Botones)
        // ---------------------------------------------------------
        JLabel lblBuscar = new JLabel("INGRESE CÓDIGO:");
        lblBuscar.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblBuscar.setBounds(20, 27, 150, 25);
        add(lblBuscar);

        txtBusquedaProducto = new JTextField();
        txtBusquedaProducto.setBounds(160, 20, 420, 40);
        txtBusquedaProducto.setFont(new Font("Tahoma", Font.PLAIN, 14));
        add(txtBusquedaProducto);

        // Al presionar Enter, procesamos el producto
        txtBusquedaProducto.addActionListener(e -> agregarProductoPorInput());

        // Botón Volver (Arriba a la derecha)
        btnVolver = new JButton("Volver al Menú");
        btnVolver.setBounds(652, 26, 111, 30);
        add(btnVolver);
        
     // --- NUEVO: INDICADOR DE CANTIDAD (1* / 2*) ---
        lblInfoCantidad = new JLabel("1*"); // Arranca en 1*
        lblInfoCantidad.setFont(new Font("Arial", Font.BOLD, 22)); // Fuente Grande
        lblInfoCantidad.setForeground(new Color(0, 100, 0)); // Verde oscuro
        lblInfoCantidad.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfoCantidad.setBounds(572, 20, 70, 40); // Entre la barra y el botón volver
        add(lblInfoCantidad);

        // --- BOTÓN F5 (BUSCAR NOMBRE) ---
        JButton btnBuscar = new JButton("<html><center>Buscar<br>Nombre (F5)</center></html>");
        btnBuscar.setBounds(13, 89, 100, 50);
        btnBuscar.setForeground(new Color(0, 0, 128)); // Azul
        btnBuscar.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnBuscar.addActionListener(e -> abrirBusquedaPorNombre());
        add(btnBuscar);

        // --- BOTÓN F6 (CONSULTAR PRECIO) ---
        JButton btnPrecio = new JButton("<html><center>Consultar<br>Precio (F6)</center></html>");
        btnPrecio.setBounds(13, 141, 100, 50);
        btnPrecio.setForeground(new Color(255, 140, 0)); // Naranja
        btnPrecio.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnPrecio.addActionListener(e -> abrirConsultaPrecio());
        add(btnPrecio);
        
        // --- BOTÓN F7 (MODO BULTO) ---
        btnModoBulto = new JButton("<html><center>Modo Bulto<br>(F7)</center></html>");
        btnModoBulto.setBounds(13, 193, 100, 50);
        btnModoBulto.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnModoBulto.setBackground(Color.LIGHT_GRAY); // Inicia desactivado
        btnModoBulto.addActionListener(e -> toggleModoBulto());
        add(btnModoBulto);
        
        // --- BOTÓN F8 (ELIMINAR ÍTEM) ---
        JButton btnEliminar = new JButton("<html><center>Eliminar<br>Item (F8)</center></html>");
        btnEliminar.setBounds(13, 245, 100, 50);
        btnEliminar.setForeground(Color.RED);
        btnEliminar.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        add(btnEliminar);

        // ---------------------------------------------------------
        // 2. ZONA CENTRAL (Tabla)
        // ---------------------------------------------------------
        
        // Definimos las columnas incluyendo Unidad y Factor
        String[] columnas = { "Cód.", "Descripción", "Unidad", "Factor", "Precio Unit.", "Cant.", "Subtotal" };
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitimos editar la columna de Cantidad (índice 5)
                return column == 5; 
            }
        };
        
        // Listener para editar cantidad en la tabla
        modeloTabla.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();
                
                // Si cambiaron la columna Cantidad (5)
                if (columna == 5 && fila >= 0) {
                    try {
                        Object valor = modeloTabla.getValueAt(fila, columna);
                        int nuevaCant = Integer.parseInt(valor.toString());
                        controlador.modificarCantidadItem(fila, nuevaCant);
                        
                        // Refrescamos y devolvemos foco al buscador
                        SwingUtilities.invokeLater(() -> {
                            actualizarTabla();
                            enfocarBuscador();
                        });
                    } catch (Exception ex) { /* Ignorar error de parseo */ }
                }
            }
        });

        tableDetalle = new JTable(modeloTabla);
        tableDetalle.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tableDetalle.setRowHeight(25);
        
        // Ajuste de ancho de columnas (Opcional)
        tableDetalle.getColumnModel().getColumn(0).setPreferredWidth(80);  // Cód
        tableDetalle.getColumnModel().getColumn(1).setPreferredWidth(200); // Descripción
        tableDetalle.getColumnModel().getColumn(2).setPreferredWidth(50);  // Unidad
        tableDetalle.getColumnModel().getColumn(3).setPreferredWidth(40);  // Factor
        tableDetalle.getColumnModel().getColumn(5).setPreferredWidth(40);  // Cant
        
        JScrollPane scrollPane = new JScrollPane(tableDetalle);
        scrollPane.setBounds(123, 89, 640, 320);
        add(scrollPane);

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
        btnCobrar.setBounds(26, 412, 166, 50);
        btnCobrar.addActionListener(e -> iniciarProcesoCobro());
        add(btnCobrar);
        
       
        // ---------------------------------------------------------
        // 4. CONFIGURACIÓN DE TECLAS (KeyBindings)
        // ---------------------------------------------------------
        setupAtajo(KeyEvent.VK_F1, "F1", e -> enfocarBuscador());
        setupAtajo(KeyEvent.VK_F5, "F5", e -> abrirBusquedaPorNombre());
        setupAtajo(KeyEvent.VK_F6, "F6", e -> abrirConsultaPrecio());
        setupAtajo(KeyEvent.VK_F7, "F7", e -> toggleModoBulto());
        setupAtajo(KeyEvent.VK_F8, "F8", e -> eliminarProductoSeleccionado());
        setupAtajo(KeyEvent.VK_F12, "F12", e -> iniciarProcesoCobro());
        
        // Foco inicial
        enfocarBuscador();
        
        // Verificar sesión pendiente al final del constructor
        // (Movemos esto al final para evitar NullPointer en modeloTabla)
        verificarSesionPendiente();
    }

    // ========================================================================
    // MÉTODOS VISUALES Y DELEGACIÓN
    // ========================================================================

    private void enfocarBuscador() {
        txtBusquedaProducto.requestFocusInWindow();
        txtBusquedaProducto.selectAll();
    }
    
    private void toggleModoBulto() {
        this.modoBulto = !this.modoBulto; // Invertir estado
        
        if (this.modoBulto) {
            btnModoBulto.setBackground(Color.GREEN);
            btnModoBulto.setText("<html><center>Modo Bulto<br>(ACTIVO)</center></html>");
        } else {
            btnModoBulto.setBackground(Color.LIGHT_GRAY);
            btnModoBulto.setText("<html><center>Modo Bulto<br>(F7)</center></html>");
        }
        enfocarBuscador();
    }
    
    
  
    
    // Método auxiliar para el cartelito
    private void actualizarIndicadorCantidad(String entrada) {
        if (entrada.contains("*")) {
            // Si el usuario escribió "5*779...", extraemos el "5"
            try {
                String cantStr = entrada.split("\\*")[0];
                lblInfoCantidad.setText(cantStr + "*");
                lblInfoCantidad.setForeground(Color.BLUE); // Azul para resaltar mult.
            } catch (Exception e) {
                lblInfoCantidad.setText("1*");
            }
        } else {
            // Si fue una venta normal
            lblInfoCantidad.setText("1*");
            lblInfoCantidad.setForeground(new Color(0, 100, 0)); // Verde normal
        }
    }

    

    private void agregarProductoPorInput() {
        try {
            String entrada = txtBusquedaProducto.getText().trim();
            
            // --- CORREGIDO AQUÍ ---
            // Le pasamos el estado del botón F7 (modoBulto)
            String mensaje = controlador.agregarPorInput(entrada, this.modoBulto);
            actualizarIndicadorCantidad(entrada);
            actualizarTabla();
            
            // Truco visual: mostrar nombre y seleccionar
            java.util.List<DetalleVenta> items = controlador.getVentaActual().getItems();
            if (!items.isEmpty()) {
                DetalleVenta ultimo = items.get(items.size() - 1);
                txtBusquedaProducto.setText(ultimo.getProducto().getDescripcion());
                txtBusquedaProducto.selectAll();
            } else {
                txtBusquedaProducto.setText("");
            }
            
            System.out.println(mensaje); 
            
        } catch (Exception e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            txtBusquedaProducto.selectAll(); 
        }
    }

    private void abrirBusquedaPorNombre() {
        String texto = JOptionPane.showInputDialog(this, "Nombre del producto:", "Buscar (F5)", JOptionPane.QUESTION_MESSAGE);
        if (texto == null || texto.trim().isEmpty()) return;

        java.util.List<Producto> resultados = controlador.buscarPorNombre(texto);
        
        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No encontrado.");
            return;
        }
        
        Object[] opciones = resultados.toArray();
        Producto seleccionado = (Producto) JOptionPane.showInputDialog(this, "Seleccione:", "Resultados", 
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccionado != null) {
            try {
                // --- CORREGIDO AQUÍ ---
                // Al buscar por nombre, asumimos venta por unidad (false)
                controlador.agregarPorInput(seleccionado.getCodigoBarra(), false);
                
                actualizarTabla();
                
                txtBusquedaProducto.setText(seleccionado.getDescripcion());
                txtBusquedaProducto.selectAll();
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
                          "<p>Unidad: " + p.getNombreUnidad() + " (x" + p.getFactor() + ")</p>" +
                          "<p>Stock: " + p.getCantidadStock() + "</p></center></html>";
            JOptionPane.showMessageDialog(this, html, "Precio", JOptionPane.INFORMATION_MESSAGE);
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Producto inexistente.");
        }
        enfocarBuscador();
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
            
            txtBusquedaProducto.setText(""); 
            enfocarBuscador();
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
        if (seleccion == 0) { 
            pagado = procesarPagoEfectivo();
        } else {
            pagado = true; 
        }

        if (pagado) {
            controlador.finalizarVenta();
            JOptionPane.showMessageDialog(this, "Venta Exitosa!");
            
            controlador.nuevaVenta(); // Reseteamos
            actualizarTabla();
            txtBusquedaProducto.setText("");
            enfocarBuscador();
        }
    }

    private boolean procesarPagoEfectivo() {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "Total: $" + controlador.getVentaActual().getTotal() + "\nCon cuánto paga?");
            if (input == null) return false;
            try {
                BigDecimal pago = new BigDecimal(input);
                BigDecimal vuelto = controlador.calcularVuelto(pago);
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
                d.getNombreUnidadSnapshot(), // Muestra "UNI" o "CAJA"
                d.getFactorSnapshot(),       // Muestra 1 o 12
                "$" + d.getPrecioUnitarioSnapshot(),
                d.getCantidad(),
                "$" + d.calcularSubtotal()
            });
        }
        lblTotalPagar.setText("$ " + v.getTotal());
    }
    
    // --- PERSISTENCIA DE SESIÓN ---
    private void verificarSesionPendiente() {
        if (controlador.existeVentaPendiente()) {
            int resp = JOptionPane.showConfirmDialog(this, 
                    "Tiene una venta sin finalizar.\n¿Desea recuperarla?", 
                    "Venta Pendiente", JOptionPane.YES_NO_OPTION);
            
            if (resp == JOptionPane.YES_OPTION) {
                controlador.restaurarVentaPendiente();
                actualizarTabla();
            } else {
                controlador.descartarVentaPendiente();
            }
        }
    }
    
    public void guardarSalida() {
        controlador.guardarVentaEnEspera();
    }

    // --- UTILIDADES ---
    private void setupAtajo(int keyEvent, String nombre, ActionListener accion) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, nombre);
        getActionMap().put(nombre, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { accion.actionPerformed(e); }
        });
    }
}