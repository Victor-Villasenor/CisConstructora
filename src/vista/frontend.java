package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import controlador.CuentaDAO;
import controlador.Sesion;
import modelo.Cuenta;

/**
 * Pantalla de inicio de sesión de CisConstructora 2.0.
 *
 * Lado izquierdo: presentación del sistema con un plano de casa que se dibuja solo.
 * Lado derecho: formulario de acceso. Todo se acomoda al tamaño de la ventana.
 */
public class frontend extends JFrame {

    // ---------- Paleta (azul y dorado, igual que el logo) ----------
    static final Color AZUL_OSCURO   = new Color(4, 24, 40);
    static final Color AZUL          = new Color(6, 54, 83);
    static final Color AZUL_CLARO    = new Color(93, 170, 226);
    static final Color DORADO        = new Color(232, 163, 61);
    static final Color FONDO_FORM    = new Color(244, 247, 250);
    static final Color TEXTO         = new Color(24, 40, 54);
    static final Color TEXTO_SUAVE   = new Color(108, 126, 140);
    static final Color BORDE         = new Color(208, 217, 225);
    static final Color ROJO          = new Color(196, 55, 55);

    // Cuentas de demostración (las mismas que crea schema_actualizacion.sql).
    // Pon MOSTRAR_DEMO en false si no quieres que aparezcan en la pantalla.
    private static final boolean MOSTRAR_DEMO = true;

    private static String familia;

    static Font fuente(int estilo, float tamano) {
        if (familia == null) {
            familia = "SansSerif";
            String[] preferidas = {"Segoe UI", "Inter", "Helvetica Neue", "SF Pro Text", "Calibri"};
            java.util.Set<String> disponibles = new java.util.HashSet<>(
                java.util.Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String p : preferidas) {
                if (disponibles.contains(p)) { familia = p; break; }
            }
        }
        return new Font(familia, estilo, 12).deriveFont(estilo, tamano);
    }

    static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ---------- Imágenes ----------
    static ImageIcon obtenerImagen(Object contexto, String nombreArchivo) {
        try {
            String userDir = System.getProperty("user.dir");
            String[] posiblesRutas = {
                userDir + File.separator + "src" + File.separator + "vista" + File.separator + nombreArchivo,
                userDir + File.separator + "cisconstructora" + File.separator + "src" + File.separator + "vista" + File.separator + nombreArchivo,
                userDir + File.separator + "vista" + File.separator + nombreArchivo,
                "src" + File.separator + "vista" + File.separator + nombreArchivo
            };
            for (String ruta : posiblesRutas) {
                File archivo = new File(ruta);
                if (archivo.exists()) return new ImageIcon(archivo.getAbsolutePath());
            }
            java.net.URL url = contexto.getClass().getResource(nombreArchivo);
            if (url == null) url = contexto.getClass().getResource("/vista/" + nombreArchivo);
            if (url != null) return new ImageIcon(url);
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + nombreArchivo);
        }
        return null;
    }

    static Image escalar(Image img, int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(img, 0, 0, w, h, null);
        g2.dispose();
        return bi;
    }

    // =====================================================================
    //  PANEL IZQUIERDO: presentación + plano animado
    // =====================================================================
    static class PanelMarca extends JPanel {

        private final Image logo;
        private final javax.swing.Timer reloj;
        private double t = 0; // segundos

        // Plano de una casa habitación (8 x 10 m), en coordenadas 0..1
        private static final double[][] MUROS = {
            // contorno
            {0, 0, 1, 0}, {1, 0, 1, 1}, {1, 1, 0, 1}, {0, 1, 0, 0.62}, {0, 0.50, 0, 0},  // hueco 0.50-0.62 = puerta de entrada
            // recámara 1 | baño | recámara 2
            {0.36, 0, 0.36, 0.42}, {0.64, 0, 0.64, 0.42},
            // pasillo (con puertas a recámara 1, baño y recámara 2)
            {0, 0.42, 0.14, 0.42}, {0.26, 0.42, 0.43, 0.42}, {0.57, 0.42, 0.74, 0.42}, {0.86, 0.42, 1, 0.42},
            // sala | cocina
            {0.55, 0.42, 0.55, 0.50}, {0.55, 0.62, 0.55, 0.72},  // hueco 0.50-0.62 = puerta sala-cocina
            // cochera
            {0, 0.72, 0.30, 0.72}, {0.44, 0.72, 1, 0.72}
        };
        // Puertas: {bisagra x, bisagra y, extremo del hueco x, extremo y, hacia dónde abre x, y}
        private static final double[][] PUERTAS = {
            {0.00, 0.62, 0.00, 0.50, 1, 0},    // entrada principal -> sala
            {0.14, 0.42, 0.26, 0.42, 0, -1},   // recámara 1
            {0.43, 0.42, 0.57, 0.42, 0, -1},   // baño
            {0.86, 0.42, 0.74, 0.42, 0, -1},   // recámara 2
            {0.55, 0.62, 0.55, 0.50, 1, 0},    // sala -> cocina
            {0.30, 0.72, 0.44, 0.72, 0, -1}    // sala <-> cochera
        };
        private static final String[] CUARTOS = {"Recámara 1", "Baño", "Recámara 2", "Sala", "Cocina", "Cochera"};
        private static final double[][] CENTROS = {
            {0.18, 0.21}, {0.50, 0.21}, {0.82, 0.21}, {0.275, 0.57}, {0.775, 0.57}, {0.5, 0.86}
        };

        PanelMarca() {
            setOpaque(true);
            ImageIcon ic = obtenerImagen(this, "LogoSinFondo.png");
            logo = ic != null ? ic.getImage() : null;
            reloj = new javax.swing.Timer(33, e -> { t += 0.033; repaint(); });
        }

        @Override public void addNotify() { super.addNotify(); reloj.start(); }
        @Override public void removeNotify() { reloj.stop(); super.removeNotify(); }

        private static List<String> envolver(String texto, FontMetrics fm, int ancho) {
            List<String> lineas = new ArrayList<>();
            StringBuilder actual = new StringBuilder();
            for (String palabra : texto.split(" ")) {
                String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
                if (fm.stringWidth(prueba) > ancho && actual.length() > 0) {
                    lineas.add(actual.toString());
                    actual = new StringBuilder(palabra);
                } else {
                    actual = new StringBuilder(prueba);
                }
            }
            if (actual.length() > 0) lineas.add(actual.toString());
            return lineas;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();

            // Fondo
            g2.setPaint(new GradientPaint(0, 0, AZUL, w, h, AZUL_OSCURO));
            g2.fillRect(0, 0, w, h);

            // Brillos suaves
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(w * 0.95f, h * 0.05f), Math.max(w, h) * 0.55f,
                new float[]{0f, 1f}, new Color[]{new Color(93, 170, 226, 70), new Color(93, 170, 226, 0)}));
            g2.fillRect(0, 0, w, h);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(w * 0.05f, h * 1.0f), Math.max(w, h) * 0.5f,
                new float[]{0f, 1f}, new Color[]{new Color(232, 163, 61, 45), new Color(232, 163, 61, 0)}));
            g2.fillRect(0, 0, w, h);

            // Cuadrícula tipo plano
            double desp = (t * 5) % 40;
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 255, 255, 10));
            for (double x = -40 + desp; x < w; x += 40) g2.drawLine((int) x, 0, (int) x, h);
            for (double y = -40 + desp; y < h; y += 40) g2.drawLine(0, (int) y, w, (int) y);
            g2.setColor(new Color(255, 255, 255, 16));
            for (double x = -160 + desp * 4 % 160; x < w; x += 160) g2.drawLine((int) x, 0, (int) x, h);
            for (double y = -160 + desp * 4 % 160; y < h; y += 160) g2.drawLine(0, (int) y, w, (int) y);

            int margen = Math.max(40, Math.min(64, w / 14));
            int anchoTexto = Math.min(440, (int) (w * 0.56));

            // ---- Marca ----
            int y = margen;
            int tamLogo = 58;
            Tema.dibujarLogo(g2, margen, y, tamLogo, tamLogo);
            g2.setFont(fuente(Font.BOLD, 22f));
            g2.setColor(Color.WHITE);
            g2.drawString("CisConstructora", margen + tamLogo + 12, y + 29);
            // etiqueta 2.0
            String ver = "VERSIÓN 2.0";
            g2.setFont(fuente(Font.BOLD, 10.5f));
            FontMetrics fmv = g2.getFontMetrics();
            int vx = margen + tamLogo + 12, vy = y + 38;
            g2.setColor(new Color(232, 163, 61, 40));
            g2.fill(new RoundRectangle2D.Float(vx, vy, fmv.stringWidth(ver) + 18, 20, 20, 20));
            g2.setColor(DORADO);
            g2.drawString(ver, vx + 9, vy + 14);

            // ---- Titular ----
            int tamTitular = w > 900 ? 40 : 32;
            int ty = y + tamLogo + Math.max(36, h / 14);
            g2.setFont(fuente(Font.BOLD, tamTitular));
            FontMetrics fmt = g2.getFontMetrics();
            int interlinea = (int) (tamTitular * 1.18);
            String[] titular = {"Cada obra,", "con su material", "bien contado."};
            for (int i = 0; i < titular.length; i++) {
                g2.setColor(i == 2 ? DORADO : Color.WHITE);
                g2.drawString(titular[i], margen, ty + fmt.getAscent() + i * interlinea);
            }
            int fin = ty + fmt.getAscent() + 2 * interlinea + 24;

            // ---- Párrafo ----
            g2.setFont(fuente(Font.PLAIN, 15f));
            g2.setColor(new Color(200, 216, 228));
            FontMetrics fmp = g2.getFontMetrics();
            for (String linea : envolver(
                "Calcula la cubicación de cada obra y controla que no salga más material del que necesita. "
                + "Todo desde un solo lugar.", fmp, anchoTexto)) {
                g2.drawString(linea, margen, fin + fmp.getAscent());
                fin += (int) (fmp.getHeight() * 1.3);
            }
            fin += 18;

            // ---- Puntos clave ----
            String[] puntos = {
                "Cubicación automática a partir de las medidas",
                "Bloqueo de salidas que pasan del tope",
                "Herramientas, maquinaria y personal por obra"
            };
            g2.setFont(fuente(Font.PLAIN, 14.5f));
            FontMetrics fmk = g2.getFontMetrics();
            for (String p : puntos) {
                g2.setColor(new Color(232, 163, 61, 55));
                g2.fillOval(margen, fin, 22, 22);
                g2.setColor(DORADO);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float palomita = new Path2D.Float();
                palomita.moveTo(margen + 6, fin + 11.5);
                palomita.lineTo(margen + 10, fin + 15.5);
                palomita.lineTo(margen + 16.5, fin + 7.5);
                g2.draw(palomita);
                g2.setColor(Color.WHITE);
                g2.drawString(p, margen + 34, fin + 16);
                fin += 36;
            }

            // ---- Pie ----
            g2.setFont(fuente(Font.PLAIN, 12f));
            g2.setColor(new Color(150, 176, 196));
            g2.drawString("Universidad Politécnica de Sinaloa  ·  Ing. en TI e Innovación Digital  ·  Grupo 4-1",
                margen, h - 30);

            // ---- Plano que se dibuja solo ----
            int espacioX = margen + anchoTexto + 30;
            int pw = Math.min(310, w - espacioX - margen);
            if (pw >= 170 && h >= 560) {
                int ph = (int) (pw / 0.8);
                if (ph > h - 220) { ph = h - 220; pw = (int) (ph * 0.8); }
                int px = w - margen - pw;
                int py = Math.max(90, (h - ph) / 2);
                dibujarPlano(g2, px, py, pw, ph);
            }

            g2.dispose();
        }

        private void dibujarPlano(Graphics2D g2, int px, int py, int pw, int ph) {
            // Ciclo de 16 s: dibuja (0-8), se queda (8-13), se desvanece (13-16)
            double c = t % 16.0;
            double p = Math.min(1.0, c / 8.0);
            p = p * p * (3 - 2 * p); // suavizado
            float vida = c < 13 ? 1f : (float) Math.max(0, 1 - (c - 13) / 3.0);

            // longitud total
            double total = 0;
            double[] largos = new double[MUROS.length];
            for (int i = 0; i < MUROS.length; i++) {
                double dx = (MUROS[i][2] - MUROS[i][0]) * pw, dy = (MUROS[i][3] - MUROS[i][1]) * ph;
                largos[i] = Math.hypot(dx, dy);
                total += largos[i];
            }
            double restante = p * total;

            Composite anterior = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, vida));

            // relleno muy suave
            g2.setColor(new Color(93, 170, 226, (int) (18 * Math.min(1, p * 1.5))));
            g2.fillRect(px, py, pw, ph);

            double puntaX = px, puntaY = py;
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < MUROS.length && restante > 0; i++) {
                double x1 = px + MUROS[i][0] * pw, y1 = py + MUROS[i][1] * ph;
                double x2 = px + MUROS[i][2] * pw, y2 = py + MUROS[i][3] * ph;
                double fr = Math.min(1.0, restante / largos[i]);
                double xe = x1 + (x2 - x1) * fr, ye = y1 + (y2 - y1) * fr;
                g2.setColor(new Color(150, 205, 245));
                g2.drawLine((int) Math.round(x1), (int) Math.round(y1), (int) Math.round(xe), (int) Math.round(ye));
                puntaX = xe; puntaY = ye;
                restante -= largos[i];
            }

            // punta dorada mientras dibuja
            if (p < 1.0) {
                g2.setColor(new Color(232, 163, 61, 90));
                g2.fillOval((int) puntaX - 9, (int) puntaY - 9, 18, 18);
                g2.setColor(DORADO);
                g2.fillOval((int) puntaX - 4, (int) puntaY - 4, 8, 8);
            }

            // nombres de cuartos y medidas al terminar
            float aparece = (float) Math.max(0, Math.min(1, (p - 0.85) / 0.15));
            if (aparece > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, vida * aparece));
                g2.setFont(fuente(Font.PLAIN, Math.max(10f, pw / 27f)));
                FontMetrics fm = g2.getFontMetrics();
                // puertas: hoja dorada abierta + arco de giro
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(DORADO);
                for (double[] d : PUERTAS) {
                    double hx = px + d[0] * pw, hy = py + d[1] * ph;
                    double ex = px + d[2] * pw, ey = py + d[3] * ph;
                    double r = Math.hypot(ex - hx, ey - hy);
                    double tx = hx + d[4] * r, ty = hy + d[5] * r;
                    g2.draw(new java.awt.geom.Line2D.Double(hx, hy, tx, ty));
                    double a0 = Math.toDegrees(Math.atan2(-(ey - hy), ex - hx));
                    double a1 = Math.toDegrees(Math.atan2(-(ty - hy), tx - hx));
                    double ext = a1 - a0;
                    while (ext > 180) ext -= 360;
                    while (ext < -180) ext += 360;
                    g2.draw(new java.awt.geom.Arc2D.Double(hx - r, hy - r, r * 2, r * 2, a0, ext, java.awt.geom.Arc2D.OPEN));
                }
                g2.setFont(fuente(Font.BOLD, 10f));
                g2.drawString("Entrada", (float) (px + 8), (float) (py + 0.62 * ph + 14));
                g2.setFont(fuente(Font.PLAIN, Math.max(10f, pw / 27f)));
                g2.setColor(new Color(200, 225, 245));
                for (int i = 0; i < CUARTOS.length; i++) {
                    int cx = px + (int) (CENTROS[i][0] * pw);
                    int cy = py + (int) (CENTROS[i][1] * ph);
                    g2.drawString(CUARTOS[i], cx - fm.stringWidth(CUARTOS[i]) / 2, cy + fm.getAscent() / 2);
                }

                // medidas
                g2.setFont(fuente(Font.BOLD, 11f));
                fm = g2.getFontMetrics();
                g2.setColor(DORADO);
                g2.setStroke(new BasicStroke(1.2f));
                // ancho (8 m)
                int ly = py - 16;
                g2.drawLine(px, ly, px + pw, ly);
                g2.drawLine(px, ly - 4, px, ly + 4);
                g2.drawLine(px + pw, ly - 4, px + pw, ly + 4);
                String m1 = "8.00 m";
                g2.setColor(new Color(4, 24, 40));
                g2.fillRect(px + pw / 2 - fm.stringWidth(m1) / 2 - 6, ly - 9, fm.stringWidth(m1) + 12, 18);
                g2.setColor(DORADO);
                g2.drawString(m1, px + pw / 2 - fm.stringWidth(m1) / 2, ly + 4);
                // alto (10 m)
                int lx = px + pw + 16;
                g2.drawLine(lx, py, lx, py + ph);
                g2.drawLine(lx - 4, py, lx + 4, py);
                g2.drawLine(lx - 4, py + ph, lx + 4, py + ph);
                String m2 = "10.00 m";
                AffineTransformHolder.rotarTexto(g2, m2, lx, py + ph / 2, fm);
            }
            g2.setComposite(anterior);
        }
    }

    /** Pequeño apoyo para escribir texto girado 90° con fondo. */
    private static class AffineTransformHolder {
        static void rotarTexto(Graphics2D g2, String texto, int x, int y, FontMetrics fm) {
            java.awt.geom.AffineTransform original = g2.getTransform();
            g2.translate(x, y);
            g2.rotate(-Math.PI / 2);
            g2.setColor(new Color(4, 24, 40));
            g2.fillRect(-fm.stringWidth(texto) / 2 - 6, -9, fm.stringWidth(texto) + 12, 18);
            g2.setColor(DORADO);
            g2.drawString(texto, -fm.stringWidth(texto) / 2, 4);
            g2.setTransform(original);
        }
    }

    // =====================================================================
    //  CAMPOS DEL FORMULARIO
    // =====================================================================
    private static void pintarHint(JTextComponent c, Graphics g, String hint) {
        if (c.getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(new Color(150, 163, 174));
            g2.setFont(c.getFont());
            FontMetrics fm = g2.getFontMetrics();
            Insets in = c.getInsets();
            g2.drawString(hint, in.left, (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    static class CampoTexto extends JTextField {
        private final String hint;
        CampoTexto(String hint) { this.hint = hint; }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); pintarHint(this, g, hint); }
    }

    private static class CampoClave extends JPasswordField {
        private final String hint;
        CampoClave(String hint) { this.hint = hint; }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); pintarHint(this, g, hint); }
    }

    /** Caja redondeada con ícono a la izquierda y borde que se enciende al enfocar. */
    static class CajaCampo extends JPanel {
        private boolean enfocado = false;
        private boolean conError = false;
        private final int tipoIcono; // 0 = sobre, 1 = candado

        CajaCampo(JTextComponent campo, int tipoIcono, JComponent derecha) {
            super(new BorderLayout());
            this.tipoIcono = tipoIcono;
            setOpaque(false);
            setBorder(new EmptyBorder(0, 46, 0, 8));
            campo.setOpaque(false);
            campo.setBorder(null);
            campo.setFont(fuente(Font.PLAIN, 15f));
            campo.setForeground(TEXTO);
            campo.setCaretColor(AZUL);
            campo.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { enfocado = true; repaint(); }
                @Override public void focusLost(FocusEvent e) { enfocado = false; repaint(); }
            });
            add(campo, BorderLayout.CENTER);
            if (derecha != null) add(derecha, BorderLayout.EAST);
            setPreferredSize(new Dimension(400, 52));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        }

        void setError(boolean e) { conError = e; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            if (enfocado) {
                g2.setColor(new Color(93, 170, 226, 60));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
            }
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 14, 14));
            g2.setStroke(new BasicStroke(enfocado ? 1.8f : 1.2f));
            g2.setColor(conError ? ROJO : (enfocado ? AZUL_CLARO : BORDE));
            g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 14, 14));

            // ícono
            Color col = enfocado ? AZUL : new Color(140, 156, 168);
            g2.setColor(col);
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int ix = 16, iy = h / 2 - 8;
            if (tipoIcono == 0) {
                g2.draw(new RoundRectangle2D.Float(ix, iy + 1, 18, 14, 4, 4));
                Path2D.Float solapa = new Path2D.Float();
                solapa.moveTo(ix + 1.5, iy + 3);
                solapa.lineTo(ix + 9, iy + 9);
                solapa.lineTo(ix + 16.5, iy + 3);
                g2.draw(solapa);
            } else {
                g2.draw(new RoundRectangle2D.Float(ix + 1, iy + 7, 16, 10, 4, 4));
                g2.drawArc(ix + 4, iy, 10, 14, 0, 180);
                g2.fillOval(ix + 8, iy + 10, 3, 3);
            }
            g2.dispose();
        }
    }

    /** Botón del ojo para mostrar u ocultar la contraseña. */
    private static class BotonOjo extends JButton {
        private boolean visible = false;
        BotonOjo() {
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(36, 36));
            setToolTipText("Mostrar u ocultar contraseña");
        }
        boolean esVisible() { return visible; }
        void alternar() { visible = !visible; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.setColor(new Color(120, 138, 152));
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float ojo = new Path2D.Float();
            ojo.moveTo(cx - 10, cy);
            ojo.curveTo(cx - 5, cy - 8, cx + 5, cy - 8, cx + 10, cy);
            ojo.curveTo(cx + 5, cy + 8, cx - 5, cy + 8, cx - 10, cy);
            ojo.closePath();
            g2.draw(ojo);
            g2.fillOval(cx - 3, cy - 3, 6, 6);
            if (!visible) {
                g2.setColor(new Color(140, 60, 60));
                g2.drawLine(cx - 9, cy + 8, cx + 9, cy - 8);
            }
            g2.dispose();
        }
    }

    /** Botón principal con color que cambia suave al pasar el mouse. */
    static class BotonPrincipal extends JButton {
        private double hover = 0;
        private boolean dentro = false;
        private final javax.swing.Timer anim;

        BotonPrincipal(String texto) {
            super(texto);
            setFont(fuente(Font.BOLD, 15f));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(400, 50));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            anim = new javax.swing.Timer(15, e -> {
                hover += dentro ? 0.12 : -0.12;
                if (hover >= 1) { hover = 1; ((javax.swing.Timer) e.getSource()).stop(); }
                if (hover <= 0) { hover = 0; ((javax.swing.Timer) e.getSource()).stop(); }
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { dentro = true; anim.start(); }
                @Override public void mouseExited(MouseEvent e) { dentro = false; anim.start(); }
            });
        }

        private static Color mezclar(Color a, Color b, double f) {
            return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * f),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * f),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * f));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            boolean activo = isEnabled();
            Color c1 = activo ? mezclar(AZUL, new Color(14, 92, 140), hover) : new Color(150, 170, 186);
            Color c2 = activo ? mezclar(new Color(4, 40, 66), AZUL, hover) : new Color(150, 170, 186);
            g2.setColor(new Color(6, 54, 83, 50));
            g2.fill(new RoundRectangle2D.Float(2, 5 - (float) (2 * hover), w - 4, h - 5, 16, 16));
            g2.setPaint(new GradientPaint(0, 0, c1, 0, h, c2));
            g2.fill(new RoundRectangle2D.Float(0, 0 - (float) (2 * hover), w, h - 3, 16, 16));
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                (h - 3 - fm.getHeight()) / 2 + fm.getAscent() - (float) (2 * hover));
            g2.dispose();
        }
    }

    /** Pastilla con el nombre del rol: al hacer clic llena los datos de la cuenta de demostración. */
    private static class Pastilla extends JButton {
        private final Color punto;
        private boolean dentro = false;
        Pastilla(String texto, Color punto) {
            super(texto);
            this.punto = punto;
            setFont(fuente(Font.PLAIN, 13f));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            FontMetrics fm = getFontMetrics(getFont());
            setPreferredSize(new Dimension(fm.stringWidth(texto) + 42, 32));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { dentro = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { dentro = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(dentro ? new Color(232, 240, 247) : Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h));
            g2.setColor(dentro ? AZUL_CLARO : BORDE);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h));
            g2.setColor(punto);
            g2.fillOval(12, h / 2 - 4, 8, 8);
            g2.setColor(TEXTO);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 28, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // =====================================================================
    //  VENTANA
    // =====================================================================
    private final CampoTexto txtCorreo = new CampoTexto("correo@ejemplo.com");
    private final CampoClave txtClave = new CampoClave("Tu contraseña");
    private final BotonOjo btnOjo = new BotonOjo();
    private final CajaCampo cajaCorreo = new CajaCampo(txtCorreo, 0, null);
    private final CajaCampo cajaClave = new CajaCampo(txtClave, 1, btnOjo);
    private final JLabel lblError = new JLabel(" ");
    private final BotonPrincipal btnLogin = new BotonPrincipal("Iniciar sesión");

    public frontend() {
        setTitle("CisConstructora 2.0 - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        EstadoVentana.seguir(this);
        EstadoVentana.aplicar(this, true);

        Tema.instalar();
        if (Tema.logo() != null) setIconImage(Tema.escalar(Tema.logo(), 128, 128));

        PanelMarca marca = new PanelMarca();
        JPanel lado = construirFormulario();

        // Reparte la ventana en 58 % presentación y 42 % formulario
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

        getRootPane().setDefaultButton(btnLogin);
        SwingUtilities.invokeLater(() -> txtCorreo.requestFocusInWindow());
    }

    private JPanel construirFormulario() {
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(FONDO_FORM);

        JPanel tarjeta = new JPanel();
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setPreferredSize(new Dimension(400, 560));

        JLabel etiqueta = new JLabel("ACCESO AL SISTEMA");
        etiqueta.setFont(fuente(Font.BOLD, 12f));
        etiqueta.setForeground(new Color(196, 132, 30));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Bienvenido de nuevo");
        titulo.setFont(fuente(Font.BOLD, 30f));
        titulo.setForeground(TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("<html>Entra con la cuenta que te dio tu constructora.</html>");
        subtitulo.setFont(fuente(Font.PLAIN, 14f));
        subtitulo.setForeground(TEXTO_SUAVE);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCorreo = etiquetaCampo("Correo");
        JLabel lblClave = etiquetaCampo("Contraseña");

        cajaCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cajaClave.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblError.setFont(fuente(Font.PLAIN, 13f));
        lblError.setForeground(ROJO);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnOlvide = new JButton("¿Olvidaste tu contraseña?");
        btnOlvide.setFont(fuente(Font.BOLD, 13f));
        btnOlvide.setForeground(AZUL);
        btnOlvide.setContentAreaFilled(false); btnOlvide.setBorderPainted(false); btnOlvide.setFocusPainted(false);
        btnOlvide.setMargin(new Insets(0, 0, 0, 0));
        btnOlvide.setBorder(BorderFactory.createEmptyBorder());
        btnOlvide.setHorizontalAlignment(SwingConstants.LEFT);
        btnOlvide.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOlvide.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnOlvide.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnOlvide.setForeground(AZUL_CLARO); }
            @Override public void mouseExited(MouseEvent e) { btnOlvide.setForeground(AZUL); }
        });
        btnOlvide.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            RecuperarContrasena ventana = new RecuperarContrasena(frontend.this);
            ventana.setVisible(true);
            setVisible(false);
        }));

        tarjeta.add(etiqueta);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(30));
        tarjeta.add(lblCorreo);
        tarjeta.add(Box.createVerticalStrut(7));
        tarjeta.add(cajaCorreo);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(lblClave);
        tarjeta.add(Box.createVerticalStrut(7));
        tarjeta.add(cajaClave);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(lblError);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(btnLogin);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(btnOlvide);

        if (MOSTRAR_DEMO) {
            tarjeta.add(Box.createVerticalStrut(30));
            JLabel demo = new JLabel("CUENTAS DE DEMOSTRACIÓN");
            demo.setFont(fuente(Font.BOLD, 11f));
            demo.setForeground(TEXTO_SUAVE);
            demo.setAlignmentX(Component.LEFT_ALIGNMENT);
            tarjeta.add(demo);
            tarjeta.add(Box.createVerticalStrut(10));

            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            fila.setOpaque(false);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            fila.add(pastilla("Dueño", DORADO, "dueno@cisconstructora.com", "dueno123"));
            fila.add(Box.createHorizontalStrut(8));
            fila.add(pastilla("Administrador", AZUL_CLARO, "admin@cisconstructora.com", "admin123"));
            fila.add(Box.createHorizontalStrut(8));
            fila.add(pastilla("Supervisor", new Color(76, 175, 120), "supervisor@cisconstructora.com", "supervisor123"));
            tarjeta.add(fila);
        }

        // Ojo de la contraseña
        txtClave.setEchoChar('•');
        btnOjo.addActionListener(e -> {
            btnOjo.alternar();
            txtClave.setEchoChar(btnOjo.esVisible() ? (char) 0 : '•');
        });

        // Quitar el mensaje de error cuando se vuelve a escribir
        javax.swing.event.DocumentListener limpiar = new javax.swing.event.DocumentListener() {
            void l() { if (!lblError.getText().trim().isEmpty()) mostrarError(null); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { l(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { l(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { l(); }
        };
        txtCorreo.getDocument().addDocumentListener(limpiar);
        txtClave.getDocument().addDocumentListener(limpiar);

        btnLogin.addActionListener(e -> iniciarSesion());

        lado.add(tarjeta);
        return lado;
    }

    private JLabel etiquetaCampo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, 13f));
        l.setForeground(TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private Pastilla pastilla(String texto, Color punto, String correo, String clave) {
        Pastilla p = new Pastilla(texto, punto);
        p.addActionListener(e -> {
            txtCorreo.setText(correo);
            txtClave.setText(clave);
            mostrarError(null);
            btnLogin.requestFocusInWindow();
        });
        return p;
    }

    private void mostrarError(String mensaje) {
        boolean hay = mensaje != null;
        lblError.setText(hay ? mensaje : " ");
        cajaCorreo.setError(hay);
        cajaClave.setError(hay);
    }

    private void iniciarSesion() {
        String correo = txtCorreo.getText().trim();
        String contrasena = new String(txtClave.getPassword()).trim();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Escribe tu correo y tu contraseña.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando…");

        // La consulta a MySQL se hace aparte para que la ventana no se congele.
        new SwingWorker<Cuenta, Void>() {
            @Override protected Cuenta doInBackground() {
                return new CuentaDAO().login(correo, contrasena);
            }
            @Override protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar sesión");
                try {
                    Cuenta cuenta = get();
                    if (cuenta == null) {
                        mostrarError("Correo o contraseña incorrectos.");
                        txtClave.requestFocusInWindow();
                        txtClave.selectAll();
                        return;
                    }
                    Sesion.iniciar(cuenta);
                    abrirPantallaInicial(cuenta);
                } catch (Exception ex) {
                    mostrarError("No se pudo iniciar sesión. Revisa la conexión a MySQL.");
                }
            }
        }.execute();
    }

    /** Cada rol entra a su propia pantalla. */
    private void abrirPantallaInicial(Cuenta cuenta) {
        SwingUtilities.invokeLater(() -> {
            String rol = cuenta.getRol();
            if ("Dueno".equals(rol)) {
                new Proyectos().setVisible(true);
            } else if ("Supervisor".equals(rol)) {
                new PanelSupervisor().setVisible(true);
            } else {
                new Inventario().setVisible(true);
            }
            frontend.this.dispose();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new frontend().setVisible(true));
    }
}
