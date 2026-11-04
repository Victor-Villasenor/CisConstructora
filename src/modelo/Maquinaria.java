package modelo;

import java.sql.Date;

public class Maquinaria {
    private int id;
    private String descripcion;
    private String tipo;
    private String estado;
    private Date ultimoMantenimiento;
    private double costo;
    private Integer idProyecto;

    public Maquinaria(String descripcion, String tipo, String estado) {
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getUltimoMantenimiento() {
        return ultimoMantenimiento;
    }

    public void setUltimoMantenimiento(Date ultimoMantenimiento) {
        this.ultimoMantenimiento = ultimoMantenimiento;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public Integer getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Integer idProyecto) {
        this.idProyecto = idProyecto;
    }

}
