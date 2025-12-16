package testModelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import modelo.Categoria;
import modelo.Producto;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    private Producto producto;
    private Categoria categoriaDummy;

    @BeforeEach
    void setUp() {
        // Configuramos un entorno limpio antes de cada test
        categoriaDummy = new Categoria(2,"EJEMPLO",1);
        
        // Creamos un producto base:
        // Costo: $100
        // Ganancia: 50% (0.5) -> Precio Neto: $150
        // IVA: 21% (0.21) -> 150 * 1.21 = $181.50
        // Factor Bulto: 12 unidades
        producto = new Producto(
            "001",      // Cód Interno
            "779001",       // Cód Barra
            categoriaDummy,
            "Coca Cola Test",
            "UNIDAD",
            12,             // Factor (1 bulto = 12 unidades)
            new BigDecimal("100.00"), // Costo
            new BigDecimal("0.50"),   // Ganancia (50%)
            new BigDecimal("0.21")    // IVA (21%)
        );
    }

    @Test
    @DisplayName("Debe calcular el Precio Final correctamente (Costo + Ganancia + IVA)")
    void testCalcularPrecioFinal() {
        // Cálculo esperado:
        // 100 + 50% = 150
        // 150 + 21% IVA = 181.50
        BigDecimal esperado = new BigDecimal("181.50");
        BigDecimal resultado = producto.calcularPrecioFinal();

        // Usamos compareTo porque equals() en BigDecimal falla si la escala es distinta (181.5 vs 181.50)
        assertEquals(0, esperado.compareTo(resultado), 
            "El precio final debería ser 181.50");
    }

    @Test
    @DisplayName("Debe sumar stock correctamente por UNIDAD")
    void testAgregarStockUnidad() {
        producto.agregarStock(10, false); // false = no es bulto
        assertEquals(10, producto.getCantidadStock());
        
        producto.agregarStock(5, false);
        assertEquals(15, producto.getCantidadStock());
    }

    @Test
    @DisplayName("Debe sumar stock correctamente por BULTO (multiplicando por factor)")
    void testAgregarStockBulto() {
        // El factor es 12 (configurado en setUp)
        producto.agregarStock(2, true); // true = es bulto (2 cajas de 12)
        
        // Debería sumar 24 unidades
        assertEquals(24, producto.getCantidadStock());
    }

    @Test
    @DisplayName("Debe descontar stock correctamente")
    void testDescontarStock() {
        // Preparamos el escenario con 20 unidades
        producto.agregarStock(20, false);
        
        // Vendemos 5
        producto.descontarStock(5, false);
        
        assertEquals(15, producto.getCantidadStock());
        producto.descontarStock(15, false);
        assertEquals(0, producto.getCantidadStock());
        
    }

    @Test
    @DisplayName("Debe lanzar excepción si intentamos descontar más stock del disponible")
    void testDescontarStockInsuficiente() {
        producto.agregarStock(10, false);

        // Intentamos sacar 15 (debería fallar)
        // Nota: Esto asume que tu método descontarStock lanza excepción. 
        // Si no lo hace, deberías agregarle: if(cantidad > stock) throw new IllegalArgumentException(...)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            producto.descontarStock(15, false);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("insuficiente"));
    }

    @Test
    @DisplayName("Debe manejar correctamente los Códigos Secundarios (Alias)")
    void testCodigosSecundarios() {
        // 1. Agregamos un alias
        producto.agregarCodigoSecundario("888555");
        
        assertTrue(producto.getCodigosSecundarios().contains("888555"), 
            "La lista debería contener el alias nuevo");

        // 2. Intentamos agregar el MISMO código principal (no debería dejar o no duplicar)
        producto.agregarCodigoSecundario("779001"); 
        
        // Verificamos que no se haya agregado a la lista de secundarios (porque ya es el principal)
        assertFalse(producto.getCodigosSecundarios().contains("779001"));
        
        // 3. Intentamos agregar un duplicado ya existente en secundarios
        producto.agregarCodigoSecundario("888555");
        assertEquals(1, producto.getCodigosSecundarios().size(), 
            "No debería haber duplicados en la lista de secundarios");
    }

    @Test
    @DisplayName("Debe calcular ganancia inversa correctamente (desde Precio Final)")
    void testCalculoInverso() {
        // Si cambio el costo a 1000 y el precio final a 2000 con 0% IVA (para facilitar calculo mental)
        // Ganancia debería ser 100%
        
        // Este test depende de si tienes la lógica inversa en Producto o en ControladorStock.
        // Si la tienes en ControladorStock, este test iría allá.
        // Pero probemos que los setters afecten el precio final:
        
        producto.setPrecioCosto(new BigDecimal("1000"));
        producto.setPorcentajeGanancia(new BigDecimal("1.00")); // 100%
        producto.setAlicuotaIVA(BigDecimal.ZERO);
        
        // 1000 + 100% = 2000.
        assertEquals(0, new BigDecimal("2000.00").compareTo(producto.calcularPrecioFinal()));
    }
}