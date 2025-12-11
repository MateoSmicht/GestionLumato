package controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import modelo.Empresa;
import modelo.Producto;
import modelo.Categoria;

public class ControladorStock {

    private Empresa empresa;

    public ControladorStock(Empresa empresa) {
        this.empresa = empresa;
    }

    /**
     * Lógica: Costo + %Ganancia + IVA = Precio Final
     */
    public BigDecimal calcularPrecioFinal(String strCosto, String strGanancia, String strIVA) {
        try {
            BigDecimal costo = new BigDecimal(strCosto);
            BigDecimal porcentajeGanancia = new BigDecimal(strGanancia).divide(new BigDecimal(100));
            BigDecimal alicuotaIVA = new BigDecimal(strIVA).divide(new BigDecimal(100));

            BigDecimal gananciaDinero = costo.multiply(porcentajeGanancia);
            BigDecimal precioNeto = costo.add(gananciaDinero);
            BigDecimal ivaDinero = precioNeto.multiply(alicuotaIVA);
            
            return precioNeto.add(ivaDinero).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException | ArithmeticException e) {
            return BigDecimal.ZERO; // Si faltan datos o son inválidos
        }
    }

    /**
     /**
     * Lógica Inversa: (Final / (1+IVA)) / Costo = %Ganancia
     */
    public BigDecimal calcularGanancia(String strCosto, String strFinal, String strIVA) {
        try {
            BigDecimal costo = new BigDecimal(strCosto);
            BigDecimal precioFinal = new BigDecimal(strFinal);
            BigDecimal alicuotaIVA = new BigDecimal(strIVA).divide(new BigDecimal(100));

            if (costo.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

            // 1. Sacar IVA (Usamos 10 decimales internos para no perder centavos en la división)
            BigDecimal divisorIVA = BigDecimal.ONE.add(alicuotaIVA);
            BigDecimal precioNeto = precioFinal.divide(divisorIVA, 10, RoundingMode.HALF_UP);

            // 2. Calcular Ganancia
            BigDecimal diferencia = precioNeto.subtract(costo);
            
            // Usamos 10 decimales en la división
            BigDecimal porcentaje = diferencia.divide(costo, 10, RoundingMode.HALF_UP);
            
            // --- EL CAMBIO ESTÁ AQUÍ ---
            // Devolvemos 6 decimales en lugar de 2.
            // Esto garantiza que al hacer la cuenta al revés, vuelvas al precio exacto.
            return porcentaje.multiply(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Valida y Guarda el producto en la empresa
     */
    public void guardarProducto(String codigo, String descripcion, String strCosto, 
                                String strGanancia, String strIVA, String unidad, 
                                String strFactor, String strStock) throws Exception {
        
        // 1. Validaciones
        if (codigo.isEmpty() || descripcion.isEmpty()) {
            throw new Exception("El código y la descripción son obligatorios.");
        }
    
        // Verificamos si ya existe ese código interno en la empresa
        if (empresa.existeCodigoInterno(codigo)) {
            throw new Exception("El código interno '" + codigo + "' ya está en uso por otro producto.");
        }
        if (empresa.elProductoYaEstaCargado(codigo)) {
            throw new Exception("El Producto :(" + codigo + " - " + descripcion +  ") Ya esta creado.");
        }
        
        if(empresa.validarNumero(strStock)) {
        	throw new IllegalArgumentException("Solo puede ingresar valores numéricos enteros."+ "ERROR: " + strStock );
        }
        if(empresa.validarNumero(codigo)) {
        	throw new IllegalArgumentException("Solo puede ingresar valores numéricos enteros."+ "ERROR: " + codigo );
        }
        try {
            new BigDecimal(strCosto);
            new BigDecimal(strGanancia);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El Costo y la Ganancia deben ser números válidos.");
        }
        

        BigDecimal costo = new BigDecimal(strCosto);
        // Guardamos la ganancia real (0.30) no la visual (30)
        BigDecimal ganancia = new BigDecimal(strGanancia).divide(new BigDecimal(100)); 
        BigDecimal iva = new BigDecimal(strIVA).divide(new BigDecimal(100));
        
        int factor = Integer.parseInt(strFactor);
        int stockIni = Integer.parseInt(strStock);

        // 2. Crear Categoría Dummy (A futuro vendrá de un combo)
        Categoria catGeneral = new Categoria(1, "General");

        // 3. Crear el Objeto
        Producto nuevo = new Producto(generarCodigoInterno(), codigo, catGeneral, descripcion, unidad, factor, costo, ganancia, iva);
        
        // 4. Stock inicial
        if (stockIni > 0) {
            nuevo.agregarStock(stockIni, false); // Asumimos unidad simple
        }

        // 5. Persistir
        empresa.agregarProducto(nuevo);
    }
    
    
    
    private String generarCodigoInterno() {
        int contador = empresa.obtenerTodoElStock() + 1;
        
        String candidato = String.valueOf(contador);
        while (empresa.existeCodigoInterno(candidato)) {
            contador++; // Probamos con el siguiente (ej: de 101 pasa a 102)
            candidato = String.valueOf(contador); // Actualizamos el string candidato
        }
        return candidato;
    }
    
    
    }
    	

