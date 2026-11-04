package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.conexionBD;
import modelo.Finanza;

public class FinanzaDAO implements BaseDAO<Finanza> {

    @Override
    public boolean registrar(Finanza fin) {
        String sql = "INSERT INTO finanzas (concepto, tipo, monto, fecha, id_proyecto) VALUES (?, ?, ?, CURDATE(), ?)";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, fin.getConcepto());
            ps.setString(2, fin.getTipo());
            ps.setDouble(3, fin.getMonto());
            if (fin.getIdProyecto() != null) {
                ps.setInt(4, fin.getIdProyecto());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al registrar movimiento financiero: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Finanza> listar() {
        List<Finanza> lista = new ArrayList<>();
        String sql = "SELECT * FROM finanzas WHERE activo = 1 ORDER BY fecha DESC, id_Finanza DESC";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar finanzas: " + e.getMessage());
        }
        return lista;
    }

    public List<Finanza> listarPorProyecto(int idProyecto) {
        List<Finanza> lista = new ArrayList<>();
        String sql = "SELECT * FROM finanzas WHERE activo = 1 AND id_proyecto = ? ORDER BY fecha DESC, id_Finanza DESC";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar finanzas del proyecto: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean modificar(Finanza fin) {
        String sql = "UPDATE finanzas SET concepto = ?, tipo = ?, monto = ?, fecha = CURDATE() WHERE id_Finanza = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, fin.getConcepto());
            ps.setString(2, fin.getTipo());
            ps.setDouble(3, fin.getMonto());
            ps.setInt(4, fin.getId());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.out.println("Error al modificar movimiento financiero: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {

        String sql = "UPDATE finanzas SET activo = 0 WHERE id_Finanza = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar movimiento financiero: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Finanza> buscar(String texto) {
        List<Finanza> lista = new ArrayList<>();
        String sql = "SELECT * FROM finanzas WHERE activo = 1 AND (concepto LIKE ? OR tipo LIKE ?)";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            String filtro = "%" + texto + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar movimientos financieros: " + e.getMessage());
        }
        return lista;
    }

    private Finanza mapearFila(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_Finanza");
        String concepto = rs.getString("concepto");
        String tipo = rs.getString("tipo");
        double monto = rs.getDouble("monto");
        java.sql.Date fecha = rs.getDate("fecha");
        int idProyecto = rs.getInt("id_proyecto");

        Finanza fin = new Finanza(concepto, tipo, monto);
        fin.setId(id);
        fin.setFecha(fecha);
        fin.setIdProyecto(rs.wasNull() ? null : idProyecto);
        return fin;
    }
}
