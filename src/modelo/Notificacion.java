package modelo;

import java.sql.Timestamp;

public class Notificacion {
    private int id;
    private String mensaje;
    private String tipo;
    private String rolDestino;
    private String estado;
    private boolean leida;
    private Integer idProyecto;
    private Double monto;
    private Timestamp fecha;
    private String categoria;
    private String datosSolicitud;

    public Notificacion(String mensaje, String tipo, String rolDestino) {
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.rolDestino = rolDestino;
        this.estado = "PENDIENTE";
        this.leida = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRolDestino() {
        return rolDestino;
    }

    public void setRolDestino(String rolDestino) {
        this.rolDestino = rolDestino;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public Integer getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Integer idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDatosSolicitud() {
        return datosSolicitud;
    }

    public void setDatosSolicitud(String datosSolicitud) {
        this.datosSolicitud = datosSolicitud;
    }
}
