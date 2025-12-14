package testModelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.DetalleVenta;
import modelo.Producto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DetalleVentaTest {

    private Producto productoDummy;

    @BeforeEach
    void setUp() {
        // CREAMOS UN PRODUCTO "MOCK" O FICTICIO PARA LA PRUEBA
        // Supongamos: Costo 50 + Ganancia 100% = Precio Final $100.00
        // Factor: 10 (1 Bulto = 10 Unidades)
        
        // NOTA: Ajusta este constructor a como lo tengas actualmente en tu clase Producto
        productoDummy = new Producto(
            "COD-INT", "779000", null, "Producto Test", 
            "PACK", // Nombre del bulto
            10,     // Factor
            new BigDecimal("50.00"), // Costo
            new BigDecimal("1.00"),  // Ganancia (100%)
            BigDecimal.ZERO          // IVA 0% para facilitar cuentas
        );
        
        // Verificamos pre-condición: El precio unitario base debe ser 100
        // Si esto falla, el problema está en Producto, no en DetalleVenta
        assertEquals(0, new BigDecimal("100.00").compareTo(productoDummy.calcularPrecioFinal()));
    }

    @Test
    @DisplayName("Caso 1: Venta por UNIDAD (Debe usar factor 1 y precio base)")
    void testVentaPorUnidad() {
        // Venta de 5 Unidades Sueltas
        DetalleVenta detalle = new DetalleVenta(productoDummy, 5, false, "779000");

        assertAll("Verificaciones de Unidad",
            // 1. Snapshot de nombre
            () -> assertEquals("UNI", detalle.getNombreUnidadSnapshot(), "Debe decir UNI"),
            
            // 2. Snapshot de Factor
            () -> assertEquals(1, detalle.getFactorSnapshot(), "Factor debe ser 1"),
            
            // 3. Precio Unitario (Debe ser $100)
            () -> assertEquals(0, new BigDecimal("100.00").compareTo(detalle.getPrecioUnitarioSnapshot()), 
                  "El precio snapshot debe ser el unitario ($100)"),
            
            // 4. Subtotal (5 * 100 = 500)
            () -> assertEquals(0, new BigDecimal("500.00").compareTo(detalle.calcularSubtotal()), 
                  "El subtotal debe ser 500"),
            
            // 5. Descuento de Stock Real (5 * 1 = 5)
            () -> assertEquals(5, detalle.getCantidadUnidadesReales(), "Debe descontar 5 unidades del stock")
        );
    }

    @Test
    @DisplayName("Caso 2: Venta por BULTO (Debe multiplicar precio y factor)")
    void testVentaPorBulto() {
        // Venta de 2 Packs (Cada pack trae 10, total 20 unidades reales)
        // Precio del pack = 100 * 10 = $1000
        DetalleVenta detalle = new DetalleVenta(productoDummy, 2, true, "779000");

        assertAll("Verificaciones de Bulto",
            // 1. Snapshot de nombre
            () -> assertEquals("PACK", detalle.getNombreUnidadSnapshot(), "Debe tomar el nombre del bulto del producto"),
            
            // 2. Snapshot de Factor
            () -> assertEquals(10, detalle.getFactorSnapshot(), "Factor debe ser 10"),
            
            // 3. Precio Unitario VISUAL (Precio del Bulto = 100 * 10 = 1000)
            () -> assertEquals(0, new BigDecimal("1000.00").compareTo(detalle.getPrecioUnitarioSnapshot()), 
                  "El precio snapshot debe ser el del bulto completo ($1000)"),
            
            // 4. Subtotal (2 bultos * 1000 = 2000)
            () -> assertEquals(0, new BigDecimal("2000.00").compareTo(detalle.calcularSubtotal()), 
                  "El subtotal debe ser 2000"),
            
            // 5. Descuento de Stock Real (2 * 10 = 20 unidades)
            () -> assertEquals(20, detalle.getCantidadUnidadesReales(), 
                  "Debe descontar 20 unidades reales del stock")
        );
    }

    @Test
    @DisplayName("Debe guardar el código leído exacto (Aliasing)")
    void testCodigoLeido() {
        // Simulamos que escaneamos un código secundario
        String codigoAlternativo = "888999";
        DetalleVenta detalle = new DetalleVenta(productoDummy, 1, false, codigoAlternativo);

        assertEquals(codigoAlternativo, detalle.getCodigoLeido(), 
            "El detalle debe recordar qué código de barras específico se usó");
        
        // Aunque el producto tenga otro código principal
        assertEquals("779000", detalle.getProducto().getCodigoBarra());
    }

    @Test
    @DisplayName("Debe actualizar el Subtotal si cambia la cantidad")
    void testModificarCantidad() {
        // Inicial: 1 Unidad ($100)
        DetalleVenta detalle = new DetalleVenta(productoDummy, 1, false, "779000");
        
        // Cambio a 3 Unidades
        detalle.setCantidad(3);

        // Nuevo Subtotal: 300
        assertEquals(0, new BigDecimal("300.00").compareTo(detalle.calcularSubtotal()), 
            "El subtotal debe recalcularse al cambiar la cantidad");
            
        // Nuevo Stock a descontar: 3
        assertEquals(3, detalle.getCantidadUnidadesReales());
    }
    
    @Test
    @DisplayName("Debe mantener el precio original incluso si el producto cambia después (Inmutabilidad del Snapshot)")
    void testIntegridadSnapshot() {
        // 1. Creamos la venta con el precio a $100
        DetalleVenta detalle = new DetalleVenta(productoDummy, 1, false, "779000");
        
        // 2. INFLACIÓN: Aumentamos el precio del producto original a $200
        // (Simulamos esto cambiando el costo o ganancia en el objeto producto)
        productoDummy.setPrecioCosto(new BigDecimal("200.00")); 
        
        // Verificamos que el producto ahora vale más
        assertTrue(productoDummy.calcularPrecioFinal().compareTo(new BigDecimal("100.00")) > 0);

        // 3. Verificamos que el DETALLE DE VENTA siga valiendo $100 (Precio Histórico)
        assertEquals(0, new BigDecimal("100.00").compareTo(detalle.getPrecioUnitarioSnapshot()), 
            "El precio en el detalle NO debe cambiar si cambia el producto después");
    }
}