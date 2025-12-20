package controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.swing.JOptionPane;

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
    
    public String calcularPrecioVenta(String strCosto, String strGanancia, String strIVA) {
        try {
            if (strCosto == null || strCosto.isEmpty()) return "";
            if (strGanancia == null || strGanancia.isEmpty()) strGanancia = "0";

            BigDecimal costo = new BigDecimal(strCosto.replace(",", "."));
            BigDecimal porcentajeGan = new BigDecimal(strGanancia.replace(",", ".")).divide(new BigDecimal(100));
            BigDecimal iva = new BigDecimal(strIVA).divide(new BigDecimal(100));
            
            // 1. Precio Neto = Costo * (1 + %Ganancia)
            BigDecimal precioNeto = costo.add(costo.multiply(porcentajeGan));
            
            // 2. Precio Final = Precio Neto * (1 + %IVA)
            BigDecimal precioFinal = precioNeto.add(precioNeto.multiply(iva));
            
            return precioFinal.setScale(2, RoundingMode.HALF_UP).toString();
        } catch (Exception e) {
            return ""; // Si hay error (letras, vacíos), devolvemos cadena vacía
        }
    }

    /**
     * Devuelve las categorías para llenar el combo.
     * Así la vista no toca la variable "empresa" directamente.
     */
    public List<Categoria> obtenerCategoriasMadre() {
        return empresa.getCategoriasMadre();
    }
    
    public String calcularPorcentajeGanancia(String strCosto, String strPrecioFinal) {
        try {
            if (strCosto == null || strCosto.isEmpty()) return "0";
            if (strPrecioFinal == null || strPrecioFinal.isEmpty()) return "0";

            java.math.BigDecimal costo = new java.math.BigDecimal(strCosto.replace(",", "."));
            java.math.BigDecimal precioFinal = new java.math.BigDecimal(strPrecioFinal.replace(",", "."));
            
            // Asumimos IVA 21% por defecto para este cálculo rápido inverso
            // Si quisieras ser exacto, deberías pasar el IVA como parámetro también.
            java.math.BigDecimal iva = new java.math.BigDecimal("1.21"); 

            // Evitamos división por cero
            if (costo.compareTo(java.math.BigDecimal.ZERO) == 0) return "0";

            // 1. Costo con IVA
            java.math.BigDecimal costoConIVA = costo.multiply(iva);
            
            // 2. División (Precio / CostoConIVA) - 1
            java.math.BigDecimal gananciaDecimal = precioFinal
                .divide(costoConIVA, 4, java.math.RoundingMode.HALF_UP)
                .subtract(java.math.BigDecimal.ONE);
            
            // 3. Multiplicar por 100 para porcentaje
            return gananciaDecimal.multiply(new java.math.BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .toString();

        } catch (Exception e) {
            return "0";
        }
    }
    
    

    /**
     * Valida y Guarda el producto en la empresa
     */
    public void guardarProducto(String codigo, String descripcion,Categoria cat, String strCosto, 
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

        
        // 3. Crear el Objeto
        Producto nuevo = new Producto(generarCodigoInterno(),codigo,cat, descripcion, unidad, factor, costo, ganancia, iva);
        
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
    /**
     * Procesa un archivo CSV y carga los productos.
     * @param archivo El archivo seleccionado por el usuario.
     * @return Un mensaje con el resumen de la operación.
     */
    public String importarProductosDesdeCSV(File archivo) {
        int cargados = 0;
        int errores = 0;
        int saltados = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                
                // 1. LIMPIEZA
                if (linea.isEmpty()) continue;
                String lineaUpper = linea.toUpperCase();
                if (lineaUpper.startsWith("CODIGO") || lineaUpper.startsWith("A,B,C")) {
                    saltados++;
                    continue;
                }

                try {
                    // 2. SEPARADOR
                    String[] datos = linea.split(";");
                    if (datos.length < 5) datos = linea.split(",");
                    if (datos.length < 5) { saltados++; continue; }

                    // 3. DATOS
                    String codigo = datos[0].trim();
                    String descripcion = datos[1].trim();
                    String nombreCategoria = datos[2].trim();
                    
                    String costoStr = datos[3].replace("$", "").replace(",", ".").trim();
                    String precioStr = datos[4].replace("$", "").replace(",", ".").trim();
                    
                    String stockStr = datos[5].replace(",", ".").trim();
                    if(stockStr.contains(".")) stockStr = stockStr.substring(0, stockStr.indexOf("."));

                    // 4. CATEGORÍA
                    Categoria cat = empresa.buscarCategoriaPorNombre(nombreCategoria);
                    if (cat == null) {
                        empresa.crearCategoria(nombreCategoria, null);
                        cat = empresa.buscarCategoriaPorNombre(nombreCategoria);
                    }

                    // 5. CALCULAR GANANCIA
                    String gananciaCalculada = calcularPorcentajeGanancia(costoStr, precioStr);

                    // 6. GUARDAR
                    this.guardarProducto(
                        codigo, 
                        descripcion, 
                        cat, 
                        costoStr, 
                        gananciaCalculada, // Usamos el valor de la función
                        "21.0", 
                        "UNI", 
                        "1", 
                        stockStr
                    );

                    cargados++;

                } catch (Exception e) {
                    errores++;
                    System.out.println("Error procesando línea: " + linea + " | " + e.getMessage());
                }
            }
            
            return "Importación finalizada.\n✅ Cargados: " + cargados + "\n❌ Errores: " + errores + "\n⏭️ Saltados: " + saltados;

        } catch (Exception e) {
            return "Error crítico al abrir archivo: " + e.getMessage();
        }   
    }
    
    }
    	

