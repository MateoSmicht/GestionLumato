package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import modelo.CalculadoraCostos;
import modelo.DetalleCarga;
import modelo.Empresa;
import modelo.Producto;
import controlador.ControladorStock;

public class DialogoModificarPrecio extends JDialog {

    private static final long serialVersionUID = 1L;
    private Producto producto;
    private DetalleCarga detalleItem;
    private Runnable onActualizarTabla;
    
    // 1. Guardamos el controlador para usar sus fórmulas
    private ControladorStock controlador; 
    
    private PanelPrecios panelPrecios; 
    private JLabel lblProyeccionPPP;

    public DialogoModificarPrecio(JFrame parent, Empresa empresa,ControladorStock cs, Producto producto, 
                                  DetalleCarga detalleItem, Runnable onActualizarTabla) {
        super(parent, "Modificar Precio - " + producto.getDescripcion(), true);
        this.producto = producto;
        this.detalleItem = detalleItem;
        this.onActualizarTabla = onActualizarTabla;
        
        // Inicializamos el controlador
        this.controlador = cs;

        setSize(500, 480);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // ... (HEADER Y LABELS IGUAL QUE ANTES) ...
        JPanel panelHeader = new JPanel(null);
        panelHeader.setBackground(new Color(44, 62, 80));
        panelHeader.setBounds(0, 0, 500, 60);
        add(panelHeader);

        JLabel lblTitulo = new JLabel(detalleItem != null ? "PRECIO ENTRADA (LOTE)" : "ACTUALIZAR PRECIO MASTER");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(20, 15, 350, 30);
        panelHeader.add(lblTitulo);

        JLabel lblInfo = new JLabel("Producto: " + producto.getDescripcion());
        lblInfo.setBounds(20, 75, 450, 20);
        add(lblInfo);

        lblProyeccionPPP = new JLabel("PPP Proyectado: $ -");
        lblProyeccionPPP.setBounds(20, 95, 450, 20);
        lblProyeccionPPP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProyeccionPPP.setForeground(new Color(41, 128, 185));
        add(lblProyeccionPPP);

        // --- PANEL PRECIOS ---
        // Pasamos el mismo controlador que instanciamos arriba
        panelPrecios = new PanelPrecios(this.controlador); 
        panelPrecios.setBounds(20, 130, 460, 180); 
        
        BigDecimal costoIni, gananciaIni, ivaIni;
        
        if (detalleItem != null) {
            costoIni = detalleItem.getCostoNuevo();
            gananciaIni = producto.getPorcentajeGanancia();
            ivaIni = producto.getAlicuotaIVA();
        } else {
            costoIni = producto.getPrecioCosto();
            gananciaIni = producto.getPorcentajeGanancia();
            ivaIni = producto.getAlicuotaIVA();
        }
        
        panelPrecios.setValores(costoIni, gananciaIni, ivaIni);

        if (detalleItem != null && detalleItem.getPrecioVenta() != null) {
            // panelPrecios.setPrecioFinalManual(detalleItem.getPrecioVenta()); 
        }

        panelPrecios.agregarEscuchadorCambios(() -> recalcularPPPEnVivo());
        add(panelPrecios);

        // ... (BOTONES IGUAL QUE ANTES) ...
        JButton btnGuardar = new JButton("CONFIRMAR CAMBIOS (F12)");
        btnGuardar.setBounds(100, 330, 300, 50);
        btnGuardar.setBackground(new Color(39, 174, 96));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardarCambios());
        add(btnGuardar);
        
        panelPrecios.setOnEnterAlFinal(e -> guardarCambios());
        getRootPane().registerKeyboardAction(e -> guardarCambios(), 
            KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(() -> {
            panelPrecios.darFocoInicial();
            recalcularPPPEnVivo();
        });
    }

    private void recalcularPPPEnVivo() {
        if (detalleItem == null) {
            lblProyeccionPPP.setText("");
            return;
        }
        try {
            // Aquí si parseamos manual porque es una proyección visual rápida
            String costoStr = panelPrecios.getCosto();
            if (costoStr.isEmpty()) return;
            BigDecimal costoEntrante = new BigDecimal(costoStr.replace(",", "."));
            
            BigDecimal nuevoPPP = CalculadoraCostos.calcularNuevoPPP(
                new BigDecimal(Math.max(0, producto.getCantidadStock())), 
                producto.getPpp(), 
                new BigDecimal(detalleItem.getUnidadesReales()), 
                costoEntrante
            );
            lblProyeccionPPP.setText("PPP Proyectado: $ " + nuevoPPP.toString());
        } catch (Exception e) {
            lblProyeccionPPP.setText("PPP Proyectado: ...");
        }
    }

    private void guardarCambios() {
        try {
            System.out.println("--- INICIANDO GUARDADO ---");
            
            // 1. OBTENER DATOS
            String strCosto = panelPrecios.getCosto();
            String strGanancia = panelPrecios.getGanancia();
            String strIVA = panelPrecios.getIVA();

            // Validación
            if (strCosto.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El costo es obligatorio.");
                return;
            }

            // 2. CONVERTIR
            BigDecimal nuevoCosto = new BigDecimal(strCosto.replace(",", "."));
            BigDecimal nuevoPrecioVenta = controlador.calcularPrecioFinal(strCosto, strGanancia, strIVA);
            
           
            if (detalleItem != null) {      
                // GUARDAMOS COSTO
                detalleItem.setCostoNuevo(nuevoCosto);
               
                // GUARDAMOS VENTA
                detalleItem.setPrecioVenta(nuevoPrecioVenta);
               

                // REFRESCA TABLA
                if (onActualizarTabla != null) {
                    onActualizarTabla.run();
                }
                
                dispose();
            } else {
                // ... resto del código master
                dispose();
            }

        } catch (Exception e) {
            e.printStackTrace(); // Esto mostrará el error rojo si explota algo oculto
            JOptionPane.showMessageDialog(this, "Error crítico: " + e.getMessage());
        }
    }
}