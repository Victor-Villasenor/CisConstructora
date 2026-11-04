package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.conexionBD;
import modelo.Administrador;
import modelo.Cuenta;
import modelo.Dueno;
import modelo.Supervisor;

public class CuentaDAO {

    /** Devuelve la cuenta si el correo y la contraseña son correctos; si no, null. */
    public Cuenta login(String correo, String contrasena) {
        String sql = "SELECT * FROM usuarios WHERE correo = ? AND contrasena = ? AND activo = 1";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, correo);
                ps.setString(2, contrasena);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Cuenta cuenta = crearCuenta(
                            rs.getString("rol"), rs.getString("correo"),
                            rs.getString("contrasena"), rs.getString("nombre"));
                        cuenta.setId(rs.getInt("id_Usuario"));
                        return cuenta;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al iniciar sesión: " + e.getMessage());
        }
        return null;
    }

    /** Cada rol de la base de datos se convierte en su clase. */
    private Cuenta crearCuenta(String rol, String correo, String contrasena, String nombre) {
        if ("DUENO".equalsIgnoreCase(rol)) return new Dueno(correo, contrasena, rol, nombre);
        if ("SUPERVISOR".equalsIgnoreCase(rol)) return new Supervisor(correo, contrasena, rol, nombre);
        return new Administrador(correo, contrasena, rol, nombre);
    }

    /** Cuentas de Supervisor activas (para elegir quién se encarga de una obra). */
    public List<Cuenta> listarSupervisores() {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE rol = 'SUPERVISOR' AND activo = 1 ORDER BY nombre";
        try (Connection con = conexionBD.conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cuenta c = crearCuenta(rs.getString("rol"), rs.getString("correo"),
                        rs.getString("contrasena"), rs.getString("nombre"));
                    c.setId(rs.getInt("id_Usuario"));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar supervisores: " + e.getMessage());
        }
        return lista;
    }
}
