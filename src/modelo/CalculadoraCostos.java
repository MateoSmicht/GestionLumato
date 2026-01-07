package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class CalculadoraCostos {
	
    // Método PPP (Igual que antes)
    public static BigDecimal calcularNuevoPPP(BigDecimal stockActual, BigDecimal pppActual, 
                                              BigDecimal cantidadEntrante, BigDecimal costoEntrante) {
        if (stockActual == null) stockActual = BigDecimal.ZERO;
        if (pppActual == null) pppActual = BigDecimal.ZERO;
        if (cantidadEntrante == null) cantidadEntrante = BigDecimal.ZERO;
        if (costoEntrante == null) costoEntrante = BigDecimal.ZERO;

        BigDecimal valorTotal = (stockActual.multiply(pppActual)).add(cantidadEntrante.multiply(costoEntrante));
        BigDecimal cantidadTotal = stockActual.add(cantidadEntrante);

        if (cantidadTotal.compareTo(BigDecimal.ZERO) == 0) return costoEntrante;
        return valorTotal.divide(cantidadTotal, 2, RoundingMode.HALF_UP);
    }

 
    //Devuelve el porcentaje de ganancia
    public static BigDecimal calcularPorcentajeGanancia(BigDecimal costo, BigDecimal precioFinal) {
        // 1. Validaciones de seguridad: Nulos o Costo Cero
        if (costo == null || precioFinal == null || costo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

            BigDecimal relacion = precioFinal.divide(costo, 10, RoundingMode.HALF_UP);

            // B. Restar 1 (Para obtener el decimal de ganancia pura)
            BigDecimal gananciaDecimal = relacion.subtract(BigDecimal.ONE);

            // C. Multiplicar por 100 y redondear al final (Escala 2 para el %)
            return convertirPorcentajeADecimal(gananciaDecimal.multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP));
    }
    
    public static BigDecimal convertirPorcentajeADecimal(BigDecimal valorVisual) {
        if (valorVisual == null) {
            return BigDecimal.ZERO;
        }
        // Usamos escala 6 para no perder precisión si ingresan 33.33 (quedaría 0.3333)
        return valorVisual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
    }
}