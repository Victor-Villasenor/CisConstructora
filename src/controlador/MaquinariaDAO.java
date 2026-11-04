package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.conexionBD;
import modelo.Maquinaria;

public class MaquinariaDAO implements BaseDAO<Maquinaria> {

    @Override
    public boolean registrar(Maquinaria maq) {
        String sql = "INSERT INTO maquinaria (descripcion, tipo, estado, ultimo_mantenimiento, costo, id_proyecto) VALUES (?, ?, ?, CURDATE(), ?, ?)";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, maq.getDescripcion());
            ps.setString(2, maq.getTipo());
            ps.setString(3, maq.getEstado());
            ps.setDouble(4, maq.getCosto());
            if (maq.getIdProyecto() != null) {
                ps.setInt(5, maq.getIdProyecto());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
            return true;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al registrar maquinaria: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Maquinaria> listar() {
        List<Maquinaria> lista = new ArrayList<>();
        String sql = "SELECT * FROM maquinaria WHERE activo = 1";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al listar maquinaria: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean modificar(Maquinaria maq) {
        String sql = "UPDATE maquinaria SET descripcion = ?, tipo = ?, estado = ?, ultimo_mantenimiento = CURDATE() WHERE id_Maquinaria = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, maq.getDescripcion());
            ps.setString(2, maq.getTipo());
            ps.setString(3, maq.getEstado());
            ps.setInt(4, maq.getId());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al modificar maquinaria: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {

        String sql = "UPDATE maquinaria SET activo = 0 WHERE id_Maquinaria = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al eliminar maquinaria: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Maquinaria> buscar(String texto) {
        List<Maquinaria> lista = new ArrayList<>();
        String sql = "SELECT * FROM maquinaria WHERE activo = 1 AND (descripcion LIKE ? OR tipo LIKE ?)";
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
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al buscar maquinaria: " + e.getMessage());
        }
        return lista;
    }

    public Maquinaria buscarPorId(int id) {
        String sql = "SELECT * FROM maquinaria WHERE id_Maquinaria = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFila(rs);
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al buscar la maquinaria: " + e.getMessage());
        }
        return null;
    }

    public boolean asignarProyecto(int id, Integer idProyecto) {
        String sql = "UPDATE maquinaria SET id_proyecto = ? WHERE id_Maquinaria = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            if (idProyecto != null) {
                ps.setInt(1, idProyecto);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setInt(2, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al asignar el proyecto a la maquinaria: " + e.getMessage());
            return false;
        }
    }

    private Maquinaria mapearFila(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_Maquinaria");
        String descripcion = rs.getString("descripcion");
        String tipo = rs.getString("tipo");
        String estado = rs.getString("estado");
        java.sql.Date ultimoMantenimiento = rs.getDate("ultimo_mantenimiento");
        double costo = rs.getDouble("costo");
        int idProyecto = rs.getInt("id_proyecto");

        Maquinaria maq = new Maquinaria(descripcion, tipo, estado);
        maq.setId(id);
        maq.setUltimoMantenimiento(ultimoMantenimiento);
        maq.setCosto(costo);
        maq.setIdProyecto(rs.wasNull() ? null : idProyecto);
        return maq;
    }
}
