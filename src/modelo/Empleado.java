package modelo;

import java.sql.Date;

public class Empleado extends Usuario {
    private String cargo;
    private String estado;
    private double salario;
    private Date ultimaActualizacion;
    private String proyectoAsignado;

    public Empleado(String nombre, String telefono, String direccion, String cargo, String estado, double salario) {
        super(nombre, telefono, direccion);
        this.cargo = cargo;
        this.estado = estado;
        this.salario = salario;
        this.proyectoAsignado = "";
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public Date getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(Date ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public String getProyectoAsignado() {
        return proyectoAsignado;
    }

    public void setProyectoAsignado(String proyectoAsignado) {
        this.proyectoAsignado = proyectoAsignado;
    }

}
