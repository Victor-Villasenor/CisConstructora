package modelo;

public class Administrador extends Cuenta{

    public Administrador(String correo, String contrasena, String rol, String nombre) {
        super(correo, contrasena, rol, nombre);
    }

    

    @Override
    public String getRol() {      
        return "Administrador";
    }



    @Override
    public boolean gestionProyectos() {
        return false;
    }

    @Override
    public String getDescripcionRol() {
        return "Administrador";
    }
    
}
