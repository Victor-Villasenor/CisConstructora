package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import conexion.conexionBD;
import modelo.Cubicacion;

/**
 * Cada vez que se guarda una cubicación se agrega un renglón nuevo y el anterior
 * queda como historial (activa = 0), con la fecha y el usuario que hizo el cambio.
 */
public class CubicacionDAO {

    private static final String SELECT_BASE =
        "SELECT c.*, u.nombre AS nombre_usuario FROM cubicaciones c " +
        "LEFT JOIN usuarios u ON u.id_Usuario = c.id_usuario ";

    public boolean guardar(Cubicacion c) {
        String desactivar = "UPDATE cubicaciones SET activa = 0 WHERE id_proyecto = ?";
        String insertar = "INSERT INTO cubicaciones (id_proyecto, id_usuario, m2_totales, ml_cimentacion, ancho_cimentacion, "
            + "alto_cimentacion, ml_muro, altura_muro, tipo_muro, piezas_m2, tipo_enjarre, espesor_techo_cm, calidad_techo, "
            + "agua_m2, arena_m2, cemento_m2, activa) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return false;
            con.setAutoCommit(false);
            try (PreparedStatement p1 = con.prepareStatement(desactivar);
                 PreparedStatement p2 = con.prepareStatement(insertar)) {
                p1.setInt(1, c.getIdProyecto());
                p1.executeUpdate();
                int i = 1;
                p2.setInt(i++, c.getIdProyecto());
                p2.setInt(i++, c.getIdUsuario());
                p2.setDouble(i++, c.getM2Totales());
                p2.setDouble(i++, c.getMlCimentacion());
                p2.setDouble(i++, c.getAnchoCimentacion());
                p2.setDouble(i++, c.getAlturaCimentacion());
                p2.setDouble(i++, c.getMlMuro());
                p2.setDouble(i++, c.getAlturaMuro());
                p2.setString(i++, c.getTipoMuro());
                p2.setDouble(i++, c.getPiezasPorM2());
                p2.setString(i++, c.getTipoEnjarre());
                p2.setDouble(i++, c.getEspesorTechoCm());
                p2.setInt(i++, c.getCalidadTecho());
                p2.setDouble(i++, c.getAguaPorM2());
                p2.setDouble(i++, c.getArenaPorM2());
                p2.setDouble(i++, c.getCementoPorM2());
                p2.executeUpdate();
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al guardar la cubicación: " + e.getMessage());
            return false;
        }
    }

    /** La cubicación vigente de la obra, o null si todavía no tiene. */
    public Cubicacion obtenerActiva(int idProyecto) {
        String sql = SELECT_BASE + "WHERE c.id_proyecto = ? AND c.activa = 1 ORDER BY c.id_Cubicacion DESC LIMIT 1";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProyecto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapear(rs);
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al buscar la cubicación: " + e.getMessage());
        }
        return null;
    }

    /** Todas las versiones de la obra, de la más nueva a la más vieja. */
    public List<Cubicacion> historial(int idProyecto) {
        List<Cubicacion> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.id_proyecto = ? ORDER BY c.id_Cubicacion DESC";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProyecto);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error al leer el historial de cubicación: " + e.getMessage());
        }
        return lista;
    }

    private Cubicacion mapear(ResultSet rs) throws SQLException {
        Cubicacion c = new Cubicacion();
        c.setId(rs.getInt("id_Cubicacion"));
        c.setIdProyecto(rs.getInt("id_proyecto"));
        c.setIdUsuario(rs.getInt("id_usuario"));
        c.setNombreUsuario(rs.getString("nombre_usuario"));
        Timestamp t = rs.getTimestamp("fecha");
        if (t != null) c.setFecha(t.toLocalDateTime());
        c.setM2Totales(rs.getDouble("m2_totales"));
        c.setMlCimentacion(rs.getDouble("ml_cimentacion"));
        c.setAnchoCimentacion(rs.getDouble("ancho_cimentacion"));
        c.setAlturaCimentacion(rs.getDouble("alto_cimentacion"));
        c.setMlMuro(rs.getDouble("ml_muro"));
        c.setAlturaMuro(rs.getDouble("altura_muro"));
        c.setTipoMuro(rs.getString("tipo_muro"));
        c.setPiezasPorM2(rs.getDouble("piezas_m2"));
        c.setTipoEnjarre(rs.getString("tipo_enjarre"));
        c.setEspesorTechoCm(rs.getDouble("espesor_techo_cm"));
        c.setCalidadTecho(rs.getInt("calidad_techo"));
        c.setAguaPorM2(rs.getDouble("agua_m2"));
        c.setArenaPorM2(rs.getDouble("arena_m2"));
        c.setCementoPorM2(rs.getDouble("cemento_m2"));
        return c;
    }
}
