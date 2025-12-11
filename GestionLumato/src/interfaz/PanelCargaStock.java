package interfaz;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.util.List;
import modelo.Empresa;
import modelo.Producto;
import controlador.ControladorCargaStock;
import controlador.ControladorCargaStock.ItemCarga;

public class PanelCargaStock extends PanelOperacionBase {

    private ControladorCargaStock controlador;

    public PanelCargaStock(Empresa empresa) {
        super();
        this.controlador = new ControladorCargaStock(empresa);
        
        // Ajustamos anchos de columnas para que entren los precios
        // Indices: 0:Cod, 1:Desc, 2:Costo, 3:Venta, 4:Modo, 5:Cant, 6:Stock
        tableDetalle.getColumnModel().getColumn(1).setPreferredWidth(200); // Desc
        tableDetalle.getColumnModel().getColumn(5).setPreferredWidth(50);  // Cant
        
        enfocarBuscador();
    }

    @Override
    protected String getTituloOperacion() {
        return "REPOSICIÓN DE STOCK";
    }

    @Override
    protected String[] getColumnasTabla() {
        // --- CAMBIO 1: AGREGAMOS COLUMNAS DE PRECIO ---
        return new String[] { 
            "Cód.", 
            "Descripción", 
            "Costo Unit.",   // Nuevo [2]
            "Precio Venta",  // Nuevo [3]
            "Modo",          // [4]
            "Cant. Sumar",   // [5] (Esta es la editable)
            "Stock Final"    // [6]
        };
    }

    @Override
    protected boolean isColumnaEditable(int col) {
        // --- CAMBIO 2: ACTUALIZAMOS ÍNDICE EDITABLE ---
        // Antes era 3, ahora al agregar 2 columnas nuevas, es la 5
        return col == 5; 
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

    private void abrirDialogoAltaRapida(String codigo) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Abrimos el diálogo
        DialogoAltaProducto dialog = new DialogoAltaProducto(parent, controlador.getEmpresa(), codigo);
        dialog.setVisible(true);
        
        // AL VOLVER:
        if (dialog.isGuardadoExitoso()) {
            try {
                // 1. Recuperamos la cantidad que el usuario escribió en la ventanita (ej: 100)
                int stockInicialDelDialogo = dialog.getStockIngresado();
                
                // 2. Agregamos el ítem a la tabla con ESA cantidad
                controlador.agregarItemConCantidad(codigo, stockInicialDelDialogo, this.modoBulto);
                
                actualizarTabla();
                txtBusqueda.setText("");
                
                JOptionPane.showMessageDialog(this, "¡Producto creado y agregado con cantidad: " + stockInicialDelDialogo + "!");
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        enfocarBuscador(); // Devolvemos el foco para seguir escaneando
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
        // Lógica F6: Buscar producto seleccionado o pedir código
        String codigo = "";
        
        if (!txtBusqueda.getText().trim().isEmpty()) {
            // Prioridad 1: Texto en barra
            String entrada = txtBusqueda.getText().trim();
            codigo = entrada.contains("*") ? entrada.split("\\*")[1] : entrada;
        } else if (tableDetalle.getSelectedRow() != -1) {
            // Prioridad 2: Selección en tabla (Columna 0 es código)
            codigo = tableDetalle.getValueAt(tableDetalle.getSelectedRow(), 0).toString();
        } else {
            // Prioridad 3: Pedir input
            codigo = JOptionPane.showInputDialog(this, "Ingrese Código para MODIFICAR PRECIO (F6):");
        }

        if (codigo == null || codigo.trim().isEmpty()) {
            enfocarBuscador();
            return;
        }

        Producto p = controlador.buscarProducto(codigo); // Asegurate que ControladorCargaStock tenga este método

        if (p != null) {
            javax.swing.JFrame parentFrame = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
            DialogoModificarPrecio dialog = new DialogoModificarPrecio(parentFrame, controlador.getEmpresa(), p);
            dialog.setVisible(true);
            
            // Al volver, refrescamos la tabla porque el precio pudo cambiar
            actualizarTabla();
            enfocarBuscador();
        } else {
            JOptionPane.showMessageDialog(this, "Producto no encontrado.");
            enfocarBuscador();
        }
    }

    @Override
    protected void onTablaEditada(int fila, int col) {
        // --- CAMBIO 3: ACTUALIZAMOS ÍNDICE ---
        // Si el usuario editó la celda de cantidad (Ahora es Columna 5)
        if (col == 5 && fila >= 0) {
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
        List<ItemCarga> lista = controlador.getListaItems();
        
        for (ItemCarga item : lista) {
            int stockActual = item.getProducto().getCantidadStock();
            int aSumar = item.getUnidadesReales();
            
            modeloTabla.addRow(new Object[] {
                item.getProducto().getCodigoBarra(),
                item.getProducto().getDescripcion(),
                // --- CAMBIO 4: MOSTRAMOS LOS PRECIOS ---
                "$" + item.getProducto().getPrecioCosto(),        // Costo
                "$" + item.getProducto().calcularPrecioFinal(),   // Venta
                
                item.isEsBulto() ? "BULTO (x" + item.getProducto().getFactor() + ")" : "UNIDAD",
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
}