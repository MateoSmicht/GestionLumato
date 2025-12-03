package modelo;

public class Cajero extends Usuario {
    public Cajero(String username, String password, String nombreCompleto) {
        super(username, password, nombreCompleto);
        this.funcionesPermitidas.add(Funcion.REGISTRAR_VENTA);
        this.funcionesPermitidas.add(Funcion.ABRIR_CAJA);
        this.funcionesPermitidas.add(Funcion.CERRAR_CAJA);
    }

    @Override
    public String getNombreRol() { return "Cajero"; }
}