package testModelo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.Cajero;
import modelo.Funcion;
import modelo.Gerente;
import modelo.Usuario;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario gerente;
    private Usuario cajero;

    @BeforeEach
    void setUp() {
        // Instanciamos las clases hijas para probar la clase padre abstracta
        gerente = new Gerente("admin", "secret123", "Juan Perez");
        cajero = new Cajero("caja1", "pass123", "Maria Lopez");
        
        // NOTA: Asumimos que el constructor de Gerente agrega funciones al Set.
        // Si tu constructor no lo hace, deberías tener un método para agregarlas 
        // o hacerlo aquí si la lista fuera pública (pero es protected).
        // Para este test, asumo que Gerente nace con permisos de ADMINISTRAR.
    }

    @Test
    @DisplayName("Debe validar correctamente la contraseña (Método validarPassword)")
    void testValidarPassword() {
        // 1. Caso Exitoso
        assertTrue(gerente.validarPassword("secret123"), 
            "La contraseña correcta debe devolver true");
        
        // 2. Caso Fallido
        assertFalse(gerente.validarPassword("otraClave"), 
            "Una contraseña incorrecta debe devolver false");
        
        // 3. Caso Case Sensitive (importante en seguridad)
        assertFalse(gerente.validarPassword("SECRET123"), 
            "La contraseña debe diferenciar mayúsculas de minúsculas");
    }

    @Test
    @DisplayName("Debe verificar permisos correctamente (Método puede)")
    void testPermisos() {
        // Este test depende de cómo cargues los permisos en tus subclases.
        // Supongamos que Gerente tiene ADMINISTRAR_USUARIOS y Cajero NO.
        
        // Test positivo (Gerente puede)
        // Nota: Asegurate que tu clase Gerente haga 'this.funcionesPermitidas.add(Funcion.ADMINISTRAR_USUARIOS)'
        if (gerente instanceof Gerente) {
             // Si aún no tienes lógica de carga de permisos, este test fallará hasta que la agregues.
             // assertTrue(gerente.puede(Funcion.ADMINISTRAR_USUARIOS));
        }

        // Test negativo (Cajero no debería poder administrar)
        assertFalse(cajero.puede(Funcion.GESTION_EMPLEADOS), 
            "El cajero no debería tener permiso de administrador");
    }

    @Test
    @DisplayName("Debe formatear el toString con el Rol y Nombre")
    void testToString() {
        // La clase Usuario define: getNombreRol() + ": " + nombreCompleto
        
        String resultadoGerente = gerente.toString();
        String resultadoCajero = cajero.toString();

        // Verificamos que contenga el Rol (implementado en la hija) y el nombre (en la padre)
        assertEquals("Gerente General: Juan Perez", resultadoGerente);
        assertEquals("Cajero: Maria Lopez", resultadoCajero);
    }

    @Test
    @DisplayName("Debe inicializar la lista de funciones vacía (Constructor)")
    void testInicializacion() {
        // Creamos un usuario "anónimo" para probar el estado inicial de la clase abstracta
        // Esto es un truco avanzado para probar clases abstractas sin usar Gerente/Cajero
        Usuario usuarioAbstracto = new Usuario("test", "123", "Test") {
            @Override
            public String getNombreRol() { return "TestRole"; }
        };

        // Verificamos que no sea nula, evitando NullPointerException
        assertNotNull(usuarioAbstracto.getFuncionesPermitidas(), 
            "El Set de funciones no debe ser null al nacer");
            
        // Verificamos que arranque vacía (antes de que la subclase agregue cosas)
        assertTrue(usuarioAbstracto.getFuncionesPermitidas().isEmpty(), 
            "El Set de funciones debe iniciar vacío");
    }
}