package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Empresa {
	private String nombre;
	private Map<String, Producto> stock; // Clave: Código de Barra
	private Map<String, Usuario> usuarios; // Clave: Username
	private List<Venta> historialVentas;
	private Map<String, Venta> ventasPendientes;
	private Map<Integer, Categoria> categorias;

	public Empresa(String nombre) {
		this.nombre = nombre;
		this.stock = new HashMap<>();
		this.usuarios = new HashMap<>();
		this.historialVentas = new ArrayList<>();
		this.ventasPendientes = new HashMap<>();
		this.categorias = new HashMap<>();

	}

	public Categoria crearCategoria(String nombre, Integer idPadre) {

		int nuevoId = 1;

		while (categorias.containsKey(nuevoId)) {
			nuevoId++;
		}

		Categoria c = new Categoria(nuevoId, nombre, idPadre);
		categorias.put(nuevoId, c);
		return c;
	}

	public List<Categoria> getCategoriasMadre() {
		return categorias.values().stream().filter(Categoria::esMadre).collect(Collectors.toList());
	}

	public List<Categoria> getSubcategorias(int idMadre) {
		List<Categoria> resultado = new ArrayList<>();
		for (Categoria c : this.categorias.values()) {
			if (c.getIdPadre() != null && c.getIdPadre() == idMadre) {
				resultado.add(c);
			}
		}

		return resultado;
	}

	public void modificarCategoria(Categoria categoria, String nuevoNombre) {
		String nombreNormalizado = nuevoNombre.trim();

		// Validar que no exista OTRA categoría con ese nombre
		for (Categoria c : categorias.values()) {
			// Si tiene el mismo nombre PERO es un ID distinto, es un duplicado prohibido
			if (c.getNombre().equalsIgnoreCase(nombreNormalizado) && c.getId() != categoria.getId()) {
				throw new IllegalArgumentException("Ya existe otra categoría llamada '" + nuevoNombre + "'.");
			}
		}
		categoria.setNombre(nombreNormalizado);
	}

	public Categoria buscarCategoriaPorId(int id) {
		return categorias.get(id); // Retorna null si no existe, o el objeto al instante.
	}

	public void agregarUsuario(Usuario u) {
		this.usuarios.put(u.getUsername(), u);
	}

	public void crearUsuario(String nameUser, String clave, String nombreCompleto, Rol rol) throws Exception {

		// 1. Validación común (escrita una sola vez)
		if (usuarios.containsKey(nameUser)) {
			throw new Exception("El nombre de usuario '" + nameUser + "' ya existe.");
		}
		Usuario usuarioNuevo = null;
		switch (rol) {
		case GERENTE:
			usuarioNuevo = new Gerente(nameUser, clave, nombreCompleto);
			break;
		case CAJERO:
			usuarioNuevo = new Cajero(nameUser, clave, nombreCompleto);
			break;
		default:
			throw new Exception("Rol desconocido");
		}
		agregarUsuario(usuarioNuevo);
	}

	public boolean existeCodigoInterno(String codigoInterno) {
		for (Producto p : this.stock.values()) {
			if (p.getCodigoInterno().equalsIgnoreCase(codigoInterno)) {
				return true;
			}
		}
		return false;
	}

	public boolean elProductoYaEstaCargado(String codigoBarra) {
		return stock.containsKey(codigoBarra);
	}

	public int obtenerTodoElStock() {
		return stock.size();
	}

	public Usuario login(String username, String password) {
		Usuario u = this.usuarios.get(username);
		if (u != null && u.validarPassword(password))
			return u;
		return null;
	}

	// -----------------------------------------------------------
	// LÓGICA DE VALIDACIÓN Y ELIMINACIÓN (EN EMPRESA.JAVA)
	// -----------------------------------------------------------

	/**
	 * Verifica si una categoría se puede borrar mirando IDs.
	 */
	public boolean categoriaEstaEnUso(Categoria cat) {
		int idParaBorrar = cat.getId();

		// 1. REVISAR SI TIENE SUBCATEGORÍAS (HIJAS)
		for (Categoria c : this.categorias.values()) {
			// Si c tiene un padre, y ese padre es el que queremos borrar...
			if (c.getIdPadre() != null && c.getIdPadre() == idParaBorrar) {
				return true; // No se puede borrar, tiene hijas.
			}
		}

		// 2. REVISAR SI TIENE PRODUCTOS ASIGNADOS
		// Asumiendo que 'productos' es un Map<String, Producto>
		for (Producto p : this.stock.values()) {
			if (p.getCategoria() != null && p.getCategoria().getId() == idParaBorrar) {
				return true; // No se puede borrar, tiene productos.
			}
		}

		return false; // Está limpia, se puede borrar.
	}

	/**
	 * Elimina la categoría buscando por ID para evitar errores de referencia.
	 */
	public void eliminarCategoria(Categoria c) {
		// removeIf es la forma moderna y segura de borrar en una lista
		// "Borrar de la lista SI el id coincide con el que quiero borrar"
		this.categorias.remove(c);
	}

	public List<Producto> buscarConFiltros(String texto, Categoria categoriaFiltro, Integer stockMaximo) {
		String textoMin = texto.toLowerCase().trim();

		return stock.values().stream()
				// 1. Texto (Igual que antes)
				.filter(p -> p.getDescripcion().toLowerCase().contains(textoMin)
						|| p.getCodigoBarra().contains(textoMin))

				// 2. FILTRO DE CATEGORÍA (MEJORADO)
				.filter(p -> {
					if (categoriaFiltro == null)
						return true; // Si es "TODAS", pasa

					Categoria catProd = p.getCategoria();
					if (catProd == null)
						return false;

					// A. Coincidencia Exacta (El producto es de esa categoría)
					boolean esExacta = catProd.getId() == categoriaFiltro.getId();

					// B. Coincidencia Jerárquica (El producto es hijo de la categoría seleccionada)
					// Esto sirve si selecciono "Perfumería" y el producto es "Jabón"
					boolean esHija = catProd.getIdPadre() != null && catProd.getIdPadre() == categoriaFiltro.getId();

					return esExacta || esHija;
				})

				// 3. Stock (Igual que antes)
				.filter(p -> {
					if (stockMaximo == null)
						return true;
					return p.getCantidadStock() <= stockMaximo;
				})

				.collect(Collectors.toList());
	}

	public void agregarProducto(Producto p) {
		this.stock.put(p.getCodigoBarra(), p);
	}

	public Producto buscarProducto(String codigo) {
		return this.stock.get(codigo);
	}

	public List<Producto> buscarProductosPorNombre(String nombreBusqueda) {
		List<Producto> resultados = new java.util.ArrayList<>();
		String busqueda = nombreBusqueda.toLowerCase().trim();
		for (Producto p : this.stock.values()) {
			if (p.getDescripcion().toLowerCase().contains(busqueda)) {
				resultados.add(p);
			}
		}
		return resultados;
	}

	public Categoria buscarCategoriaPorNombre(String nombre) {
		// 1. Validación básica
		if (nombre == null || nombre.trim().isEmpty()) {
			return null;
		}

		String nombreBuscado = nombre.trim(); // Quitamos espacios adelante/atrás

		// 2. Recorremos el mapa de categorías (asumiendo que usas Map<Integer,
		// Categoria>)
		for (Categoria c : categorias.values()) {
			// Comparamos ignorando mayúsculas (asi "bebidas" encuentra "Bebidas")
			if (c.getNombre().equalsIgnoreCase(nombreBuscado)) {
				return c;
			}
		}

		// 3. Si terminamos el bucle y no apareció
		return null;
	}

	public void unificarProductos(String codPrincipal, String codDuplicado) throws Exception {
		Producto principal = stock.get(codPrincipal);
		Producto duplicado = stock.get(codDuplicado);

		if (principal == null || duplicado == null) {
			throw new Exception("Uno de los productos no existe.");
		}
		if (principal == duplicado) { // Comparan referencia de memoria
			throw new Exception("¡Son el mismo producto! No se pueden unificar.");
		}

		// 1. Sumar Stocks
		int stockDelDuplicado = duplicado.getCantidadStock();
		if (stockDelDuplicado > 0) {
			// Asumimos que ambos son unidad o bulto, sumamos directo unidades
			principal.agregarStock(stockDelDuplicado, false);
		}

		// 2. Guardar el código viejo como secundario en el principal
		principal.agregarCodigoSecundario(codDuplicado);

		// 3. Si el duplicado tenía sus propios alias, también nos los robamos
		for (String alias : duplicado.getCodigosSecundarios()) {
			principal.agregarCodigoSecundario(alias);
			// Re-apuntamos esos alias también en el mapa
			stock.put(alias, principal);
		}

		// 4. EL PASO CLAVE: Re-apuntamos el código del duplicado al objeto principal
		stock.put(codDuplicado, principal);

		// Ahora 'stock.get(codDuplicado)' devuelve el objeto 'principal'.
		// El objeto 'duplicado' viejo queda sin referencias y el recolector de basura
		// lo borra.
	}

	public void borrarCodigoSecundario(Producto producto, String codigoABorrar) {
		// 1. Lo sacamos de la lista interna del producto
		producto.getCodigosSecundarios().remove(codigoABorrar);
		stock.remove(codigoABorrar);
	}

	public void eliminarProducto(String codigo) {
		stock.remove(codigo);
	}

	public boolean validarNumero(String texto) {
		// 1. Chequeamos si es null O si esta vacío O si NO son digitos
		if (texto == null || texto.trim().isEmpty() || !texto.matches("\\d+")) {
			return true;
		}
		return false;
	}

	public void registrarVenta(Venta venta) {
		this.historialVentas.add(venta);
	}

	public void setVentaPendiente(Usuario u, Venta v) {
		// Guardamos la venta en el casillero de ESTE usuario
		this.ventasPendientes.put(u.getUsername(), v);
	}

	public Venta getVentaPendiente(Usuario u) {
		// Recuperamos solo la de ESTE usuario
		return this.ventasPendientes.get(u.getUsername());
	}

	public void borrarVentaPendiente(Usuario u) {
		this.ventasPendientes.remove(u.getUsername());
	}

	public boolean hayVentaPendiente(Usuario u) {
		// Verificamos si existe y si tiene ítems
		Venta v = this.ventasPendientes.get(u.getUsername());
		return v != null && !v.getItems().isEmpty();
	}

	public List<Venta> getHistorialVentas() {
		return historialVentas;
	}

	public String getNombre() {
		return nombre;
	}

	public List<Categoria> getCategorias() {
		return new ArrayList<>(categorias.values());
	}
}