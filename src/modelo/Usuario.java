package modelo;

import java.util.HashSet;
import java.util.Set;

public abstract class Usuario {
    protected String username;
    protected String password;
    protected String nombreCompleto;
    protected Set<Funcion> funcionesPermitidas;

    public Usuario(String username, String password, String nombreCompleto) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.funcionesPermitidas = new HashSet<>();
    }

    public boolean puede(Funcion funcion) {
        return this.funcionesPermitidas.contains(funcion);
    }
    
    public boolean validarPassword(String passwordIngresada) {
        return this.password.equals(passwordIngresada);
    }

    public abstract String getNombreRol();

    public String getUsername() { return username; }
    public String getNombreCompleto() { return nombreCompleto; }
    
    
    public Set<Funcion> getFuncionesPermitidas() {
		return funcionesPermitidas;
	}

	public void setFuncionesPermitidas(Set<Funcion> funcionesPermitidas) {
		this.funcionesPermitidas = funcionesPermitidas;
	}

	@Override
    public String toString() {
        return getNombreRol() + ": " + nombreCompleto;
    }
}