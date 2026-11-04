package modelo;

/**
 * Una obra (casa habitación) registrada por el Dueño.
 * Guarda los datos del cliente y el Supervisor responsable.
 */
public class Proyecto {
    private int id;
    private String nombre;
    private String ubicacion;
    private String estado;
    private int progreso;

    private String clienteNombre = "";
    private String clienteTelefono = "";
    private String clienteCorreo = "";

    private Integer idSupervisor;
    private String supervisorNombre = "";

    public Proyecto(String nombre, String ubicacion, String estado, int progreso) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.progreso = progreso;
        setEstado(estado);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        if ("Completado".equalsIgnoreCase(estado)) {
            this.progreso = 100;
        }
    }

    public int getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso = progreso;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre == null ? "" : clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono == null ? "" : clienteTelefono;
    }

    public String getClienteCorreo() {
        return clienteCorreo;
    }

    public void setClienteCorreo(String clienteCorreo) {
        this.clienteCorreo = clienteCorreo == null ? "" : clienteCorreo;
    }

    /** Id de la cuenta del Supervisor responsable (null si todavía no tiene). */
    public Integer getIdSupervisor() {
        return idSupervisor;
    }

    public void setIdSupervisor(Integer idSupervisor) {
        this.idSupervisor = idSupervisor;
    }

    public String getSupervisorNombre() {
        return supervisorNombre;
    }

    public void setSupervisorNombre(String supervisorNombre) {
        this.supervisorNombre = supervisorNombre == null ? "" : supervisorNombre;
    }
}
