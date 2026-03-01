package controlador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import modelo.Empresa;
import modelo.Producto;
import persistencia.RepositorioProducto;
import modelo.CalculadoraCostos;
import modelo.Categoria;
import modelo.DetalleCarga; // <--- Importamos la nueva clase

public class ControladorCargaStock {

    private List<DetalleCarga> listaItems;
    private ControladorStock controladorStock;
   
    // CAMBIO: Recibimos el ControladorStock ya fabricado desde afuera
    public ControladorCargaStock( ControladorStock cs) {
        this.listaItems = new ArrayList<>();
        
        // Guardamos la referencia al que ya existe. NO hacemos 'new'.
        this.controladorStock = cs; 
      
    }


	public Producto buscarProducto(String codigo) {
		return this.controladorStock.buscarProducto(codigo);
	}

	

	public void agregarItem(String entrada, boolean modoBulto) throws Exception {
		if (entrada.isEmpty())
			return;

		int cantidad = 1;
		String codigoLimpio = entrada; // Este será el codigoLeido

		// Si viene con asterisco (ej: "10*779123")
		if (entrada.contains("*")) {
			String[] partes = entrada.split("\\*");
			cantidad = Integer.parseInt(partes[0]);
			codigoLimpio = partes[1]; // Nos quedamos solo con el código
		}

		Producto p = this.controladorStock.buscarProducto(codigoLimpio);
		if (p == null)
			throw new Exception("Producto no encontrado: " + codigoLimpio);

		// --- CAMBIO AQUÍ: Pasamos codigoLimpio al constructor ---
		listaItems.add(new DetalleCarga(p, cantidad, modoBulto, codigoLimpio));
	}

	public void agregarItemConCantidad(String codigo, int cantidad, boolean modoBulto) throws Exception {
		Producto p = this.controladorStock.buscarProducto(codigo);
		if (p == null)
			throw new Exception("Error interno.");

		int cantFinal = (cantidad > 0) ? cantidad : 1;

		listaItems.add(new DetalleCarga(p, cantFinal, modoBulto, codigo));
	}

	public void eliminarItem(int index) {
		if (index >= 0 && index < listaItems.size()) {
			listaItems.remove(index);
		}
	}

	public void modificarCantidad(int index, int nuevaCant) {
		if (index >= 0 && index < listaItems.size() && nuevaCant > 0) {
			listaItems.get(index).setCantidad(nuevaCant);
		}
	}

	public List<DetalleCarga> getListaItems() {
		return listaItems;
	}

	public List<Producto> buscarPorNombre(String nombre) {
		return this.controladorStock.buscarPorNombre(nombre);
	}

	public void guardarProductoRapido(String codigo, String descripcion, String costoStr, String precioFinalStr)
			throws Exception {

		// 1. Validaciones
		if (codigo.isEmpty())
			throw new Exception("Falta el Código");
		if (descripcion.isEmpty())
			throw new Exception("Falta la Descripción");
		if (precioFinalStr.isEmpty())
			throw new Exception("Falta el Precio");

		// 2. Lógica de Negocio "Rápida" (Rellenar huecos)
		if (costoStr == null || costoStr.trim().isEmpty()) {
			costoStr = precioFinalStr; // Si no hay costo, asumimos ganancia 0
		}

		// 3. Reutilizamos la matemática del controlador principal
		String ganancia = controladorStock.calcularPorcentajeGanancia(costoStr, precioFinalStr);

		

		// 5. Delegamos el guardado final al Controlador Principal
		controladorStock.guardarProducto(codigo, descripcion, null, costoStr, ganancia, "21.0", // IVA default
				"UNI", // Unidad default
				"1", // Factor default
				"0" // Stock 0
		);
	}


private void actualizarProductoCompleto(Producto p, int cantidadNueva, BigDecimal nuevoCosto, BigDecimal nuevoPrecioVenta) {
        
        // A. CÁLCULO DE STOCK Y PPP
        BigDecimal stockActual = new BigDecimal(Math.max(0, p.getCantidadStock())); 
        BigDecimal pppViejo = p.getPpp(); 
        BigDecimal cantidadIngreso = new BigDecimal(cantidadNueva);

        BigDecimal nuevoPPP = CalculadoraCostos.calcularNuevoPPP(stockActual, pppViejo, cantidadIngreso, nuevoCosto);
        BigDecimal stockTotal = stockActual.add(cantidadIngreso);

        // B. GUARDAR DATOS DE STOCK Y COSTO
        p.setCantidadStock(stockTotal.intValue());
        p.setPpp(nuevoPPP);
        p.setPrecioCosto(nuevoCosto); 

        // C. CÁLCULO DE GANANCIA REAL
        BigDecimal alicuota = p.getAlicuotaIVA(); 
        if (alicuota == null) alicuota = BigDecimal.ZERO;
        
        BigDecimal porcentajeCorrecto = CalculadoraCostos.calcularPorcentajeGanancia(
            nuevoCosto, 
            nuevoPrecioVenta
        );
 
        p.setPorcentajeGanancia(porcentajeCorrecto);
}


    public void confirmarCargaMasiva() {
        // Recorremos toda la lista de productos que cargaste en la tabla
        for (DetalleCarga detalle : listaItems) {
            
            // 1. Buscamos el producto real en la "Base de Datos" (Memoria)
            Producto productoReal = controladorStock.buscarProducto(detalle.getProducto().getCodigoBarra());

            if (productoReal != null) {
                // 2. Extraemos los datos finales de la tabla
                int cantidadAingresar = detalle.getUnidadesReales();
                BigDecimal costoFactura = detalle.getCostoNuevo(); // El costo editado
                BigDecimal precioVentaElegido = detalle.getPrecioVenta(); // El precio venta editado

                // 3. LLAMAMOS AL MÉTODO QUE ACTUALIZA (Aquí se usa)
                actualizarProductoCompleto(productoReal, cantidadAingresar, costoFactura, precioVentaElegido);
            }
        }
        
        // 4. Limpiamos la lista porque ya se cargó todo
        listaItems.clear();
    }
	// 1. Modificar el Costo de la Factura (Columna 2)
    public void modificarCostoEntrada(int index, BigDecimal nuevoCosto) {
        // Validamos que el índice sea correcto y el costo no sea negativo
        if (index >= 0 && index < listaItems.size() && nuevoCosto.compareTo(BigDecimal.ZERO) >= 0) {
            listaItems.get(index).setCostoNuevo(nuevoCosto);
        }
    }

    // 2. Modificar el Precio de Venta Deseado (Columna 4)
    public void modificarPrecioVenta(int index, BigDecimal nuevoPrecio) {
        // Validamos índice y que el precio no sea negativo
        if (index >= 0 && index < listaItems.size() && nuevoPrecio.compareTo(BigDecimal.ZERO) >= 0) {
            listaItems.get(index).setPrecioVenta(nuevoPrecio);
        }
    }

	/**
	 * Método para mostrar la proyección en la tabla en tiempo real
	 */
	public BigDecimal calcularProyeccionPPP(DetalleCarga item) {
		Producto p = item.getProducto();
		return CalculadoraCostos.calcularNuevoPPP(new BigDecimal(Math.max(0, p.getCantidadStock())), p.getPpp(),
				new BigDecimal(item.getUnidadesReales()), item.getCostoNuevo());
	}
	
	
}