package vista;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Diseño compartido de CisConstructora: colores, tipografía, imágenes en buena
 * calidad, íconos dibujados con vectores (se ven nítidos a cualquier tamaño) y
 * estilos globales para campos, botones, listas desplegables y barras de scroll.
 */
public final class Tema {

    private Tema() { }

    // ------------------------------------------------------------------ colores
    public static final Color AZUL_OSCURO = new Color(4, 24, 40);
    public static final Color AZUL        = new Color(6, 54, 83);
    public static final Color AZUL_MEDIO  = new Color(14, 92, 140);
    public static final Color AZUL_CLARO  = new Color(93, 170, 226);
    public static final Color DORADO      = new Color(232, 163, 61);
    public static final Color FONDO       = new Color(238, 242, 246);
    public static final Color FONDO_FORM  = new Color(244, 247, 250);
    public static final Color TEXTO       = new Color(24, 40, 54);
    public static final Color TEXTO_SUAVE = new Color(108, 126, 140);
    public static final Color BORDE       = new Color(208, 217, 225);
    public static final Color ROJO        = new Color(196, 55, 55);
    public static final Color VERDE       = new Color(34, 139, 74);
    public static final Color FILA_PAR    = Color.WHITE;
    public static final Color FILA_IMPAR  = new Color(245, 248, 251);
    public static final Color FILA_HOVER  = new Color(230, 240, 249);
    public static final Color FILA_SELECC = new Color(209, 229, 246);

    // --------------------------------------------------------------- tipografía
    private static String familia;
    private static final Map<Integer, Font> CACHE_FUENTES = new HashMap<>();

    private static String familia() {
        if (familia == null) {
            java.util.List<String> disponibles = java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            familia = Font.SANS_SERIF;
            for (String n : new String[]{"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Roboto", "Arial"}) {
                if (disponibles.contains(n)) { familia = n; break; }
            }
        }
        return familia;
    }

    public static Font fuente(int estilo, float tamano) {
        int clave = estilo * 1000 + Math.round(tamano * 4);
        return CACHE_FUENTES.computeIfAbsent(clave, k -> new Font(familia(), estilo, 12).deriveFont(estilo, tamano));
    }

    // ----------------------------------------------------------------- imágenes
    private static final Map<String, BufferedImage> CACHE_IMG = new HashMap<>();
    private static final Map<String, BufferedImage> CACHE_ESCALADAS = new HashMap<>();

    /** Busca un archivo de imagen junto al código (src/vista) o dentro del classpath. */
    public static BufferedImage imagen(String nombre) {
        if (CACHE_IMG.containsKey(nombre)) return CACHE_IMG.get(nombre);
        BufferedImage img = null;
        try {
            String d = System.getProperty("user.dir");
            String s = File.separator;
            String[] rutas = {
                d + s + "src" + s + "vista" + s + nombre,
                d + s + "cisconstructora" + s + "src" + s + "vista" + s + nombre,
                d + s + "vista" + s + nombre,
                "src" + s + "vista" + s + nombre
            };
            for (String r : rutas) {
                File f = new File(r);
                if (f.exists()) { img = ImageIO.read(f); break; }
            }
            if (img == null) {
                URL u = Tema.class.getResource(nombre);
                if (u == null) u = Tema.class.getResource("/vista/" + nombre);
                if (u != null) img = ImageIO.read(u);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen: " + nombre);
        }
        CACHE_IMG.put(nombre, img);
        return img;
    }

    /**
     * Logo de la empresa. Si junto al código existe "CisConstructora.jpg" (el logo
     * original con fondo blanco) se usa ese y se le quita el fondo al cargarlo;
     * si no, se usa LogoSinFondo.png.
     */
    public static BufferedImage logo() {
        if (CACHE_IMG.containsKey("#logo")) return CACHE_IMG.get("#logo");
        BufferedImage res = null;
        for (String n : new String[]{"CisConstructora.png", "CisConstructora.jpg", "CisConstructora.jpeg"}) {
            BufferedImage orig = imagen(n);
            if (orig != null) {
                res = n.endsWith(".png") ? orig : quitarFondoBlanco(orig);
                break;
            }
        }
        if (res == null) res = imagen("LogoSinFondo.png");
        CACHE_IMG.put("#logo", res);
        return res;
    }

    /** Vuelve transparente el blanco de una imagen (con borde suave, sin halo blanco). */
    public static BufferedImage quitarFondoBlanco(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = src.getRGB(x, y);
                int a0 = (p >>> 24) & 0xFF;
                int r = (p >> 16) & 0xFF, g = (p >> 8) & 0xFF, b = p & 0xFF;
                int minimo = Math.min(r, Math.min(g, b));
                double a;
                if (minimo >= 248) a = 0;
                else if (minimo <= 214) a = 1;
                else a = (248 - minimo) / 34.0;
                a *= a0 / 255.0;
                if (a <= 0) { out.setRGB(x, y, 0); continue; }
                if (a < 1) { // quitar el tinte blanco de los pixeles del borde
                    r = clamp((int) Math.round((r - 255 * (1 - a)) / a));
                    g = clamp((int) Math.round((g - 255 * (1 - a)) / a));
                    b = clamp((int) Math.round((b - 255 * (1 - a)) / a));
                }
                out.setRGB(x, y, ((int) Math.round(a * 255) << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    /** Reduce una imagen por pasos (mitad a mitad) para que quede nítida y sin dientes. */
    public static BufferedImage escalar(BufferedImage src, int ancho, int alto) {
        if (src == null) return null;
        BufferedImage actual = src;
        int w = src.getWidth(), h = src.getHeight();
        while (w > ancho * 2 || h > alto * 2) {
            w = Math.max(ancho, w / 2);
            h = Math.max(alto, h / 2);
            actual = pasoEscala(actual, w, h);
        }
        if (w != ancho || h != alto) actual = pasoEscala(actual, ancho, alto);
        return actual;
    }

    private static BufferedImage pasoEscala(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
        g.dispose();
        return out;
    }

    /**
     * Dibuja una imagen respetando la escala real de la pantalla (monitores con zoom
     * de Windows al 125 %, 150 %...), así no se ve borrosa.
     */
    public static void dibujarImagen(Graphics2D g, BufferedImage src, String clave, int x, int y, int w, int h) {
        if (src == null) return;
        double k = g.getTransform().getScaleX();
        if (k < 1) k = 1;
        int pw = (int) Math.round(w * k), ph = (int) Math.round(h * k);
        String id = clave + "@" + pw + "x" + ph;
        BufferedImage esc = CACHE_ESCALADAS.get(id);
        if (esc == null) {
            esc = escalar(src, pw, ph);
            CACHE_ESCALADAS.put(id, esc);
        }
        g.drawImage(esc, x, y, w, h, null);
    }

    /** Dibuja el logo manteniendo sus proporciones dentro de un cuadro w x h. */
    public static void dibujarLogo(Graphics2D g, int x, int y, int w, int h) {
        BufferedImage l = logo();
        if (l == null) return;
        double r = Math.min((double) w / l.getWidth(), (double) h / l.getHeight());
        int dw = (int) Math.round(l.getWidth() * r), dh = (int) Math.round(l.getHeight() * r);
        dibujarImagen(g, l, "logo", x + (w - dw) / 2, y + (h - dh) / 2, dw, dh);
    }

    /** Icono Swing con el logo (para etiquetas). */
    public static Icon iconoLogo(int tam) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                dibujarLogo(g2, x, y, tam, tam);
                g2.dispose();
            }
            @Override public int getIconWidth() { return tam; }
            @Override public int getIconHeight() { return tam; }
        };
    }

    // ------------------------------------------------------------------- íconos
    /** Ícono vectorial dibujado en un cuadro de 24 x 24 escalado a "tam". */
    public static void icono(Graphics2D g0, String tipo, double x, double y, double tam, Color color) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        g.scale(tam / 24.0, tam / 24.0);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (tipo) {
            case "campana": {
                Path2D p = new Path2D.Double();
                p.moveTo(5.5, 17.5);
                p.curveTo(7, 16.5, 7.5, 14.5, 7.5, 11);
                p.curveTo(7.5, 8, 9.5, 6, 12, 6);
                p.curveTo(14.5, 6, 16.5, 8, 16.5, 11);
                p.curveTo(16.5, 14.5, 17, 16.5, 18.5, 17.5);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(12, 3.6, 12, 6));
                g.draw(new Arc2D.Double(9.8, 17.5, 4.4, 4, 180, 180, Arc2D.OPEN));
                break;
            }
            case "usuario": {
                g.draw(new Ellipse2D.Double(8.2, 4, 7.6, 7.6));
                g.draw(new Arc2D.Double(4.5, 13.5, 15, 14, 0, 180, Arc2D.OPEN));
                break;
            }
            case "inventario": {
                g.draw(new RoundRectangle2D.Double(3.5, 6.5, 17, 13.5, 3, 3));
                g.draw(new Line2D.Double(3.5, 11, 20.5, 11));
                g.draw(new Line2D.Double(9.5, 14.8, 14.5, 14.8));
                g.draw(new Line2D.Double(6, 3.8, 18, 3.8));
                break;
            }
            case "empleados": {
                g.draw(new Ellipse2D.Double(5.5, 5, 6.4, 6.4));
                g.draw(new Arc2D.Double(2.5, 14.2, 12.4, 12, 0, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(13.2, 5.3, 5.6, 5.6, 270, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(15.4, 14.2, 7, 9, 0, 130, Arc2D.OPEN));
                break;
            }
            case "maquinaria": {
                g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 8; i++) {
                    double a = Math.toRadians(i * 45);
                    g.draw(new Line2D.Double(12 + Math.cos(a) * 7.4, 12 + Math.sin(a) * 7.4,
                                             12 + Math.cos(a) * 10, 12 + Math.sin(a) * 10));
                }
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Ellipse2D.Double(5.2, 5.2, 13.6, 13.6));
                g.draw(new Ellipse2D.Double(9.2, 9.2, 5.6, 5.6));
                break;
            }
            case "obras": {
                g.draw(new RoundRectangle2D.Double(4, 4, 10, 16.5, 1.5, 1.5));
                g.draw(new Path2D.Double() {{ moveTo(14, 9.5); lineTo(19.5, 9.5); lineTo(19.5, 20.5); lineTo(14, 20.5); }});
                g.draw(new Line2D.Double(7, 8, 11, 8));
                g.draw(new Line2D.Double(7, 11.5, 11, 11.5));
                g.draw(new Line2D.Double(7, 15, 11, 15));
                g.draw(new Line2D.Double(2.5, 20.5, 21.5, 20.5));
                break;
            }
            case "calculadora": {
                g.draw(new RoundRectangle2D.Double(5, 3, 14, 18, 2.5, 2.5));
                g.draw(new RoundRectangle2D.Double(8, 6, 8, 3.6, 1, 1));
                for (int r = 0; r < 2; r++)
                    for (int c = 0; c < 3; c++)
                        g.fill(new Ellipse2D.Double(8.2 + c * 3.1, 12.2 + r * 3.4, 1.6, 1.6));
                break;
            }
            case "buscar": {
                g.draw(new Ellipse2D.Double(4.5, 4.5, 11, 11));
                g.draw(new Line2D.Double(13.6, 13.6, 19.5, 19.5));
                break;
            }
            case "flecha-abajo": {
                Path2D p = new Path2D.Double();
                p.moveTo(6, 9.5); p.lineTo(12, 15.5); p.lineTo(18, 9.5);
                g.draw(p);
                break;
            }
            case "salir": {
                Path2D p = new Path2D.Double();
                p.moveTo(10, 4.5); p.lineTo(5, 4.5); p.lineTo(5, 19.5); p.lineTo(10, 19.5);
                g.draw(p);
                g.draw(new Line2D.Double(9, 12, 20, 12));
                Path2D f = new Path2D.Double();
                f.moveTo(16, 8); f.lineTo(20, 12); f.lineTo(16, 16);
                g.draw(f);
                break;
            }
            default:
                break;
        }
        g.dispose();
    }

    public static Icon iconoSwing(String tipo, int tam, Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                icono((Graphics2D) g, tipo, x, y, tam, color);
            }
            @Override public int getIconWidth() { return tam; }
            @Override public int getIconHeight() { return tam; }
        };
    }

    // ---------------------------------------------------- piezas reutilizables
    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    public static Color mezclar(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
            Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
            Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Panel blanco con esquinas redondeadas y sombra suave; recorta lo que lleve dentro. */
    public static class Tarjeta extends JPanel {
        private final int radio;
        private Color relleno = Color.WHITE;
        public Tarjeta() { this(18); }
        public Tarjeta(int radio) {
            this.radio = radio;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 2));
        }
        public Tarjeta conRelleno(Color c) { relleno = c; return this; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth() - 2, h = getHeight() - 4;
            for (int i = 3; i >= 1; i--) {
                g2.setColor(new Color(6, 54, 83, 9));
                g2.fill(new RoundRectangle2D.Float(-i + 1, i, w + 2 * i - 2, h + 1, radio + i * 2, radio + i * 2));
            }
            g2.setColor(relleno);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radio, radio));
            g2.dispose();
        }
        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clip(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 4, radio, radio));
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    /** Botón de acción redondeado con degradado y efecto al pasar el mouse. */
    public static JButton botonAccion(String texto, Color color) {
        final boolean[] hover = {false};
        JButton b = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                Color c = hover[0] ? mezclar(color, Color.WHITE, 0.12f) : color;
                g2.setPaint(new GradientPaint(0, 0, c, 0, getHeight(), mezclar(c, Color.BLACK, 0.18f)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        b.setFont(fuente(Font.BOLD, 13f));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(Math.max(100, b.getFontMetrics(b.getFont()).stringWidth(texto) + 36), 36));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover[0] = true; b.repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover[0] = false; b.repaint(); }
        });
        return b;
    }

    /** Tarjeta de resumen (título pequeño + valor grande). Componente 0 = título, 1 = valor. */
    public static JPanel tarjetaKpi(String titulo, String valor) {
        String tl = titulo.toLowerCase();
        final Color acento = (tl.contains("bajo") || tl.contains("repar") || tl.contains("manten")) ? DORADO
            : (tl.contains("activ") || tl.contains("disponible") ? VERDE : AZUL_MEDIO);
        Tarjeta k = new Tarjeta(16) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                g2.setColor(acento);
                g2.fill(new RoundRectangle2D.Float(0, 14, 5, getHeight() - 4 - 28, 5, 5));
                g2.dispose();
            }
        };
        k.setLayout(new GridLayout(2, 1, 0, 0));
        k.setBorder(BorderFactory.createEmptyBorder(14, 24, 18, 12));
        k.setPreferredSize(new Dimension(230, 92));
        JLabel lt = new JLabel(titulo.toUpperCase());
        lt.setFont(fuente(Font.BOLD, 11f));
        lt.setForeground(TEXTO_SUAVE);
        JLabel lv = new JLabel(valor);
        lv.setFont(fuente(Font.BOLD, 22f));
        lv.setForeground(AZUL);
        k.add(lt);
        k.add(lv);
        return k;
    }

    /** Campo de búsqueda con lupa. */
    public static void estilizarBuscador(JTextField campo) {
        campo.setBorder(new BordeCampo(34));
    }

    // ----------------------------------------------------------------- tablas
    public static void estilizarTabla(JTable t, JScrollPane scroll) {
        t.setRowHeight(46);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(231, 236, 241));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFont(fuente(Font.PLAIN, 14f));
        t.setSelectionBackground(FILA_SELECC);
        t.setSelectionForeground(TEXTO);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(10, 44));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setOpaque(false);
                l.setForeground(Color.WHITE);
                l.setFont(fuente(Font.BOLD, 12.5f));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return l;
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, AZUL_MEDIO, 0, getHeight(), AZUL));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRect(getWidth() - 1, 10, 1, getHeight() - 20);
                g2.dispose();
                super.paintComponent(g);
            }
        });
        h.setOpaque(false);
        h.setBackground(AZUL);

        if (scroll != null) {
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setViewportBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(24);
        }
    }

    public static Color colorFila(int fila, boolean seleccionada, boolean hover) {
        if (seleccionada) return FILA_SELECC;
        if (hover) return FILA_HOVER;
        return fila % 2 == 0 ? FILA_PAR : FILA_IMPAR;
    }

    // ------------------------------------------------- estilos globales (UI)
    private static boolean instalado = false;

    public static void instalar() {
        if (instalado) return;
        instalado = true;

        Font base = fuente(Font.PLAIN, 13f);
        Font negrita = fuente(Font.BOLD, 13f);
        for (String k : new String[]{"Label.font", "TextField.font", "PasswordField.font", "FormattedTextField.font",
                "ComboBox.font", "Table.font", "List.font", "TextArea.font", "ToolTip.font", "CheckBox.font",
                "RadioButton.font", "Spinner.font", "OptionPane.messageFont", "PopupMenu.font", "MenuItem.font"}) {
            UIManager.put(k, base);
        }
        for (String k : new String[]{"Button.font", "OptionPane.buttonFont", "TableHeader.font", "TitledBorder.font"}) {
            UIManager.put(k, negrita);
        }

        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", FONDO_FORM);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Button.background", AZUL);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.caretForeground", AZUL);
        UIManager.put("TextField.selectionBackground", FILA_SELECC);
        UIManager.put("TextField.selectionForeground", TEXTO);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.disabledBackground", new Color(236, 240, 244));
        UIManager.put("ComboBox.disabledForeground", TEXTO_SUAVE);
        UIManager.put("ComboBox.selectionBackground", FILA_SELECC);
        UIManager.put("ComboBox.selectionForeground", TEXTO);
        UIManager.put("PopupMenu.background", Color.WHITE);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDE));
        UIManager.put("ToolTip.background", AZUL_OSCURO);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(5, 9, 5, 9));

        UIManager.put("TextField.border", new BordeCampo(10));
        UIManager.put("PasswordField.border", new BordeCampo(10));
        UIManager.put("FormattedTextField.border", new BordeCampo(10));
        UIManager.put("ComboBox.border", new BordeCampo(6));
        UIManager.put("ComboBoxUI", ListaDesplegableUI.class.getName());
        UIManager.put("ScrollBarUI", BarraScrollUI.class.getName());
        UIManager.put("ButtonUI", BotonUI.class.getName());
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());

        // repintar los campos al ganar o perder el foco para que se vea el borde azul
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", e -> {
            for (Object o : new Object[]{e.getOldValue(), e.getNewValue()}) {
                if (o instanceof JComponent) {
                    JComponent c = (JComponent) o;
                    Component p = c.getParent();
                    if (p instanceof JComboBox) p.repaint();
                    c.repaint();
                }
            }
        });
    }

    /** Borde redondeado de los campos; pinta las esquinas con el color del fondo. */
    public static class BordeCampo extends AbstractBorder {
        private final int izq;
        public BordeCampo(int izquierda) { this.izq = izquierda; }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(5, Math.max(izq, 10), 5, 10);
        }
        @Override public Insets getBorderInsets(Component c, Insets i) {
            Insets n = getBorderInsets(c);
            i.set(n.top, n.left, n.bottom, n.right);
            return i;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            Component p = c.getParent();
            if (p instanceof JViewport || p instanceof JComboBox) p = p.getParent();
            Color fondoPadre = p != null && p.getBackground() != null ? p.getBackground() : FONDO;
            Area fuera = new Area(new Rectangle2D.Double(x, y, w, h));
            fuera.subtract(new Area(new RoundRectangle2D.Double(x + 0.5, y + 0.5, w - 1, h - 1, 12, 12)));
            g2.setColor(fondoPadre);
            g2.fill(fuera);
            boolean enfocado = c.hasFocus() || (c.getParent() instanceof JComboBox && c.getParent().hasFocus());
            g2.setColor(enfocado ? AZUL_CLARO : BORDE);
            g2.setStroke(new BasicStroke(enfocado ? 1.8f : 1.2f));
            g2.draw(new RoundRectangle2D.Double(x + 0.8, y + 0.8, w - 1.6, h - 1.6, 12, 12));
            if (izq > 12 && c instanceof javax.swing.text.JTextComponent) {
                icono(g2, "buscar", x + 10, y + (h - 16) / 2.0, 16, enfocado ? AZUL : new Color(140, 156, 168));
            }
            g2.dispose();
        }
    }

    /** Botón estándar: redondeado, azul, con efecto al pasar el mouse. */
    public static class BotonUI extends BasicButtonUI {
        public static javax.swing.plaf.ComponentUI createUI(JComponent c) { return new BotonUI(); }
        @Override public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setRolloverEnabled(true);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override public void update(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            if (b.isContentAreaFilled()) {
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                Color base = b.getBackground();
                if (b.getModel().isPressed()) base = mezclar(base, Color.BLACK, 0.2f);
                else if (b.getModel().isRollover()) base = mezclar(base, Color.WHITE, 0.12f);
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 12, 12));
                g2.dispose();
            }
            paint(g, c);
        }
    }

    /** Lista desplegable sencilla con flecha propia. */
    public static class ListaDesplegableUI extends BasicComboBoxUI {
        public static javax.swing.plaf.ComponentUI createUI(JComponent c) { return new ListaDesplegableUI(); }
        @Override protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    icono(g2, "flecha-abajo", (getWidth() - 16) / 2.0, (getHeight() - 16) / 2.0, 16, TEXTO_SUAVE);
                    g2.dispose();
                }
            };
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder());
            return b;
        }
        @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(comboBox.isEnabled() ? Color.WHITE : new Color(236, 240, 244));
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        @Override public Dimension getMinimumSize(JComponent c) {
            Dimension d = super.getMinimumSize(c);
            return new Dimension(d.width, Math.max(d.height, 30));
        }
    }

    /** Barra de scroll delgada y redondeada. */
    public static class BarraScrollUI extends BasicScrollBarUI {
        public static javax.swing.plaf.ComponentUI createUI(JComponent c) { return new BarraScrollUI(); }
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(170, 186, 200);
            trackColor = new Color(0, 0, 0, 0);
        }
        @Override protected JButton createDecreaseButton(int o) { return cero(); }
        @Override protected JButton createIncreaseButton(int o) { return cero(); }
        private JButton cero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override public Dimension getPreferredSize(JComponent c) {
            return scrollbar.getOrientation() == JScrollBar.VERTICAL ? new Dimension(11, 10) : new Dimension(10, 11);
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(isThumbRollover() ? new Color(130, 150, 168) : thumbColor);
            g2.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
            g2.dispose();
        }
    }
}
