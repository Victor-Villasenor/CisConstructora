package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.conexionBD;
import modelo.Material;

public class MaterialDAO implements BaseDAO<Material> {

    @Override
    public boolean registrar(Material mat) {
        String sql = "INSERT INTO inventario (nombre, cantidad, unidad, precio, origen, ultima_actualizacion, id_proyecto) VALUES (?, ?, ?, ?, ?, CURDATE(), ?)";

        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, mat.getNombre());
            ps.setInt(2, mat.getCantidad());
            ps.setString(3, mat.getUnidad());
            ps.setDouble(4, mat.getPrecio());
            ps.setString(5, mat.getOrigen());
            if (mat.getIdProyecto() != null) {
                ps.setInt(6, mat.getIdProyecto());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
            return true;

        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al registrar en inventario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Material> listar() {
        List<Material> listaMaterials = new ArrayList<>();
        String sql = "SELECT * FROM inventario WHERE activo = 1";

        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                listaMaterials.add(mapearFila(rs));
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al listar el inventario: " + e.getMessage());
        }
        return listaMaterials;
    }

    @Override
    public boolean modificar(Material mat) {

        String sql = "UPDATE inventario SET nombre = ?, cantidad = ?, unidad = ?, precio = ?, origen = ?, ultima_actualizacion = CURDATE() WHERE id_Material = ?";

        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, mat.getNombre());
            ps.setInt(2, mat.getCantidad());
            ps.setString(3, mat.getUnidad());
            ps.setDouble(4, mat.getPrecio());
            ps.setString(5, mat.getOrigen());
            ps.setInt(6, mat.getId());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al modificar material en inventario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {

        String sql = "UPDATE inventario SET activo = 0 WHERE id_Material = ?";
        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            int filasAfectadas = ps.executeUpdate();

            return filasAfectadas > 0;
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al eliminar material del inventario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Material> buscar(String texto) {
        List<Material> listaMaterials = new ArrayList<>();
        String sql = "SELECT * FROM inventario WHERE activo = 1 AND (nombre LIKE ? OR origen LIKE ?)";

        try {
            Connection con = conexionBD.conectar();
            PreparedStatement ps = con.prepareStatement(sql);

            String filtro = "%" + texto + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaMaterials.add(mapearFila(rs));
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al buscar materiales: " + e.getMessage());
        }
        return listaMaterials;
    }

    private Material mapearFila(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_Material");
        String nombre = rs.getString("nombre");
        int cantidad = rs.getInt("cantidad");
        String unidad = rs.getString("unidad");
        double precio = rs.getDouble("precio");
        String origen = rs.getString("origen");
        java.sql.Date ultimaActualizacion = rs.getDate("ultima_actualizacion");
        int idProyecto = rs.getInt("id_proyecto");

        Material mat = new Material(nombre, cantidad, unidad, precio, origen);
        mat.setId(id);
        mat.setUltimaActualizacion(ultimaActualizacion);
        mat.setIdProyecto(rs.wasNull() ? null : idProyecto);
        return mat;
    }
}
