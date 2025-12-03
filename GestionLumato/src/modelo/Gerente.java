package modelo;

public class Gerente extends Usuario {
    public Gerente(String username, String password, String nombreCompleto) {
        super(username, password, nombreCompleto);
        // Tiene todos los permisos
        for (Funcion f : Funcion.values()) {
            this.funcionesPermitidas.add(f);
        }
    }

    @Override
    public String getNombreRol() { return "Gerente General"; }
}