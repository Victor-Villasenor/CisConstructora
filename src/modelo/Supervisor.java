package modelo;

/**
 * Persona que trabaja en la obra. Solo ve la obra que tiene asignada.
 */
public class Supervisor extends Cuenta {

    public Supervisor(String correo, String contrasena, String rol, String nombre) {
        super(correo, contrasena, rol, nombre);
    }

    @Override
    public String getRol() {
        return "Supervisor";
    }

    @Override
    public boolean gestionProyectos() {
        return false;
    }

    @Override
    public String getDescripcionRol() {
        return "Supervisor";
    }
}
