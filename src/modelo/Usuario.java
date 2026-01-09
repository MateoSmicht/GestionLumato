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
    
    public void agregarPermiso(Funcion funcion) {
        this.funcionesPermitidas.add(funcion);
    }

    public void quitarPermiso(Funcion funcion) {
        this.funcionesPermitidas.remove(funcion);
    }

    public boolean puede(Funcion funcion) {
        return this.funcionesPermitidas.contains(funcion);
    }
    
    public boolean validarPassword(String passwordIngresada) {
        return this.password.equals(passwordIngresada);
    }
    
    public boolean cambiarPassword(String passwordActual, String nuevaPassword) {
        // Solo permitimos el cambio si conoce su clave actual
        if (validarPassword(passwordActual)) {
            // Validación extra: Que la nueva no sea vacía
            if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
                this.password = nuevaPassword;
                return true; // Cambio exitoso
            }
        }
        return false; 
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
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (o == null || getClass() != o.getClass()) return false;
	    Usuario usuario = (Usuario) o;
	    // La identidad del usuario es su USERNAME (debe ser único)
	    return username != null && username.equals(usuario.username);
	}

	@Override
	public int hashCode() {
	    return username != null ? username.hashCode() : 0;
	}

	@Override
    public String toString() {
        return getNombreRol() + ": " + nombreCompleto;
    }
}