package interfaz;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer; // Import necesario para centrar
import javax.swing.JLabel; // Import necesario para centrar

import modelo.Empresa;
import modelo.Producto;
import modelo.DetalleCarga; // Usamos la clase del modelo
import controlador.ControladorCargaStock;

public class PanelCargaStock extends PanelOperacionBase {

    private ControladorCargaStock controlador;

    public PanelCargaStock(Empresa empresa) {
        super();
        this.controlador = new ControladorCargaStock(empresa);
        
        // --- AJUSTE VISUAL DE COLUMNAS ---
        // Indices: 0:Cod, 1:Desc, 2:FACTOR, 3:Costo, 4:Venta, 5:Modo, 6:Cant, 7:Stock
        
        tableDetalle.getColumnModel().getColumn(0).setPreferredWidth(80);  // Cód
        tableDetalle.getColumnModel().getColumn(1).setPreferredWidth(200); // Desc
        tableDetalle.getColumnModel().getColumn(2).setPreferredWidth(50);  // Factor (Chico)
        tableDetalle.getColumnModel().getColumn(6).setPreferredWidth(60);  // Cantidad
        
        // Centrar la columna de Factor para que se vea prolijo
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableDetalle.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        enfocarBuscador();
    }

    @Override
    protected String getTituloOperacion() {
        return "REPOSICIÓN DE STOCK";
    }

    @Override
    protected String[] getColumnasTabla() {
        // --- CAMBIO 1: AGREGAMOS "Factor" ---
        return new String[] { 
            "Cód.", 
            "Descripción",
            "Factor",        // [2] NUEVO
            "Costo Unit.",   // [3]
            "Precio Venta",  // [4]
            "Modo",          // [5]
            "Cant. Sumar",   // [6] (Editable)
            "Stock Final"    // [7]
        };
    }

    @Override
    protected boolean isColumnaEditable(int col) {
        // --- CAMBIO 2: EL ÍNDICE EDITABLE AHORA ES 6 ---
        // Antes era 5, pero al meter "Factor" se corrió todo un lugar.
        return col == 6; 
    }

    @Override
    protected void onEnterBuscador() {
        String entrada = txtBusqueda.getText().trim();
        if (entrada.isEmpty()) return;

        try {
            // Intentamos agregar
            controlador.agregarItem(entrada, this.modoBulto);
            
            // Si funciona...
            actualizarIndicador(entrada);
            actualizarTabla();
            txtBusqueda.setText("");
            
        } catch (Exception e) {
            
            // SI NO EXISTE EL PRODUCTO
            if (e.getMessage().contains("no encontrado")) {
                
                String codigoLimpio = entrada.contains("*") ? entrada.split("\\*")[1] : entrada;
                
                int resp = JOptionPane.showConfirmDialog(this, 
                        "El producto '" + codigoLimpio + "' no existe.\n¿Crearlo ahora sin perder la carga actual?", 
                        "Nuevo Producto", JOptionPane.YES_NO_OPTION);

                if (resp == JOptionPane.YES_OPTION) {
                    abrirDialogoAltaRapida(codigoLimpio);
                } else {
                    txtBusqueda.selectAll();
                }
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                txtBusqueda.selectAll();
            }
        }
        enfocarBuscador();
    }

    @Override
    protected void confirmarOperacion() {
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

    @Override
    protected void eliminarItemSeleccionado() {
        int fila = tableDetalle.getSelectedRow();
        if (fila != -1) {
            controlador.eliminarItem(fila);
            actualizarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem de la tabla para borrar.");
        }
        enfocarBuscador();
    }

    @Override
    protected void abrirBusquedaAvanzada() {
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

    @Override
    protected void abrirConsultaPrecio() {
        String codigo = "";
        
        if (!txtBusqueda.getText().trim().isEmpty()) {
            String entrada = txtBusqueda.getText().trim();
            codigo = entrada.contains("*") ? entrada.split("\\*")[1] : entrada;
        } else if (tableDetalle.getSelectedRow() != -1) {
            codigo = tableDetalle.getValueAt(tableDetalle.getSelectedRow(), 0).toString();
        } else {
            codigo = JOptionPane.showInputDialog(this, "Ingrese Código para MODIFICAR PRECIO (F6):");
        }

        if (codigo == null || codigo.trim().isEmpty()) {
            enfocarBuscador();
            return;
        }

        Producto p = controlador.buscarProducto(codigo);

        if (p != null) {
            javax.swing.JFrame parentFrame = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
            DialogoModificarPrecio dialog = new DialogoModificarPrecio(parentFrame, controlador.getEmpresa(), p);
            dialog.setVisible(true);
            actualizarTabla();
            enfocarBuscador();
        } else {
            JOptionPane.showMessageDialog(this, "Producto no encontrado.");
            enfocarBuscador();
        }
    }

    @Override
    protected void onTablaEditada(int fila, int col) {
        // --- CAMBIO 3: VERIFICAMOS COLUMNA 6 ---
        if (col == 6 && fila >= 0) {
            try {
                Object valor = modeloTabla.getValueAt(fila, col);
                int nuevaCant = Integer.parseInt(valor.toString());
                controlador.modificarCantidad(fila, nuevaCant);
                SwingUtilities.invokeLater(this::actualizarTabla);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Ingrese solo números enteros.");
                actualizarTabla();
            } catch (Exception e) { }
        }
        enfocarBuscador();
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        List<DetalleCarga> lista = controlador.getListaItems();
        
        for (DetalleCarga item : lista) {
            int aSumar = item.getUnidadesReales();
           
            
            modeloTabla.addRow(new Object[] {
                item.getCodigoLeido(),
                item.getProducto().getDescripcion(),
                item.getProducto().getFactor(), // --- CAMBIO 4: MOSTRAMOS EL FACTOR ---
                "$" + item.getProducto().getPrecioCosto(),
                "$" + item.getProducto().calcularPrecioFinal(),
                item.isEsBulto() ? "BULTO" : "UNIDAD", // Ya mostramos factor en su propia columna
                item.getCantidad(),
                item.getProducto().getCantidadStock() + aSumar 
            });
        }
        
        lblTotalInfo.setText("Ítems a cargar: " + lista.size());
    }
    
    private void actualizarIndicador(String entrada) {
        if (entrada.contains("*")) {
            try {
                lblInfoCantidad.setText(entrada.split("\\*")[0] + "*");
            } catch (Exception e) {
                lblInfoCantidad.setText("1*");
            }
        } else {
            lblInfoCantidad.setText("1*");
        }
    }

    // Método para abrir el alta rápida (sin cambios lógicos, solo lo mantengo aquí)
    private void abrirDialogoAltaRapida(String codigo) {
        javax.swing.JFrame parent = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
        DialogoAltaProducto dialog = new DialogoAltaProducto(parent, controlador.getEmpresa(), codigo);
        dialog.setVisible(true);
        
        if (dialog.isGuardadoExitoso()) {
            try {
                int stockInicialDelDialogo = dialog.getStockIngresado();
                controlador.agregarItemConCantidad(codigo, stockInicialDelDialogo, this.modoBulto);
                actualizarTabla();
                txtBusqueda.setText("");
                JOptionPane.showMessageDialog(this, "¡Producto creado y agregado!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        enfocarBuscador();
    }
}