package interfaz;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;

import modelo.Empresa;
import modelo.Producto;
import modelo.DetalleCarga;
import controlador.ControladorCargaStock;
import controlador.ControladorCategoria;
import controlador.ControladorStock;
import interfaz.dialogos.DialogoAltaProducto;
import interfaz.dialogos.DialogoModificarPrecio;

public class PanelCargaStock extends JPanel {

    private static final long serialVersionUID = 1L;

    private ControladorCargaStock controlador;
    private ControladorStock controladorStock;
    private ControladorCategoria controladorCategoria;
    private boolean modoBulto = false;

    // Componentes Visuales
    private JTextField txtBusqueda;
    private JTable tableDetalle;
    private DefaultTableModel modeloTabla;
    private JLabel lblInfoCantidad;
    private JLabel lblTotalInfo; 

    public JButton btnVolver;
    private JButton btnModoBulto;

    // Colores
    private final Color COLOR_FONDO = new Color(245, 246, 250);
    private final Color COLOR_HEADER = new Color(44, 62, 80);
    private final Color COLOR_ACCENT = new Color(52, 152, 219);
    private final Color COLOR_VERDE = new Color(39, 174, 96);
    private final Color COLOR_ROJO = new Color(231, 76, 60);
    private final Color COLOR_NARANJA = new Color(230, 126, 34);

    public PanelCargaStock(Empresa empresa, ControladorStock cs, ControladorCategoria controladorCategoria) {
        this.controlador = new ControladorCargaStock(empresa, cs);
        this.controladorStock= cs;
        this.controladorCategoria= controladorCategoria;
       
        setLayout(null);
        setBackground(COLOR_FONDO);
        setBounds(0, 0, 1000, 680);

        // =========================================================
        // 1. CABECERA
        // =========================================================
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(null);
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBounds(0, 0, 1000, 80);
        add(panelHeader);

        JLabel lblTitulo = new JLabel("REPOSICIÓN DE STOCK (PPP):");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(30, 15, 200, 20);
        panelHeader.add(lblTitulo);

        txtBusqueda = new JTextField();
        txtBusqueda.setBounds(30, 35, 650, 35);
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtBusqueda.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtBusqueda.addActionListener(e -> onEnterBuscador());
        panelHeader.add(txtBusqueda);

        lblInfoCantidad = new JLabel("1*");
        lblInfoCantidad.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblInfoCantidad.setForeground(SystemColor.activeCaption);
        lblInfoCantidad.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfoCantidad.setBounds(690, 35, 60, 35);
        panelHeader.add(lblInfoCantidad);

        btnVolver = new JButton("Volver al Menú");
        btnVolver.setBounds(820, 35, 150, 35);
        estilizarBoton(btnVolver, new Color(149, 165, 166), Color.WHITE);
        panelHeader.add(btnVolver);

        // =========================================================
        // 2. BARRA DE HERRAMIENTAS
        // =========================================================
        int yBtns = 95;
        int wBtn = 160;
        int hBtn = 40;
        int gap = 20;
        int xStart = 30;

        JButton btnBuscar = new JButton("<html><center>BUSCAR (F5)<br><font size=2>Por Nombre</font></center></html>");
        btnBuscar.setBounds(xStart, yBtns, wBtn, hBtn);
        estilizarBoton(btnBuscar, COLOR_ACCENT, Color.WHITE);
        add(btnBuscar);
        
        JButton btnPrecio = new JButton("<html><center>PRECIO (F6)<br><font size=2>Consultar</font></center></html>");
        btnPrecio.setBounds(xStart + wBtn + gap, yBtns, wBtn, hBtn);
        estilizarBoton(btnPrecio, COLOR_NARANJA, Color.WHITE);
        add(btnPrecio);
        
        btnModoBulto = new JButton("<html><center>Modo Bulto(F7)<br><font size=2>Desactivado</font></center></html>");
        btnModoBulto.setBounds(xStart + (wBtn + gap) * 2, yBtns, wBtn, hBtn);
        estilizarBoton(btnModoBulto, Color.LIGHT_GRAY, Color.BLACK);
        btnModoBulto.addActionListener(e -> toggleModoBulto());
        add(btnModoBulto);

        JButton btnEliminar = new JButton("<html><center>ELIMINAR (F8)<br><font size=2>Borrar Ítem</font></center></html>");
        btnEliminar.setBounds(xStart + (wBtn + gap) * 3, yBtns, wBtn, hBtn);
        estilizarBoton(btnEliminar, COLOR_ROJO, Color.WHITE);
        btnEliminar.addActionListener(e -> eliminarItemSeleccionado());
        add(btnEliminar);
        
        btnPrecio.addActionListener(e -> abrirConsultaPrecio());
        btnBuscar.addActionListener(e -> abrirBusquedaAvanzada());

        // =========================================================
        // 3. TABLA CENTRAL (CONFIGURADA PARA PPP)
        // =========================================================
        String[] columnas = { 
            "Cód.",           // 0
            "Descripción",    // 1
            "Costo Factura",  // 2 (EDITABLE)
            "PPP Est.",       // 3 (Visual Azul)
            "Precio Venta",   // 4 (EDITABLE - Verde)
            "Modo",           // 5
            "Cant. Sumar",    // 6 (EDITABLE)
            "Stock Final"     // 7
        };
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Permitimos editar Costo(2), Venta(4) y Cantidad(6)
                return column == 2 || column == 4 || column == 6; 
            }
        };

        modeloTabla.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int col = e.getColumn();
                if (fila >= 0) { 
                     // Solo reaccionamos a las columnas editables
                    if (col == 2 || col == 4 || col == 6) { 
                        onTablaEditada(fila, col);
                    }
                }
            }
        });
        
    

        tableDetalle = new JTable(modeloTabla);
        tableDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableDetalle.setRowHeight(30);
        tableDetalle.setShowGrid(true);
        tableDetalle.setGridColor(new Color(230, 230, 230));
        tableDetalle.setSelectionBackground(new Color(220, 230, 241));
        tableDetalle.setSelectionForeground(Color.BLACK);

        JTableHeader header = tableDetalle.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_HEADER);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        // Alineación y Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        tableDetalle.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        tableDetalle.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 
        
        // PPP (Azul)
        DefaultTableCellRenderer pppRenderer = new DefaultTableCellRenderer();
        pppRenderer.setHorizontalAlignment(JLabel.CENTER);
        pppRenderer.setForeground(new Color(41, 128, 185)); 
        pppRenderer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableDetalle.getColumnModel().getColumn(3).setCellRenderer(pppRenderer);

        // Precio Venta (Verde)
        DefaultTableCellRenderer ventaRenderer = new DefaultTableCellRenderer();
        ventaRenderer.setHorizontalAlignment(JLabel.CENTER);
        ventaRenderer.setForeground(new Color(39, 174, 96)); 
        ventaRenderer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableDetalle.getColumnModel().getColumn(4).setCellRenderer(ventaRenderer);

        tableDetalle.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tableDetalle.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tableDetalle.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // Anchos
        tableDetalle.getColumnModel().getColumn(0).setPreferredWidth(90);  // Cód
        tableDetalle.getColumnModel().getColumn(1).setPreferredWidth(280); // Desc
        tableDetalle.getColumnModel().getColumn(2).setPreferredWidth(80);  // Costo
        tableDetalle.getColumnModel().getColumn(3).setPreferredWidth(80);  // PPP
        tableDetalle.getColumnModel().getColumn(4).setPreferredWidth(90);  // Venta
        tableDetalle.getColumnModel().getColumn(5).setPreferredWidth(70);  // Modo
        tableDetalle.getColumnModel().getColumn(6).setPreferredWidth(70);  // Cant
        tableDetalle.getColumnModel().getColumn(7).setPreferredWidth(80);  // Stock

        JScrollPane scrollPane = new JScrollPane(tableDetalle);
        scrollPane.setBounds(30, 150, 935, 330); 
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scrollPane);
        
     // ... (Tu código anterior del JScrollPane) ...
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scrollPane);

        // =========================================================
        // NUEVA CONFIGURACIÓN DE NAVEGACIÓN (EXCEL STYLE)
        // =========================================================
        
        // A. Permitir que la tabla detecte "Enter" para EDITAR en vez de bajar de fila
        InputMap im = tableDetalle.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = tableDetalle.getActionMap();

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        // Reemplazamos la acción por defecto del Enter
        im.put(enterKey, "Action.editarCelda");
        am.put("Action.editarCelda", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tableDetalle.getSelectedRow();
                int col = tableDetalle.getSelectedColumn();
                
                // Si la celda es editable (Costo=2, Venta=4, Cantidad=6)
                if (col == 2 || col == 4 || col == 6) {
                    if (row != -1) {
                        tableDetalle.editCellAt(row, col);
                        Component editor = tableDetalle.getEditorComponent();
                        if (editor != null) {
                            editor.requestFocusInWindow();
                        }
                    }
                } else {
                    // Si no es editable, que el Enter funcione como Tab (mover a la derecha)
                    // o bajar si estamos al final.
                    if (row < tableDetalle.getRowCount() - 1) {
                         tableDetalle.setRowSelectionInterval(row + 1, row + 1);
                    }
                }
            }
        });
        
        // B. Propiedad para que guardar el cambio si el usuario hace clic afuera
        tableDetalle.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // C. Conectar Buscador -> Tabla (Flecha Abajo)
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (tableDetalle.getRowCount() > 0) {
                        tableDetalle.requestFocusInWindow();
                        // Seleccionar la primera fila y la columna de Costo (2) por defecto
                        tableDetalle.setRowSelectionInterval(0, 0);
                        tableDetalle.setColumnSelectionInterval(2, 2); 
                    }
                }
            }
        });

        // =========================================================
        // 4. FOOTER
        // =========================================================
        JPanel panelFooter = new JPanel();
        panelFooter.setLayout(null);
        panelFooter.setBackground(Color.WHITE);
        panelFooter.setBounds(0, 495, 1000, 90);
        panelFooter.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        add(panelFooter);

        lblTotalInfo = new JLabel("Ítems a cargar: 0");
        lblTotalInfo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalInfo.setForeground(Color.GRAY);
        lblTotalInfo.setBounds(40, 30, 300, 30);
        panelFooter.add(lblTotalInfo);

        JButton btnConfirmar = new JButton("<html><center><font size=5>CONFIRMAR</font><br><font size=3>(F12)</font></center></html>");
        btnConfirmar.setBounds(750, 10, 220, 70);
        btnConfirmar.setBackground(COLOR_VERDE);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBorderPainted(true);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(30, 132, 73)));
        btnConfirmar.addActionListener(e -> confirmarOperacion());
        panelFooter.add(btnConfirmar);

        // =========================================================
        // 5. ATAJOS
        // =========================================================
        setupAtajo(KeyEvent.VK_F1, "F1", e -> enfocarBuscador());
        setupAtajo(KeyEvent.VK_F5, "F5", e -> abrirBusquedaAvanzada());
        setupAtajo(KeyEvent.VK_F6, "F6", e -> abrirConsultaPrecio());
        setupAtajo(KeyEvent.VK_F7, "F7", e -> toggleModoBulto());
        setupAtajo(KeyEvent.VK_F8, "F8", e -> eliminarItemSeleccionado());
        setupAtajo(KeyEvent.VK_F12, "F12", e -> confirmarOperacion());

        enfocarBuscador();
        
        
        
    }

    // --- LOGICA DE NEGOCIO ---

    private void onEnterBuscador() {
        String entrada = txtBusqueda.getText().trim();
        if (entrada.isEmpty()) return;

        try {
            controlador.agregarItem(entrada, this.modoBulto);
            actualizarIndicador(entrada);
            actualizarTabla();
            txtBusqueda.setText("");
        } catch (Exception e) {
            // Manejo de "No encontrado" -> Alta Rápida
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("no encontrado")) {
                String codigoLimpio = entrada.contains("*") ? entrada.split("\\*")[1] : entrada;
                int resp = JOptionPane.showConfirmDialog(this, 
                        "El producto '" + codigoLimpio + "' no existe.\n¿Crearlo ahora?", 
                        "Nuevo Producto", JOptionPane.YES_NO_OPTION);

                if (resp == JOptionPane.YES_OPTION) {
                    abrirDialogoAltaRapida(codigoLimpio);
                } else {
                    txtBusqueda.selectAll();
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                txtBusqueda.selectAll();
            }
        }
        enfocarBuscador();
    }

    private void confirmarOperacion() {
        if (controlador.getListaItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La lista de carga está vacía.");
            return;
        }
        
        int resp = JOptionPane.showConfirmDialog(this, 
                "¿Confirmar el ingreso de mercadería al stock?", 
                "Confirmar Stock (F12)", JOptionPane.YES_NO_OPTION);
        
        if (resp == JOptionPane.YES_OPTION) {
            controlador.confirmarCargaMasiva();
            JOptionPane.showMessageDialog(this, "¡Stock actualizado exitosamente!");
            actualizarTabla(); 
            lblInfoCantidad.setText("1*");
            enfocarBuscador();
        }
    }

    // --- MÉTODO CLAVE: GESTIONAR EDICIÓN DE TABLA (COSTO / PRECIO / CANTIDAD) ---
    private void onTablaEditada(int fila, int col) {
        try {
            Object valor = modeloTabla.getValueAt(fila, col);
            // Limpieza básica para evitar error con el signo $ o comas
            String strValor = valor.toString().replace("$", "").replace(",", "."); 
            
            if (col == 6) { // COLUMNA CANTIDAD
                int nuevaCant = Integer.parseInt(strValor);
                controlador.modificarCantidad(fila, nuevaCant);
            } 
            else if (col == 2) { // COLUMNA COSTO (Nuevo)
                BigDecimal nuevoCosto = new BigDecimal(strValor);
                controlador.modificarCostoEntrada(fila, nuevoCosto);
            }
            else if (col == 4) { // COLUMNA PRECIO VENTA (Nuevo)
                BigDecimal nuevoPrecio = new BigDecimal(strValor);
                controlador.modificarPrecioVenta(fila, nuevoPrecio);
            }
            
         // Refrescamos la tabla y volvemos al buscador
            SwingUtilities.invokeLater(() -> {
                actualizarTabla();
                
                // --- CAMBIO AQUÍ: VOLVER AL BUSCADOR INMEDIATAMENTE ---
                enfocarBuscador(); 
            });
            
         // --- CAMBIO PARA NAVEGACIÓN FLUIDA ---
            // Intentamos volver a poner el foco en la tabla en la misma posición
            if (tableDetalle.getRowCount() > fila) {
                tableDetalle.requestFocusInWindow();
                tableDetalle.setRowSelectionInterval(fila, fila);
                // Si editó costo(2), pasamos a venta(4). Si editó venta, a cantidad(6).
                int nextCol = col;
                if (col == 2) nextCol = 4;
                else if (col == 4) nextCol = 6;
                
                tableDetalle.setColumnSelectionInterval(nextCol, nextCol);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Use números y punto para decimales.");
            SwingUtilities.invokeLater(() -> {
                actualizarTabla();
                enfocarBuscador(); // En caso de error, también volvemos
            });
            
        }
        
    }

    // --- MÉTODO CLAVE: MOSTRAR DATOS Y PROYECCIONES ---
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        List<DetalleCarga> lista = controlador.getListaItems();
        
        System.out.println("--- REFRESCANDO TABLA (" + lista.size() + " ítems) ---");

        for (DetalleCarga item : lista) {
            // 1. Proyección PPP
            BigDecimal valorPPP = controlador.calcularProyeccionPPP(item);
            String pppTexto = "$" + valorPPP.toString();
            
            // --- AQUÍ ESTÁ LA CLAVE ---
            // Imprimimos qué tiene el objeto realmente en la memoria
            System.out.println("Item: " + item.getProducto().getDescripcion());
            System.out.println("   > Costo en Producto (Viejo): " + item.getProducto().getPrecioCosto());
            System.out.println("   > Costo en Carga (Nuevo):    " + item.getCostoNuevo());
            
            // SI ESTA LÍNEA ESTÁ MAL, LA TABLA SE VERÁ MAL
            // Debes usar item.getCostoNuevo(), NO getProducto().getPrecioCosto()
            String costoTexto = item.getCostoNuevo().toString(); 
            
            String ventaTexto = item.getPrecioVenta().toString();

            modeloTabla.addRow(new Object[] {
                item.getCodigoLeido(),
                item.getProducto().getDescripcion(),
                costoTexto,   // <--- Esta variable es la que manda
                pppTexto,
                ventaTexto,
                item.isEsBulto() ? "BULTO" : "UNIDAD",
                item.getCantidad(),
                item.getProducto().getCantidadStock() + item.getUnidadesReales() 
            });
        }
        lblTotalInfo.setText("Ítems a cargar: " + lista.size());
    }
    // --- RESTO DE MÉTODOS DE BÚSQUEDA Y DIÁLOGOS ---

    private void abrirBusquedaAvanzada() {
        String texto = JOptionPane.showInputDialog(this, "Nombre del producto:", "Buscar Stock (F5)", JOptionPane.QUESTION_MESSAGE);
        if (texto == null || texto.trim().isEmpty()) return;

        List<Producto> resultados = controlador.buscarPorNombre(texto);
        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No encontrado.");
            enfocarBuscador();
            return;
        }
        
        Object[] opciones = resultados.toArray();
        Producto seleccionado = (Producto) JOptionPane.showInputDialog(this, "Seleccione:", "Resultados", 
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccionado != null) {
            try {
                controlador.agregarItem(seleccionado.getCodigoBarra(), this.modoBulto);
                actualizarTabla();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
        enfocarBuscador();
    }

    private void abrirConsultaPrecio() {
        // 1. SI ESTÁN EDITANDO UNA CELDA, LA CERRAMOS A LA FUERZA
        if (tableDetalle.isEditing()) {
            tableDetalle.getCellEditor().stopCellEditing();
        }

        // --- VALIDACIÓN NUEVA (LO QUE PEDISTE) ---
        
        // Caso A: No hay nada cargado en la tabla
        if (controlador.getListaItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay productos en la lista.\nPor favor, cargue un producto primero.", 
                "Tabla Vacía", JOptionPane.WARNING_MESSAGE);
            enfocarBuscador();
            return;
        }

        // Caso B: Hay productos, pero no seleccionó ninguno y el buscador está vacío
        // (Solo validamos esto si NO escribió un código manual en el buscador)
        if (tableDetalle.getSelectedRow() == -1 && txtBusqueda.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione un producto de la tabla para modificar su precio.", 
                "Sin Selección", JOptionPane.WARNING_MESSAGE);
            tableDetalle.requestFocus();
            return;
        }
        // -----------------------------------------

        String codigo = "";
        DetalleCarga detalleSeleccionado = null; 

        // 1. PRIORIDAD ABSOLUTA: LA TABLA SELECCIONADA
        if (tableDetalle.getSelectedRow() != -1) {
            int viewRow = tableDetalle.getSelectedRow();
            int modelRow = tableDetalle.convertRowIndexToModel(viewRow);
            
            if (modelRow >= 0 && modelRow < controlador.getListaItems().size()) {
                detalleSeleccionado = controlador.getListaItems().get(modelRow);
                codigo = detalleSeleccionado.getProducto().getCodigoBarra();
            }
        }

        // 2. PRIORIDAD SECUNDARIA: EL BUSCADOR
        if (detalleSeleccionado == null && !txtBusqueda.getText().trim().isEmpty()) {
            String entrada = txtBusqueda.getText().trim();
            codigo = entrada.contains("*") ? entrada.split("\\*")[1] : entrada;
        } 
        
        // Validación final de seguridad
        if (codigo == null || codigo.trim().isEmpty()) { enfocarBuscador(); return; }

        // ... (Resto del código de búsqueda y apertura del diálogo sigue igual) ...
        Producto p = controlador.buscarProducto(codigo);
        
        if (p != null) {
            if (detalleSeleccionado == null) {
                for (DetalleCarga d : controlador.getListaItems()) {
                    if (d.getProducto().getCodigoBarra().equals(p.getCodigoBarra())) {
                        detalleSeleccionado = d;
                        int indiceLista = controlador.getListaItems().indexOf(d);
                        int indiceVista = tableDetalle.convertRowIndexToView(indiceLista);
                        tableDetalle.setRowSelectionInterval(indiceVista, indiceVista);
                        break;
                    }
                }
            }

            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            DialogoModificarPrecio dialog = new DialogoModificarPrecio(
                parent, 
                controlador.getEmpresa(), 
                this.controladorStock,
                p, 
                detalleSeleccionado,
                () -> actualizarTabla() 
            );
            dialog.setVisible(true);
            enfocarBuscador();
        } else {
            JOptionPane.showMessageDialog(this, "Producto no encontrado.");
            enfocarBuscador();
        }
    }
    private void abrirDialogoAltaRapida(String codigo) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        DialogoAltaProducto dialog = new DialogoAltaProducto(parent, codigo, this.controladorStock, this.controladorCategoria);
        dialog.setVisible(true);
        
        if (dialog.isGuardadoExitoso()) {
            try {
                int stockInicial = dialog.getStockIngresado();
                controlador.agregarItemConCantidad(codigo, stockInicial, this.modoBulto);
                actualizarTabla();
                txtBusqueda.setText("");
                JOptionPane.showMessageDialog(this, "¡Producto creado y agregado!");
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        enfocarBuscador();
    }

    private void eliminarItemSeleccionado() {
        int fila = tableDetalle.getSelectedRow();
        if (fila != -1) {
            controlador.eliminarItem(fila);
            actualizarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem para borrar.");
        }
        enfocarBuscador();
    }

    private void toggleModoBulto() {
        this.modoBulto = !this.modoBulto;
        if (this.modoBulto) {
            estilizarBoton(btnModoBulto, new Color(46, 204, 113), Color.WHITE);
            btnModoBulto.setText("<html><center>Modo Bulto(F7)<br>ACTIVO</center></html>");
        } else {
            estilizarBoton(btnModoBulto, Color.LIGHT_GRAY, Color.BLACK);
            btnModoBulto.setText("<html><center>Modo Bulto(F7)<br>Desactivado</center></html>");
        }
        enfocarBuscador();
    }

    private void actualizarIndicador(String entrada) {
        if (entrada.contains("*")) {
            try { lblInfoCantidad.setText(entrada.split("\\*")[0] + "*"); } 
            catch (Exception e) { lblInfoCantidad.setText("1*"); }
        } else { lblInfoCantidad.setText("1*"); }
    }

    private void enfocarBuscador() {
        txtBusqueda.requestFocusInWindow();
        txtBusqueda.selectAll();
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupAtajo(int keyEvent, String nombre, ActionListener accion) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, nombre);
        getActionMap().put(nombre, new AbstractAction() { public void actionPerformed(ActionEvent e) { accion.actionPerformed(e); } });
    }
}