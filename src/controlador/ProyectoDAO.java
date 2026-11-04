package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import conexion.conexionBD;
import modelo.Proyecto;

public class ProyectoDAO implements BaseDAO<Proyecto> {

    private static final String SELECT_BASE =
        "SELECT p.*, u.nombre AS supervisor_nombre " +
        "FROM proyectos p LEFT JOIN usuarios u ON u.id_Usuario = p.id_supervisor ";

    @Override
    public boolean registrar(Proyecto proy) {
        String sql = "INSERT INTO proyectos (nombre, ubicacion, estado, progreso, cliente_nombre, cliente_telefono, cliente_correo, id_supervisor) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, proy.getNombre());
                ps.setString(2, proy.getUbicacion());
                ps.setString(3, proy.getEstado());
                ps.setInt(4, proy.getProgreso());
                ps.setString(5, proy.getClienteNombre());
                ps.setString(6, proy.getClienteTelefono());
                ps.setString(7, proy.getClienteCorreo());
                if (proy.getIdSupervisor() == null) ps.setNull(8, Types.INTEGER);
                else ps.setInt(8, proy.getIdSupervisor());
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error al registrar proyecto: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Proyecto> listar() {
        return consultar(SELECT_BASE + "WHERE p.activo = 1 ORDER BY p.id_Proyecto");
    }

    @Override
    public boolean modificar(Proyecto proy) {
        String sql = "UPDATE proyectos SET nombre = ?, ubicacion = ?, estado = ?, progreso = ?, "
                   + "cliente_nombre = ?, cliente_telefono = ?, cliente_correo = ?, id_supervisor = ? "
                   + "WHERE id_Proyecto = ?";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, proy.getNombre());
                ps.setString(2, proy.getUbicacion());
                ps.setString(3, proy.getEstado());
                ps.setInt(4, proy.getProgreso());
                ps.setString(5, proy.getClienteNombre());
                ps.setString(6, proy.getClienteTelefono());
                ps.setString(7, proy.getClienteCorreo());
                if (proy.getIdSupervisor() == null) ps.setNull(8, Types.INTEGER);
                else ps.setInt(8, proy.getIdSupervisor());
                ps.setInt(9, proy.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al modificar proyecto: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "UPDATE proyectos SET activo = 0 WHERE id_Proyecto = ?";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar proyecto: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Proyecto> buscar(String texto) {
        String filtro = "%" + texto + "%";
        List<Proyecto> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE p.activo = 1 AND (p.nombre LIKE ? OR p.ubicacion LIKE ? OR p.cliente_nombre LIKE ?)";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, filtro);
                ps.setString(2, filtro);
                ps.setString(3, filtro);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar proyectos: " + e.getMessage());
        }
        return lista;
    }

    public Proyecto buscarPorId(int id) {
        String sql = SELECT_BASE + "WHERE p.id_Proyecto = ?";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapearFila(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar el proyecto: " + e.getMessage());
        }
        return null;
    }

    /** La obra que tiene asignada un Supervisor (la más reciente que siga activa). */
    public Proyecto buscarPorSupervisor(int idUsuario) {
        String sql = SELECT_BASE + "WHERE p.activo = 1 AND p.id_supervisor = ? ORDER BY p.id_Proyecto DESC LIMIT 1";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapearFila(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar la obra del supervisor: " + e.getMessage());
        }
        return null;
    }

    /** Guarda el avance que reporta el Supervisor. Al llegar a 100 la obra queda terminada. */
    public boolean actualizarAvance(int idProyecto, int progreso) {
        String estado = progreso >= 100 ? "Completado" : "En curso";
        String sql = "UPDATE proyectos SET progreso = ?, estado = ? WHERE id_Proyecto = ?";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, Math.max(0, Math.min(100, progreso)));
                ps.setString(2, estado);
                ps.setInt(3, idProyecto);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar el avance: " + e.getMessage());
            return false;
        }
    }

    private List<Proyecto> consultar(String sql) {
        List<Proyecto> lista = new ArrayList<>();
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar proyectos: " + e.getMessage());
        }
        return lista;
    }

    private Proyecto mapearFila(ResultSet rs) throws SQLException {
        Proyecto proy = new Proyecto(rs.getString("nombre"), rs.getString("ubicacion"),
                                     rs.getString("estado"), rs.getInt("progreso"));
        proy.setId(rs.getInt("id_Proyecto"));
        proy.setClienteNombre(rs.getString("cliente_nombre"));
        proy.setClienteTelefono(rs.getString("cliente_telefono"));
        proy.setClienteCorreo(rs.getString("cliente_correo"));
        int idSup = rs.getInt("id_supervisor");
        proy.setIdSupervisor(rs.wasNull() ? null : idSup);
        proy.setSupervisorNombre(rs.getString("supervisor_nombre"));
        return proy;
    }
}
