package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

import conexion.conexionBD;
import modelo.Empleado;

public class EmpleadoDAO implements BaseDAO<Empleado> {

    @Override
    public boolean registrar(Empleado emp) {
        String sql = "INSERT INTO empleados(nombre, telefono, direccion, cargo, estado, salario, ultima_actualizacion, proyecto_asignado) VALUES(?, ?, ?, ?, ?, ?, CURDATE(), ?)";

        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getTelefono());
            ps.setString(3, emp.getDireccion());
            ps.setString(4, emp.getCargo());
            ps.setString(5, emp.getEstado());
            ps.setDouble(6, emp.getSalario());
            ps.setString(7, emp.getProyectoAsignado() == null ? "" : emp.getProyectoAsignado());

            ps.executeUpdate();
            return true;

        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al registrar empleado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Empleado> listar() {
        List<Empleado> listaEmpleados = new ArrayList<>();
        String sql = "SELECT * FROM empleados WHERE activo = 1";

        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                listaEmpleados.add(mapearFila(rs));
            }

        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al listar empleados: " + e.getMessage());
        }

        return listaEmpleados;

    }

    @Override
    public boolean modificar(Empleado emp) {
        String sql = "UPDATE empleados SET nombre = ?, telefono = ?, direccion = ?, cargo = ?, estado = ?, salario = ?, ultima_actualizacion = CURDATE(), proyecto_asignado = ? WHERE id_Empleado = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getTelefono());
            ps.setString(3, emp.getDireccion());
            ps.setString(4, emp.getCargo());
            ps.setString(5, emp.getEstado());
            ps.setDouble(6, emp.getSalario());
            ps.setString(7, emp.getProyectoAsignado() == null ? "" : emp.getProyectoAsignado());
            ps.setInt(8, emp.getId());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al modificar empleado: " + e.getMessage());
            return false;
        }

    }

    @Override
    public boolean eliminar(int id) {

        String sql = "UPDATE empleados SET activo = 0 WHERE id_Empleado = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            int filasAfectadas = ps.executeUpdate();

            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al eliminar empleado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Empleado> buscar(String texto) {
        List<Empleado> listaEmpleados = new ArrayList<>();
        String sql = "SELECT * FROM empleados WHERE activo = 1 AND (nombre LIKE ? OR cargo LIKE ?)";

        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            String filtro = "%" + texto + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    listaEmpleados.add(mapearFila(rs));
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al buscar empleados: " + e.getMessage());
        }
        return listaEmpleados;
    }

    public boolean actualizarAsignacion(int id, String estado, String proyectoAsignado) {
        String sql = "UPDATE empleados SET estado = ?, proyecto_asignado = ? WHERE id_Empleado = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, estado);
            ps.setString(2, proyectoAsignado == null ? "" : proyectoAsignado);
            ps.setInt(3, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al actualizar la asignación del empleado: " + e.getMessage());
            return false;
        }
    }

    private Empleado mapearFila(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_Empleado");
        String nombre = rs.getString("nombre");
        String telefono = rs.getString("telefono");
        String direccion = rs.getString("direccion");
        String cargo = rs.getString("cargo");
        String estado = rs.getString("estado");
        double salario = rs.getDouble("salario");
        java.sql.Date ultimaActualizacion = rs.getDate("ultima_actualizacion");
        String proyectoAsignado = rs.getString("proyecto_asignado");

        Empleado emp = new Empleado(nombre, telefono, direccion, cargo, estado, salario);
        emp.setId(id);
        emp.setUltimaActualizacion(ultimaActualizacion);
        emp.setProyectoAsignado(proyectoAsignado == null ? "" : proyectoAsignado);
        return emp;
    }

}
