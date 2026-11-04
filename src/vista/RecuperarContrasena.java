package vista;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.Pattern;
import javax.swing.*;

/**
 * Pantalla para recuperar la contraseña, con el mismo diseño que el inicio de sesión.
 * Conserva la pantalla completa (o el tamaño) que tenía la ventana del login.
 */
public class RecuperarContrasena extends JFrame {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private final JFrame ventanaLogin;
    private final frontend.CampoTexto txtCorreo = new frontend.CampoTexto("correo@ejemplo.com");
    private final frontend.CajaCampo cajaCorreo = new frontend.CajaCampo(txtCorreo, 0, null);
    private final JLabel lblError = new JLabel(" ");
    private final frontend.BotonPrincipal btnEnviar = new frontend.BotonPrincipal("Enviar instrucciones");

    public RecuperarContrasena(JFrame login) {
        this.ventanaLogin = login;
        Tema.instalar();

        setTitle("CisConstructora 2.0 - Recuperar contraseña");
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 640));
        if (Tema.logo() != null) setIconImage(Tema.escalar(Tema.logo(), 128, 128));
        EstadoVentana.seguir(this);
        EstadoVentana.aplicar(this, true);

        if (ventanaLogin != null) {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { volverAlLogin(); }
            });
        } else {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        frontend.PanelMarca marca = new frontend.PanelMarca();
        JPanel lado = construirFormulario();

        JPanel raiz = new JPanel(null) {
            @Override public void doLayout() {
                int w = getWidth(), h = getHeight();
                int izq = (int) (w * 0.58);
                marca.setBounds(0, 0, izq, h);
                lado.setBounds(izq, 0, w - izq, h);
            }
        };
        raiz.add(marca);
        raiz.add(lado);
        setContentPane(raiz);

        getRootPane().setDefaultButton(btnEnviar);
        SwingUtilities.invokeLater(() -> txtCorreo.requestFocusInWindow());
    }

    private JPanel construirFormulario() {
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(frontend.FONDO_FORM);

        JPanel tarjeta = new JPanel();
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setPreferredSize(new Dimension(400, 460));

        JLabel etiqueta = new JLabel("RECUPERAR ACCESO");
        etiqueta.setFont(frontend.fuente(Font.BOLD, 12f));
        etiqueta.setForeground(new Color(196, 132, 30));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("¿Olvidaste tu contraseña?");
        titulo.setFont(frontend.fuente(Font.BOLD, 28f));
        titulo.setForeground(frontend.TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("<html>Escribe el correo de tu cuenta para recibir las instrucciones y restablecer tu contraseña.</html>");
        subtitulo.setFont(frontend.fuente(Font.PLAIN, 14f));
        subtitulo.setForeground(frontend.TEXTO_SUAVE);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCorreo = new JLabel("Correo");
        lblCorreo.setFont(frontend.fuente(Font.BOLD, 13f));
        lblCorreo.setForeground(frontend.TEXTO);
        lblCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cajaCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblError.setFont(frontend.fuente(Font.PLAIN, 13f));
        lblError.setForeground(frontend.ROJO);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnEnviar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEnviar.addActionListener(e -> enviar());

        JButton btnVolver = new JButton("←  Volver a iniciar sesión");
        btnVolver.setFont(frontend.fuente(Font.BOLD, 13f));
        btnVolver.setForeground(frontend.AZUL);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setMargin(new Insets(0, 0, 0, 0));
        btnVolver.setBorder(BorderFactory.createEmptyBorder());
        btnVolver.setHorizontalAlignment(SwingConstants.LEFT);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVolver.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnVolver.setForeground(frontend.AZUL_CLARO); }
            @Override public void mouseExited(MouseEvent e) { btnVolver.setForeground(frontend.AZUL); }
        });
        btnVolver.addActionListener(e -> volverAlLogin());

        tarjeta.add(etiqueta);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(32));
        tarjeta.add(lblCorreo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(cajaCorreo);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(lblError);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(btnEnviar);
        tarjeta.add(Box.createVerticalStrut(22));
        tarjeta.add(btnVolver);
        tarjeta.add(Box.createVerticalGlue());

        lado.add(tarjeta);
        return lado;
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje == null ? " " : mensaje);
        cajaCorreo.setError(mensaje != null);
    }

    private void enviar() {
        String correo = txtCorreo.getText().trim();
        if (correo.isEmpty()) {
            mostrarError("Escribe el correo de tu cuenta.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            mostrarError("El correo no tiene un formato válido. Ejemplo: usuario@dominio.com");
            return;
        }
        mostrarError(null);
        String mensajeHtml = "<html><body style='width: 280px;'>"
            + "<b>¡Se ha enviado un correo!</b><br><br>"
            + "Por favor, sigue los pasos que se te indicarán en el mensaje "
            + "para restablecer tu contraseña correctamente."
            + "</body></html>";
        JOptionPane.showMessageDialog(this, mensajeHtml, "Verifica tu correo", JOptionPane.INFORMATION_MESSAGE);
        volverAlLogin();
    }

    /** Regresa al login dejando la ventana igual (pantalla completa o tamaño normal). */
    private void volverAlLogin() {
        if (ventanaLogin instanceof JFrame) {
            EstadoVentana.aplicar(ventanaLogin, true);
            ventanaLogin.setVisible(true);
        }
        dispose();
    }
}
