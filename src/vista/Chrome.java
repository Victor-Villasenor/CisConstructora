package vista;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.*;

import controlador.Sesion;
import modelo.Cuenta;

/**
 * Encabezado y menú lateral que comparten todas las pantallas del sistema.
 */
public final class Chrome {

    private Chrome() { }

    public interface Acciones {
        void inicio();
        void campana(Component origen, int x, int y);
        void perfil(Component origen, int x, int y);
    }

    public static boolean esSupervisor() {
        return Sesion.haySesion() && "Supervisor".equalsIgnoreCase(Sesion.getUsuarioActual().getRol());
    }

    public static boolean esDueno() {
        return Sesion.haySesion() && "Dueno".equalsIgnoreCase(Sesion.getUsuarioActual().getRol());
    }

    private static String nombreUsuario() {
        if (!Sesion.haySesion()) return "Invitado";
        String n = Sesion.getUsuarioActual().getNombre();
        return n == null || n.isBlank() ? "Usuario" : n;
    }

    private static String rolUsuario() {
        if (!Sesion.haySesion()) return "Sin sesión";
        String r = Sesion.getUsuarioActual().getRol();
        if ("Dueno".equalsIgnoreCase(r)) return "Dueño de la obra";
        if ("Supervisor".equalsIgnoreCase(r)) return "Supervisor";
        return "Administrador";
    }

    private static String iniciales(String nombre) {
        String[] p = nombre.trim().split("\\s+");
        String s = p[0].substring(0, 1);
        if (p.length > 1) s += p[p.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    /** Abre otra pantalla del sistema y cierra la actual. */
    public static void abrir(JFrame actual, String modulo) {
        try {
            JFrame nueva;
            switch (modulo) {
                case "INVENTARIO": nueva = new Inventario(); break;
                case "EMPLEADOS": nueva = new Empleados(); break;
                case "MAQUINARIA": nueva = new Maquinaria(); break;
                case "PROYECTOS": nueva = new Proyectos(); break;
                case "MIOBRA": nueva = new PanelSupervisor(); break;
                case "CUBICACION": nueva = new PantallaCubicacion(); break;
                default: return;
            }
            nueva.setVisible(true);
            actual.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(actual, "No se pudo abrir el módulo " + modulo + ".\n" + ex.getMessage(),
                "Error al abrir módulo", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Campana y perfil (cerrar sesión) para las pantallas del Supervisor. */
    public static Acciones accionesBasicas(JFrame ventana) {
        return new Acciones() {
            @Override public void inicio() { }
            @Override public void campana(Component origen, int x, int y) {
                JPopupMenu m = new JPopupMenu();
                JLabel l = new JLabel("<html><body style='width:200px'>No tienes notificaciones por ahora.</body></html>");
                l.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
                l.setForeground(Tema.TEXTO_SUAVE);
                m.add(l);
                m.show(origen, x - m.getPreferredSize().width, y + 6);
            }
            @Override public void perfil(Component origen, int x, int y) {
                JPopupMenu m = new JPopupMenu();
                JMenuItem salir = new JMenuItem("Cerrar sesión", Tema.iconoSwing("salir", 18, Tema.ROJO));
                salir.setFont(Tema.fuente(Font.BOLD, 13f));
                salir.setForeground(Tema.ROJO);
                salir.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 24));
                salir.addActionListener(e -> {
                    int op = JOptionPane.showConfirmDialog(ventana, "¿Seguro que quieres cerrar sesión?",
                        "Cerrar sesión", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (op == JOptionPane.YES_OPTION) {
                        Sesion.cerrar();
                        new frontend().setVisible(true);
                        ventana.dispose();
                    }
                });
                m.add(salir);
                m.show(origen, x - m.getPreferredSize().width, y + 6);
            }
        };
    }

    // ----------------------------------------------------------- encabezado
    public static JPanel encabezado(Supplier<Integer> noLeidas, Acciones acciones) {
        final int ALTO = 74;
        final Rectangle[] zonas = {new Rectangle(), new Rectangle(), new Rectangle()}; // logo, campana, usuario
        final int[] hover = {-1};

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, Tema.AZUL, w, 0, Tema.AZUL_OSCURO));
                g2.fillRect(0, 0, w, h);
                g2.setColor(Tema.DORADO);
                g2.fillRect(0, h - 3, w, 3);

                // logo + nombre
                zonas[0].setBounds(0, 0, 340, h - 3);
                if (hover[0] == 0) {
                    g2.setColor(new Color(255, 255, 255, 14));
                    g2.fillRect(0, 0, 340, h - 3);
                }
                Tema.dibujarLogo(g2, 20, 9, 54, 54);
                g2.setColor(Color.WHITE);
                g2.setFont(Tema.fuente(Font.BOLD, 20f));
                g2.drawString("CisConstructora", 84, 36);
                g2.setColor(Tema.DORADO);
                g2.setFont(Tema.fuente(Font.BOLD, 9.5f));
                g2.drawString("CONTROL DE OBRA  ·  v2.0", 85, 52);

                // zona derecha: campana + usuario
                String nombre = nombreUsuario(), rol = rolUsuario();
                g2.setFont(Tema.fuente(Font.BOLD, 13.5f));
                int wNombre = g2.getFontMetrics().stringWidth(nombre);
                g2.setFont(Tema.fuente(Font.PLAIN, 11.5f));
                int wRol = g2.getFontMetrics().stringWidth(rol);
                int wTexto = Math.max(wNombre, wRol);
                int bloqueW = 44 + 12 + wTexto + 14;
                int ux = w - 22 - bloqueW;
                zonas[2].setBounds(ux - 6, 10, bloqueW + 12, h - 23);
                if (hover[0] == 2) {
                    g2.setColor(new Color(255, 255, 255, 22));
                    g2.fill(new RoundRectangle2D.Float(zonas[2].x, zonas[2].y, zonas[2].width, zonas[2].height, 16, 16));
                }
                // avatar
                g2.setPaint(new GradientPaint(ux, 14, new Color(246, 190, 100), ux, 58, Tema.DORADO));
                g2.fill(new Ellipse2D.Float(ux, 15, 42, 42));
                g2.setColor(Tema.AZUL_OSCURO);
                g2.setFont(Tema.fuente(Font.BOLD, 15f));
                String ini = iniciales(nombre);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, ux + (42 - fm.stringWidth(ini)) / 2f, 15 + (42 - fm.getHeight()) / 2f + fm.getAscent());
                // textos
                g2.setColor(Color.WHITE);
                g2.setFont(Tema.fuente(Font.BOLD, 13.5f));
                g2.drawString(nombre, ux + 54, 33);
                g2.setColor(new Color(170, 205, 232));
                g2.setFont(Tema.fuente(Font.PLAIN, 11.5f));
                g2.drawString(rol, ux + 54, 50);
                // flechita
                Tema.icono(g2, "flecha-abajo", ux + 54 + wTexto + 2, 28, 14, new Color(170, 205, 232));

                // campana
                int bx = zonas[2].x - 20 - 40;
                zonas[1].setBounds(bx, 14, 44, 44);
                if (hover[0] == 1) {
                    g2.setColor(new Color(255, 255, 255, 26));
                    g2.fill(new Ellipse2D.Float(bx, 14, 44, 44));
                }
                Tema.icono(g2, "campana", bx + 10, 24, 24, Color.WHITE);
                int n = noLeidas.get();
                if (n > 0) {
                    g2.setColor(Tema.AZUL_OSCURO);
                    g2.fill(new Ellipse2D.Float(bx + 24, 12, 20, 20));
                    g2.setColor(new Color(224, 52, 52));
                    g2.fill(new Ellipse2D.Float(bx + 26, 14, 16, 16));
                    g2.setColor(Color.WHITE);
                    g2.setFont(Tema.fuente(Font.BOLD, 10f));
                    String t = n > 9 ? "9+" : String.valueOf(n);
                    FontMetrics f2 = g2.getFontMetrics();
                    g2.drawString(t, bx + 26 + (16 - f2.stringWidth(t)) / 2f, 14 + (16 - f2.getHeight()) / 2f + f2.getAscent());
                }
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(10, ALTO));
        p.setMinimumSize(new Dimension(10, ALTO));

        p.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int nuevo = -1;
                for (int i = 0; i < zonas.length; i++) if (zonas[i].contains(e.getPoint())) nuevo = i;
                if (nuevo != hover[0]) {
                    hover[0] = nuevo;
                    p.setCursor(nuevo >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    p.repaint();
                }
            }
        });
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hover[0] = -1; p.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (zonas[1].contains(e.getPoint())) {
                    acciones.campana(p, zonas[1].x + zonas[1].width, zonas[1].y + zonas[1].height - 4);
                } else if (zonas[2].contains(e.getPoint())) {
                    acciones.perfil(p, zonas[2].x + zonas[2].width, zonas[2].y + zonas[2].height - 2);
                } else if (zonas[0].contains(e.getPoint())) {
                    acciones.inicio();
                }
            }
        });
        return p;
    }

    // ------------------------------------------------------------ menú lateral
    /** @param activo "INVENTARIO", "EMPLEADOS", "MAQUINARIA" o "PROYECTOS" */
    public static JPanel menuLateral(String activo, Consumer<String> abrir) {
        final String[] nombres, etiquetas, iconos;
        if (esDueno()) {
            nombres = new String[]{"PROYECTOS"}; etiquetas = new String[]{"Obras"}; iconos = new String[]{"obras"};
        } else if (esSupervisor()) {
            nombres = new String[]{"MIOBRA", "CUBICACION"};
            etiquetas = new String[]{"Mi obra", "Cubicación"};
            iconos = new String[]{"obras", "calculadora"};
        } else {
            nombres = new String[]{"INVENTARIO", "EMPLEADOS", "MAQUINARIA"};
            etiquetas = new String[]{"Inventario", "Empleados", "Maquinaria"};
            iconos = new String[]{"inventario", "empleados", "maquinaria"};
        }
        final int ITEM = 92, ANCHO = 116, TOP = 18;
        final int[] hover = {-1};

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.setPaint(new GradientPaint(0, 0, new Color(6, 44, 70), 0, getHeight(), Tema.AZUL_OSCURO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());

                for (int i = 0; i < nombres.length; i++) {
                    int y = TOP + i * ITEM;
                    boolean act = nombres[i].equals(activo);
                    boolean hv = hover[0] == i;
                    if (act || hv) {
                        g2.setColor(new Color(255, 255, 255, act ? 24 : 12));
                        g2.fill(new RoundRectangle2D.Float(10, y + 4, ANCHO - 20, ITEM - 8, 16, 16));
                    }
                    if (act) {
                        g2.setColor(Tema.DORADO);
                        g2.fill(new RoundRectangle2D.Float(0, y + 18, 4, ITEM - 36, 4, 4));
                    }
                    Color c = act ? Tema.DORADO : (hv ? Color.WHITE : new Color(170, 200, 225));
                    Tema.icono(g2, iconos[i], (ANCHO - 32) / 2.0, y + 16, 32, c);
                    g2.setColor(c);
                    g2.setFont(Tema.fuente(Font.BOLD, 11.5f));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(etiquetas[i], (ANCHO - fm.stringWidth(etiquetas[i])) / 2f, y + 66);
                }
                g2.setColor(new Color(120, 150, 175));
                g2.setFont(Tema.fuente(Font.PLAIN, 10f));
                String v = "Versión 2.0";
                g2.drawString(v, (ANCHO - g2.getFontMetrics().stringWidth(v)) / 2f, getHeight() - 16);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(ANCHO, 10));

        p.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int i = (e.getY() - TOP) / ITEM;
                int nuevo = (e.getY() >= TOP && i >= 0 && i < nombres.length) ? i : -1;
                if (nuevo != hover[0]) {
                    hover[0] = nuevo;
                    p.setCursor(nuevo >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    p.repaint();
                }
            }
        });
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hover[0] = -1; p.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                int i = (e.getY() - TOP) / ITEM;
                if (e.getY() >= TOP && i >= 0 && i < nombres.length && !nombres[i].equals(activo)) {
                    abrir.accept(nombres[i]);
                }
            }
        });
        return p;
    }

    // ---------------------------------------------------------- títulos
    /** Título grande de la sección con subtítulo y una línea dorada corta. */
    public static JPanel titulo(String titulo, String subtitulo) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo);
        t.setFont(Tema.fuente(Font.BOLD, 28f));
        t.setForeground(Tema.AZUL);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = new JLabel(subtitulo);
        s.setFont(Tema.fuente(Font.PLAIN, 14f));
        s.setForeground(Tema.TEXTO_SUAVE);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(s);
        return p;
    }
}
