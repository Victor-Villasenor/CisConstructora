package modelo;

public abstract class Cuenta {
    private int id;
    private String correo;
    private String contrasena;
    private String rol;
    private String nombre;

    public Cuenta(String correo, String contrasena, String rol, String nombre) {
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.nombre = nombre;
    }

    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public abstract String getRol();

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    

    public abstract String getDescripcionRol();
    public abstract boolean gestionProyectos();

}
