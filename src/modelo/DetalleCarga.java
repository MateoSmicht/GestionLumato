package modelo;

import java.math.BigDecimal;

public class DetalleCarga {
	private Producto producto;
	private int cantidad;
	private boolean esBulto;
	private String codigoLeido;
	private BigDecimal costoNuevo;
	private BigDecimal precioVenta;

	// Constructor actualizado
	public DetalleCarga(Producto producto, int cantidad, boolean esBulto, String codigoLeido) {
		this.producto = producto;
		this.cantidad = cantidad;
		this.esBulto = esBulto;
		this.codigoLeido = codigoLeido;
		this.costoNuevo = producto.getPrecioCosto();
		this.precioVenta = producto.calcularPrecioFinal();
	}

	public int getUnidadesReales() {
		return esBulto ? cantidad * producto.getFactor() : cantidad;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public void setEsBulto(boolean esBulto) {
		this.esBulto = esBulto;
	}

	public void setCodigoLeido(String codigoLeido) {
		this.codigoLeido = codigoLeido;
	}

	public BigDecimal getCostoNuevo() {
		return costoNuevo;
	}

	public void setCostoNuevo(BigDecimal costoNuevo) {
		this.costoNuevo = costoNuevo;
	}

	public BigDecimal getPrecioVenta() {
		return precioVenta;
	}

	public void setPrecioVenta(BigDecimal precioVenta) {
		this.precioVenta = precioVenta;
	}

	public Producto getProducto() {
		return producto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public boolean isEsBulto() {
		return esBulto;
	}

	public String getCodigoLeido() {
		return codigoLeido;
	}
}