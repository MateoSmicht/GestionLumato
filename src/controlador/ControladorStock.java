package controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import modelo.Empresa;
import modelo.Producto;
import modelo.Categoria;
import persistencia.RepositorioProducto;
import persistencia.RepositorioProductoJSON;

public class ControladorStock {

    private Empresa empresa;
    private RepositorioProducto repositorio;

    public ControladorStock(Empresa empresa, RepositorioProducto repositorio) {
        this.empresa = empresa;
        this.repositorio = repositorio;
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

   
    
    public Producto buscarProducto(String codigo) {
    	return repositorio.buscarPorCodigo(codigo);
    }
    
    public String calcularPorcentajeGanancia(String strCosto, String strPrecioFinal) {
        try {
            if (strCosto == null || strCosto.isEmpty()) return "0";
            if (strPrecioFinal == null || strPrecioFinal.isEmpty()) return "0";

            BigDecimal costo = new java.math.BigDecimal(strCosto.replace(",", "."));
            BigDecimal precioFinal = new java.math.BigDecimal(strPrecioFinal.replace(",", "."));
            BigDecimal iva = new java.math.BigDecimal("1.0"); 

            // Evitamos división por cero
            if (costo.compareTo(BigDecimal.ZERO) == 0) return "0";

            // 1. Costo con IVA
            BigDecimal costoConIVA = costo.multiply(iva);
            
            // 2. División (Precio / CostoConIVA) - 1
           BigDecimal gananciaDecimal = precioFinal
                .divide(costoConIVA, 4, java.math.RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE);
            
            // 3. Multiplicar por 100 para porcentaje
            return gananciaDecimal.multiply(new BigDecimal("100"))
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
        
    	if (codigo == null || codigo.isEmpty() || descripcion.isEmpty()) {
            throw new Exception("El código y la descripción son obligatorios.");
        }

        // B. Validar que Stock y Factor sean números (Sustituye a empresa.validarNumero)
        if (!esNumerico(strStock)) {
            throw new IllegalArgumentException("El Stock debe ser un número entero válido. Valor ingresado: " + strStock);
        }
        Producto existente = repositorio.buscarPorCodigo(codigo);
        if (existente != null) {
            throw new Exception("El producto con código de barra '" + codigo + "' ya existe: " + existente.getDescripcion());
        }
        try {
            BigDecimal costo = new BigDecimal(strCosto);
            BigDecimal ganancia = new BigDecimal(strGanancia).divide(new BigDecimal(100)); 
            BigDecimal iva = new BigDecimal(strIVA).divide(new BigDecimal(100));
            int factor = Integer.parseInt(strFactor);
            int stockIni = Integer.parseInt(strStock);

            // Crear el Objeto
            Producto nuevo = new Producto(generarCodigoInterno(), codigo, cat, descripcion, unidad, factor, costo, ganancia, iva);
            
            if (stockIni > 0) {
                nuevo.agregarStock(stockIni, false);
            }
            repositorio.guardar(nuevo); 

        } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Error en los montos (Costo/Ganancia/IVA). Verifique los números.");
        }
    }
    
    private boolean esNumerico(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    
    private String generarCodigoInterno() {
        List<Producto> todos = repositorio.obtenerTodos();
        int maxId = 0;
        for (Producto p : todos) {
            try {
                int id = Integer.parseInt(p.getCodigoInterno());
                if (id > maxId) maxId = id;
            } catch (Exception e) { /* Ignorar alfanuméricos */ }
        }
        return String.valueOf(maxId + 1);
    }
    
    public void modificarProductoCompleto(String codigoOriginal, String nuevoCodigo, String descripcion, 
                                          Categoria categoria, String costo, String ganancia, String iva, 
                                          String unidad, String factor, String stock) throws Exception {
        
        // 1. Si cambió el código, validamos que el nuevo no exista ya (para no pisar otro producto)
        if (!codigoOriginal.equals(nuevoCodigo)) {
            if (empresa.elProductoYaEstaCargado(nuevoCodigo)) {
                throw new Exception("El nuevo código ya pertenece a otro producto.");
            }
            // Eliminamos el viejo (porque la Key del Map cambió)
            empresa.eliminarProducto(codigoOriginal);
        }

        // 2. Reutilizamos el método guardar (que hace Upsert: crea o actualiza)
        guardarProducto(nuevoCodigo, descripcion, categoria, costo, ganancia, iva, unidad, factor, stock);
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
    
 // Pegar dentro de ControladorStock.java
    public List<Producto> buscarProductosConFiltros(String texto, Categoria categoriaFiltro, Integer stockMaximo) {
        List<Producto> todos = repositorio.obtenerTodos();
        java.util.List<Producto> resultado = new java.util.ArrayList<>();
        String textoBusqueda = (texto != null) ? texto.toUpperCase().trim() : "";

        for (Producto p : todos) {
            boolean coincideTexto = textoBusqueda.isEmpty() || p.coincideCon(textoBusqueda);
            
            boolean coincideCategoria = true;
            if (categoriaFiltro != null && p.getCategoria() != null) {
                boolean esExacta = p.getCategoria().getId() == categoriaFiltro.getId();
                boolean esHija = p.getCategoria().esHijaDe(categoriaFiltro.getId());
                coincideCategoria = esExacta || esHija;
            } else if (categoriaFiltro != null) {
                coincideCategoria = false;
            }

            boolean coincideStock = true;
            if (stockMaximo != null) {
                coincideStock = p.getCantidadStock() <= stockMaximo;
            }

            if (coincideTexto && coincideCategoria && coincideStock) {
                resultado.add(p);
            }
        }
        return resultado;
    }
    

    public List<Producto> obtenerTodosLosProductos() {
        
        return repositorio.obtenerTodos();
    }
    

    /**
     * Convierte el decimal (0.30) a String para la vista ("30")
     * LOGICA PURA: La vista no debe saber multiplicar.
     */
    public String formatearGananciaParaVista(BigDecimal ganancia) {
        if (ganancia == null) return "0";
        return ganancia.multiply(new BigDecimal(100))
                       .stripTrailingZeros()
                       .toPlainString();
    }

    /**
     * Convierte el decimal (0.21) a String para el Combo ("21.0")
     */
    public String formatearIVAParaVista(BigDecimal iva) {
        if (iva == null) return "0.0";
        String texto = iva.multiply(new BigDecimal(100))
                          .stripTrailingZeros()
                          .toPlainString();
        if (texto.equals("21")) return "21.0"; // Ajuste estético
        return texto;
    }
 

    public List<Categoria> obtenerSubCategorias(int idMadre) {
        return empresa.getSubcategorias(idMadre);
    }
    
    public void unificarProductos(String codPrincipal, String codDuplicado) throws Exception {
        // 1. Buscamos los objetos usando el repositorio
        Producto principal = repositorio.buscarPorCodigo(codPrincipal);
        Producto duplicado = repositorio.buscarPorCodigo(codDuplicado);

        // 2. Validaciones
        if (principal == null || duplicado == null) {
            throw new Exception("Uno de los productos no existe.");
        }
        
        // Comparamos por ID interno para saber si son el mismo objeto en memoria
        if (principal.getCodigoInterno().equals(duplicado.getCodigoInterno())) { 
            throw new Exception("¡Son el mismo producto! No se pueden unificar.");
        }

        // 3. FUSIONAR STOCKS
        int stockDelDuplicado = duplicado.getCantidadStock();
        if (stockDelDuplicado > 0) {
            principal.agregarStock(stockDelDuplicado, false);
        }
        principal.agregarCodigoSecundario(duplicado.getCodigoBarra());

        for (String alias : duplicado.getCodigosSecundarios()) {
            principal.agregarCodigoSecundario(alias);
        }

        repositorio.eliminar(duplicado.getCodigoInterno());
        
        repositorio.guardar(principal); 
    }
    
    public void borrarCodigoSecundario(Producto producto, String codigoABorrar) {
        if (producto.getCodigosSecundarios().remove(codigoABorrar)) {
            // 1. Actualizamos el archivo JSON
            repositorio.guardar(producto);
            
            // 2. Actualizamos la memoria RAM del repositorio (CASTEO NECESARIO SI NO LO PONES EN LA INTERFAZ)
            if (repositorio instanceof RepositorioProductoJSON) {
                ((RepositorioProductoJSON) repositorio).eliminarIndiceBarra(codigoABorrar);
            }
        }
    }
    
    }
    	

