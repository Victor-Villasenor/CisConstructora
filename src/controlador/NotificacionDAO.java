package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.conexionBD;
import modelo.Notificacion;

public class NotificacionDAO {

    public boolean registrar(Notificacion notif) {
        String sql = "INSERT INTO notificaciones (mensaje, tipo, rol_destino, estado, leida, id_proyecto, monto, categoria, datos_solicitud) VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?)";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, notif.getMensaje());
            ps.setString(2, notif.getTipo());
            ps.setString(3, notif.getRolDestino());
            ps.setString(4, notif.getEstado());
            if (notif.getIdProyecto() != null) {
                ps.setInt(5, notif.getIdProyecto());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            if (notif.getMonto() != null) {
                ps.setDouble(6, notif.getMonto());
            } else {
                ps.setNull(6, java.sql.Types.DECIMAL);
            }
            ps.setString(7, notif.getCategoria());
            ps.setString(8, notif.getDatosSolicitud());
            ps.executeUpdate();
            return true;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al registrar notificación: " + e.getMessage());
            return false;
        }
    }

    public List<Notificacion> listarPorRol(String rol) {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones WHERE rol_destino = ? OR rol_destino = 'TODOS' ORDER BY fecha DESC";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, rol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al listar notificaciones: " + e.getMessage());
        }
        return lista;
    }

    public int contarNoLeidas(String rol) {
        String sql = "SELECT COUNT(*) FROM notificaciones WHERE (rol_destino = ? OR rol_destino = 'TODOS') AND leida = 0";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, rol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al contar notificaciones: " + e.getMessage());
        }
        return 0;
    }

    public boolean marcarTodasLeidas(String rol) {
        String sql = "UPDATE notificaciones SET leida = 1 WHERE (rol_destino = ? OR rol_destino = 'TODOS') AND leida = 0";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, rol);
            ps.executeUpdate();
            return true;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al marcar notificaciones como leídas: " + e.getMessage());
            return false;
        }
    }

    public boolean aprobar(int idNotificacion) {
        String sql = "UPDATE notificaciones SET estado = 'APROBADA', leida = 1 WHERE id_Notificacion = ?";
        return actualizarEstado(sql, idNotificacion);
    }

    public boolean rechazar(int idNotificacion) {
        String sql = "UPDATE notificaciones SET estado = 'RECHAZADA', leida = 1 WHERE id_Notificacion = ?";
        return actualizarEstado(sql, idNotificacion);
    }

    private boolean actualizarEstado(String sql, int idNotificacion) {
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idNotificacion);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al actualizar la notificación: " + e.getMessage());
            return false;
        }
    }

    private Notificacion mapearFila(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion(
            rs.getString("mensaje"),
            rs.getString("tipo"),
            rs.getString("rol_destino")
        );
        n.setId(rs.getInt("id_Notificacion"));
        n.setEstado(rs.getString("estado"));
        n.setLeida(rs.getBoolean("leida"));
        int idProyecto = rs.getInt("id_proyecto");
        n.setIdProyecto(rs.wasNull() ? null : idProyecto);
        double monto = rs.getDouble("monto");
        n.setMonto(rs.wasNull() ? null : monto);
        n.setFecha(rs.getTimestamp("fecha"));
        n.setCategoria(rs.getString("categoria"));
        n.setDatosSolicitud(rs.getString("datos_solicitud"));
        return n;
    }
}
