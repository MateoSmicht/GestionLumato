package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Venta {
    private static long contadorId = 1;
    private long id;
    private LocalDateTime fechaHora;
    private Usuario vendedor;
    private List<DetalleVenta> items;
    private BigDecimal total;
    //Facturacion
    public Venta(Usuario vendedor) {
        this.id = contadorId++;
        this.fechaHora = LocalDateTime.now();
        this.vendedor = vendedor;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }



    public void agregarItem(Producto p, int cantidad, boolean esBulto, String codigoLeido) {
        // Creamos el detalle pasando el flag esBulto
        DetalleVenta detalle = new DetalleVenta(p, cantidad, esBulto, codigoLeido);
        
        this.items.add(detalle);
        this.total = this.total.add(detalle.calcularSubtotal());
    }
    
    public void eliminarItem(DetalleVenta detalle) {
        if (this.items.contains(detalle)) {
            this.total = this.total.subtract(detalle.calcularSubtotal());
            this.items.remove(detalle);
        }
    }
 
    public void recalcularTotal() {
        this.total = BigDecimal.ZERO;
        for (DetalleVenta d : this.items) {
            this.total = this.total.add(d.calcularSubtotal());
        }
    }
 // En modelo.Venta.java

    /**
     * Calcula cuántas unidades de un producto específico ya están en la lista de items.
     * Convierte bultos a unidades base para tener el número real.
     */
    public int calcularUnidadesEnCarrito(Producto productoBuscado) {
        int totalUnidades = 0;

        for (DetalleVenta detalle : items) {
            // 1. Verificamos si es el mismo producto
            // Usamos equals() del código interno para mayor seguridad
            String codigoItem = detalle.getProducto().getCodigoInterno();
            String codigoBuscado = productoBuscado.getCodigoInterno();

            if (codigoItem.equals(codigoBuscado)) {
                // 2. Sumamos la cantidad REAL (multiplicada por factor si es bulto)
                totalUnidades += detalle.getCantidadUnidadesReales();
            }
        }
        return totalUnidades;
    }
    
    public static long getContadorId() {
		return contadorId;
	}



	public static void setContadorId(long contadorId) {
		Venta.contadorId = contadorId;
	}



	public LocalDateTime getFechaHora() {
		return fechaHora;
	}



	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}
	
	public String getFechaFormateada() {
        if (this.fechaHora == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fechaHora.format(formatter);
    }



	public void setId(long id) {
		this.id = id;
	}



	public void setVendedor(Usuario vendedor) {
		this.vendedor = vendedor;
	}



	public void setItems(List<DetalleVenta> items) {
		this.items = items;
	}



	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public long getId() { return id; }
    public BigDecimal getTotal() { return total; }
    public Usuario getVendedor() { return vendedor; }
    public List<DetalleVenta> getItems() { return items; }



	@Override
	public int hashCode() {
		return Objects.hash(fechaHora, id, items, total, vendedor);
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Venta other = (Venta) obj;
		return Objects.equals(fechaHora, other.fechaHora) && id == other.id && Objects.equals(items, other.items)
				&& Objects.equals(total, other.total) && Objects.equals(vendedor, other.vendedor);
	}
    
}