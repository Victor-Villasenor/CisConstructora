package modelo;

public class Dueno extends Cuenta{

    public Dueno(String correo, String contrasena, String rol, String nombre) {
        super(correo, contrasena, rol, nombre);
    }

    @Override
    public String getRol() {      
        return "Dueno";
    }

    @Override
    public boolean gestionProyectos() {
        return true;
    }

    @Override
    public String getDescripcionRol() {
        return "Dueno";
    }
    
}
