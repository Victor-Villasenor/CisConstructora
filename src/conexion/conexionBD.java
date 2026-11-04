package conexion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class conexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/cisconstructora";
    private static final String USER = "root";
    private static final String PASSWORD = "tu_contrasena";

    private static boolean errorYaMostrado = false;

    public static Connection conectar(){
        Connection conexion = null;
        try{

            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexion exitosa a BD Cisconstructora... ");
            errorYaMostrado = false;

        }
        catch(ClassNotFoundException ex){
            System.out.println("Error: ¡No se encontro el driver JDBC en la carpeta lib!");
            mostrarErrorVisible(
                "No se encontró el driver de MySQL (mysql-connector-j).\n\n" +
                "Verifica que el .jar del conector esté dentro de la carpeta lib/\n" +
                "y agregado a las 'Referenced Libraries' del proyecto en VS Code."
            );
        }
        catch(SQLException ex){
            System.out.println("Error al conectar a MySQL: " + ex.getMessage());
            mostrarErrorVisible(
                "No se pudo conectar a la base de datos.\n\n" +
                "Detalle técnico: " + ex.getMessage() + "\n\n" +
                "Revisa:\n" +
                "1) Que MySQL / XAMPP esté corriendo.\n" +
                "2) Que exista una base de datos llamada 'cisconstructora'.\n" +
                "3) Que el usuario/contraseña en conexionBD.java sean correctos\n" +
                "   (ahora mismo: usuario 'root', contraseña 'tu_contrasena').\n" +
                "4) Que MySQL esté escuchando en el puerto 3306."
            );
        }
        return conexion;
    }

    private static void mostrarErrorVisible(String mensaje) {
        if (errorYaMostrado) return;
        errorYaMostrado = true;
        javax.swing.SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null, mensaje, "Error de conexión a la base de datos", JOptionPane.ERROR_MESSAGE)
        );
    }
}
