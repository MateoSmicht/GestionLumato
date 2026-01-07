package testModelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.CalculadoraCostos;
import modelo.Categoria;
import modelo.Producto;


public class testCalculadoraCosto {
	
	private Producto producto;
    private Categoria categoriaDummy;

    @BeforeEach
    void setUp() {
        // Configuramos un entorno limpio antes de cada test
        categoriaDummy = new Categoria(2,"EJEMPLO",1);
        producto = new Producto(
            "1",      // Cód Interno
            "779",       // Cód Barra
            categoriaDummy,
            "Coca Cola Test",
            "UNIDAD",
            12,             // Factor (1 bulto = 12 unidades)
            new BigDecimal("1500.00"), // Costo
            new BigDecimal("0.50"),   // Ganancia (50%)
            new BigDecimal("0.21")    // IVA (21%)
            
        );
        producto.agregarStock(1, false); //Una unidad en stock
    }
	    
	    @Test
	    void testCalcularPorcentajeGanancia() {
	        BigDecimal precioVenta = new BigDecimal("1500.00");
	        BigDecimal precioCosto = new BigDecimal("1000.00");
	        BigDecimal esperado = new BigDecimal("0.500000");
	        BigDecimal resultado = CalculadoraCostos.calcularPorcentajeGanancia(precioCosto,precioVenta);

	        assertEquals(esperado, resultado, 
	            "El porcentaje final deberia ser 0.500000");
	    }
	    @Test
	    void testCalcularNuevoPPP() {
	    	BigDecimal precioNew = new BigDecimal("1000.00");
	    	BigDecimal stockEntrante =new BigDecimal("1.0");
	    	BigDecimal stockIni =new BigDecimal(producto.getCantidadStock());
	    	BigDecimal esperado = new BigDecimal("1250.00");//(1500+1000)/2 = 1250 
	    	 assertEquals(esperado, CalculadoraCostos.calcularNuevoPPP(stockIni, producto.getPpp(), stockEntrante, precioNew), 
	 	            "el ppp esperado es 1250.00");
	    	
	    }
	    
	    @Test 
	    void testConvertirPorcentajeADecimal() {
	    	BigDecimal porcentajeVisual= new BigDecimal("50.0");
	    	BigDecimal esperado = new BigDecimal("0.500000");
	    	assertEquals(esperado,CalculadoraCostos.convertirPorcentajeADecimal(porcentajeVisual) , 
	 	            "el porcentaje decimal espero es 0.500000");
	    }
}
