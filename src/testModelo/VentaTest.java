package testModelo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.Cajero;
import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Usuario;
import modelo.Venta;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class VentaTest {

    private Venta venta;
    private Usuario vendedor;
    private Producto producto;
    private Producto productoBulto;

    @BeforeEach
    void setUp() {
        // 1. Preparamos el Vendedor
        // Asumimos que tienes una subclase concreta como Cajero o usas una anónima
        vendedor = new Cajero("caja1", "123", "Pepe Cajero");

        // 2. Iniciamos una Venta nueva antes de cada test
        venta = new Venta(vendedor);

        // 3. Preparamos Productos de prueba
        // Producto A: Precio Final $100.00
        producto = new Producto(
            "COD1", "7791", null, "Coca Cola", "UNI", 1, 
            new BigDecimal("50.00"), new BigDecimal("1.00"), BigDecimal.ZERO // 50 costo + 100% ganancia = 100 precio
        );

        // Producto B (Con Factor): Precio Unit $200, Factor 10 (Bulto vale $2000)
        productoBulto = new Producto(
            "COD2", "7792", null, "Pack Cerveza", "PACK", 10,
            new BigDecimal("100.00"), new BigDecimal("1.00"), BigDecimal.ZERO // 100 costo + 100% ganancia = 200 precio unit
        );
    }

    @Test
    @DisplayName("Debe iniciar con valores en cero/vacíos")
    void testInicializacion() {
    	// Busca la línea 51 y cámbiala por esto:
    	assertEquals(vendedor.getNombreCompleto(), venta.getNombreVendedor(), "El nombre del vendedor debe coincidir");
        assertTrue(venta.getItems().isEmpty(), "La lista de ítems debe iniciar vacía");
        assertEquals(BigDecimal.ZERO, venta.getTotal(), "El total debe iniciar en 0");
    }

    @Test
    @DisplayName("Debe sumar correctamente al agregar UNIDADES sueltas")
    void testAgregarItemUnidad() {
        // Agregamos 2 unidades de $100 c/u
        venta.agregarItem(producto, 2, false, "7791");

        // Verificamos lista
        assertEquals(1, venta.getItems().size());
        
        // Verificamos Total: 2 * 100 = 200
        assertEquals(0, new BigDecimal("200.00").compareTo(venta.getTotal()), 
            "El total debería ser $200.00");
    }

    @Test
    @DisplayName("Debe sumar correctamente al agregar BULTOS (Precio Unit * Factor)")
    void testAgregarItemBulto() {
        // ProductoBulto: Unitario $200. Factor 10. Precio Bulto = $2000.
        // Agregamos 2 Bultos. Total esperado: $4000.
        
        venta.agregarItem(productoBulto, 2, true, "7792");

        assertEquals(1, venta.getItems().size());
        assertEquals(0, new BigDecimal("4000.00").compareTo(venta.getTotal()), 
            "El total debería ser 4000 (2 packs de 2000)");
    }

    @Test
    @DisplayName("Debe acumular el total con múltiples productos distintos")
    void testAcumulacionTotal() {
        // 1. Agregamos 1 unidad de $100
        venta.agregarItem(producto, 1, false, "7791");
        
        // 2. Agregamos 1 bulto de $2000
        venta.agregarItem(productoBulto, 1, true, "7792");

        // Total esperado: 2100
        assertEquals(0, new BigDecimal("2100.00").compareTo(venta.getTotal()));
        assertEquals(2, venta.getItems().size());
    }

    @Test
    @DisplayName("Debe restar el total correctamente al eliminar un ítem")
    void testEliminarItem() {
        // Agregamos dos productos
        venta.agregarItem(producto, 1, false, "7791"); // $100
        venta.agregarItem(productoBulto, 1, true, "7792"); // $2000
        // Total parcial: 2100

        // Obtenemos referencia al primer detalle para borrarlo
        DetalleVenta detalleABorrar = venta.getItems().get(0); // El de $100
        
        // Ejecutamos eliminación
        venta.eliminarItem(detalleABorrar);

        // Verificaciones
        assertEquals(1, venta.getItems().size(), "Debe quedar 1 solo ítem");
        assertFalse(venta.getItems().contains(detalleABorrar), "El ítem borrado no debe estar");
        
        // El total debe haber bajado a 2000
        assertEquals(0, new BigDecimal("2000.00").compareTo(venta.getTotal()), 
            "El total debe restar el ítem eliminado");
    }

    @Test
    @DisplayName("Debe recalcular el total si modificamos un detalle externamente")
    void testRecalcularTotal() {
        // Agregamos 1 unidad ($100)
        venta.agregarItem(producto, 1, false, "7791");
        
        // Accedemos al detalle y cambiamos la cantidad a 5 "por fuera" de la clase Venta
        // (Esto simula cuando editas la celda de cantidad en la tabla visual)
        DetalleVenta detalle = venta.getItems().get(0);
        detalle.setCantidad(5); 
        // Ahora el subtotal del detalle es 500, pero la Venta sigue pensando que es 100
        
        // Forzamos el recalculo
        venta.recalcularTotal();

        assertEquals(0, new BigDecimal("500.00").compareTo(venta.getTotal()), 
            "El método recalcularTotal debe sumarizar los subtotales actuales");
    }

    @Test
    @DisplayName("Debe generar IDs autoincrementales")
    void testIdsUnicos() {
        Venta venta1 = new Venta(vendedor);
        Venta venta2 = new Venta(vendedor);

        assertNotEquals(venta1.getId(), venta2.getId(), "Los IDs deben ser distintos");
        assertTrue(venta2.getId() > venta1.getId(), "El ID debe incrementar");
    }
    
    @Test
    @DisplayName("Debe respetar el código leído (Snapshot) en la lista")
    void testCodigoLeidoEnLista() {
        String codigoAlternativo = "99999";
        venta.agregarItem(producto, 1, false, codigoAlternativo);
        
        DetalleVenta detalleGuardado = venta.getItems().get(0);
        
        assertEquals(codigoAlternativo, detalleGuardado.getCodigoLeido(), 
            "La venta debe guardar el detalle con el código específico que se escaneó");
    }
}