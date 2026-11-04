package vista;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

import controlador.ProyectoDAO;
import controlador.Sesion;
import modelo.Cuenta;
import modelo.Proyecto;

/**
 * Pantalla del Supervisor: ve únicamente la obra que tiene asignada
 * y reporta su avance.
 */
public class PanelSupervisor extends JFrame {

    private static final Color AZUL_OSCURO = new Color(4, 24, 40);
    private static final Color AZUL = new Color(6, 54, 83);
    private static final Color AZUL_CLARO = new Color(93, 170, 226);
    private static final Color DORADO = new Color(232, 163, 61);
    private static final Color FONDO = Tema.FONDO;
    private static final Color TEXTO = new Color(24, 40, 54);
    private static final Color TEXTO_SUAVE = new Color(108, 126, 140);
    private static final Color VERDE = new Color(34, 139, 74);

    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private Proyecto obra;
    private JSlider deslizador;
    private JLabel lblPorcentaje;
    private JLabel lblEstado;
    private JPanel tarjetaAvance;

    public PanelSupervisor() {
        setTitle("CisConstructora - Supervisor");
        Tema.instalar();
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Cuenta cuenta = Sesion.haySesion() ? Sesion.getUsuarioActual() : null;
        obra = cuenta == null ? null : proyectoDAO.buscarPorSupervisor(cuenta.getId());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        raiz.add(crearEncabezado(cuenta), BorderLayout.NORTH);
        raiz.add(Chrome.menuLateral("MIOBRA", m -> Chrome.abrir(this, m)), BorderLayout.WEST);
        raiz.add(obra == null ? crearSinObra() : crearContenido(), BorderLayout.CENTER);
        setContentPane(raiz);
        EstadoVentana.seguir(this);
        EstadoVentana.aplicar(this, true);
    }

    private static Font fuente(int estilo, float tam) {
        return Tema.fuente(estilo, tam);
    }

    private JPanel crearEncabezado(Cuenta cuenta) {
        return Chrome.encabezado(() -> 0, Chrome.accionesBasicas(this));
    }

    private JPanel crearSinObra() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel("<html><div style='text-align:center'><b style='font-size:20px'>Todavía no tienes una obra asignada</b>"
            + "<br><br>Cuando el Dueño te asigne una, la verás aquí<br>y podrás reportar su avance.</div></html>", SwingConstants.CENTER);
        l.setFont(fuente(Font.PLAIN, 15f));
        l.setForeground(TEXTO_SUAVE);
        p.add(l);
        return p;
    }

    private JPanel tarjeta() {
        JPanel t = new Tema.Tarjeta(22);
        t.setLayout(null);
        return t;
    }

    private JLabel texto(String s, int estilo, float tam, Color c, int x, int y, int w, int h) {
        JLabel l = new JLabel(s);
        l.setFont(fuente(estilo, tam));
        l.setForeground(c);
        l.setBounds(x, y, w, h);
        return l;
    }

    private JPanel crearContenido() {
        JPanel p = new JPanel(new GridLayout(1, 2, 24, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        // ---- Tarjeta 1: datos de la obra
        JPanel datos = tarjeta();
        datos.add(texto("MI OBRA", Font.BOLD, 11f, DORADO, 32, 28, 300, 16));
        datos.add(texto(obra.getNombre(), Font.BOLD, 24f, AZUL, 32, 48, 420, 34));
        String[][] filas = {
            {"Ubicación", obra.getUbicacion()},
            {"Cliente", obra.getClienteNombre().isEmpty() ? "—" : obra.getClienteNombre()},
            {"Teléfono del cliente", obra.getClienteTelefono().isEmpty() ? "—" : obra.getClienteTelefono()},
            {"Correo del cliente", obra.getClienteCorreo().isEmpty() ? "—" : obra.getClienteCorreo()},
        };
        int y = 110;
        for (String[] f : filas) {
            datos.add(texto(f[0], Font.BOLD, 12f, TEXTO_SUAVE, 32, y, 400, 16));
            datos.add(texto(f[1], Font.PLAIN, 16f, TEXTO, 32, y + 18, 440, 24));
            y += 66;
        }
        p.add(datos);

        // ---- Tarjeta 2: avance
        tarjetaAvance = tarjeta();
        tarjetaAvance.add(texto("AVANCE DE LA OBRA", Font.BOLD, 11f, DORADO, 32, 28, 300, 16));
        lblPorcentaje = texto(obra.getProgreso() + "%", Font.BOLD, 64f, AZUL, 32, 48, 300, 80);
        tarjetaAvance.add(lblPorcentaje);
        lblEstado = texto(obra.getEstado(), Font.BOLD, 14f, VERDE, 36, 130, 300, 20);
        tarjetaAvance.add(lblEstado);

        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(223, 230, 236));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                int w = (int) (getWidth() * deslizador.getValue() / 100.0);
                if (w > 0) {
                    g2.setPaint(new GradientPaint(0, 0, AZUL_CLARO, getWidth(), 0, AZUL));
                    g2.fill(new RoundRectangle2D.Float(0, 0, Math.max(w, getHeight()), getHeight(), getHeight(), getHeight()));
                }
                g2.dispose();
            }
        };
        barra.setOpaque(false);
        barra.setBounds(32, 170, 400, 16);

        deslizador = new JSlider(0, 100, obra.getProgreso());
        deslizador.setOpaque(false);
        deslizador.setMajorTickSpacing(25);
        deslizador.setMinorTickSpacing(5);
        deslizador.setPaintTicks(true);
        deslizador.setPaintLabels(true);
        deslizador.setFont(fuente(Font.PLAIN, 11f));
        deslizador.setBounds(26, 215, 412, 60);
        deslizador.addChangeListener(e -> {
            lblPorcentaje.setText(deslizador.getValue() + "%");
            barra.repaint();
        });
        tarjetaAvance.add(barra);
        tarjetaAvance.add(texto("Mueve la barra al porcentaje real de la obra", Font.PLAIN, 13f, TEXTO_SUAVE, 32, 190, 400, 18));
        tarjetaAvance.add(deslizador);

        JButton guardar = new JButton("Guardar avance") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, getModel().isRollover() ? new Color(14, 92, 140) : AZUL, 0, getHeight(), AZUL_OSCURO));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        guardar.setFont(fuente(Font.BOLD, 15f));
        guardar.setForeground(Color.WHITE);
        guardar.setContentAreaFilled(false);
        guardar.setBorderPainted(false);
        guardar.setFocusPainted(false);
        guardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        guardar.setBounds(32, 310, 400, 48);
        guardar.addActionListener(e -> guardarAvance());
        tarjetaAvance.add(guardar);
        p.add(tarjetaAvance);
        return p;
    }

    private void guardarAvance() {
        int valor = deslizador.getValue();
        if (proyectoDAO.actualizarAvance(obra.getId(), valor)) {
            obra.setProgreso(valor);
            obra.setEstado(valor >= 100 ? "Completado" : "En curso");
            lblEstado.setText(obra.getEstado());
            JOptionPane.showMessageDialog(this, valor >= 100
                ? "Avance guardado. La obra quedó marcada como terminada."
                : "Avance guardado: " + valor + "%.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el avance. Revisa la conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
