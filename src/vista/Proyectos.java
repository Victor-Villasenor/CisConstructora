package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import controlador.NotificacionDAO;
import controlador.ProyectoDAO;
import controlador.Sesion;
import modelo.Notificacion;
import modelo.Proyecto;

public class Proyectos extends JFrame {

    private boolean accesoDenegado = false;

    private final Color AZUL_HEADER = new Color(6, 54, 83);
    private final Color AMARILLO_MENU = new Color(175, 140, 21);
    private final Color AZUL_MARINO_FONDO = Tema.FONDO;
    private final Color GRIS_TARJETA = Color.WHITE;
    private final Color AZUL_BADGE = new Color(24, 90, 168);
    private final Color VERDE_BADGE = new Color(34, 139, 74);
    private final Color DORADO_BADGE = new Color(214, 158, 46);
    private final Color ROJO_ACCION = new Color(196, 60, 60);
    private final Color AZUL_ACCIONES = Tema.AZUL_MEDIO;
    private final Color AZUL_BOTON = new Color(117, 179, 231);

    private final String PLACEHOLDER_BUSCAR = "Buscar proyecto o ubicación...";

    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private List<Proyecto> proyectosActuales;

    private Image imgLogo;
    private Image imgCampana;
    private Image imgUsuario;
    private Image[] iconosMenu = new Image[3];
    private int notificacionesNoLeidas = 0;
    private JPanel panelHeaderRef;

    private JTable tablaProyectos;
    private CardLayout cardLayoutTabla;
    private JPanel panelTablaContenedor;
    private JPanel panelEstadoVacio;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscarProyecto;
    private JButton[] botonesResumen;
    private String filtroEstadoActivo = null;
    private int filaHover = -1;

    private ImageIcon obtenerImagen(String nombreArchivo) {
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
            URL url = getClass().getResource(nombreArchivo);
            if (url == null) url = getClass().getResource("/vista/" + nombreArchivo);
            if (url != null) return new ImageIcon(url);
        } catch (Exception e) {
            System.err.println("Error grave al intentar cargar: " + nombreArchivo);
        }
        return null;
    }

    private Image escalarConTransparencia(Image imagenOriginal, int ancho, int alto) {
        if (imagenOriginal == null) return null;
        BufferedImage imagenEscalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagenEscalada.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.Src);
        g2.drawImage(imagenOriginal, 0, 0, ancho, alto, null);
        g2.dispose();
        return imagenEscalada;
    }

    private Image cargarImagenEscalada(String nombreArchivo, int ancho, int alto) {
        ImageIcon icon = obtenerImagen(nombreArchivo);
        if (icon == null) return null;
        return escalarConTransparencia(icon.getImage(), ancho, alto);
    }

    private Icon crearIconoMini(String tipo, Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                switch (tipo) {
                    case "list":
                        g2.drawRoundRect(2, 2, size - 4, size - 4, 4, 4);
                        g2.drawLine(6, size / 2 - 4, size - 6, size / 2 - 4);
                        g2.drawLine(6, size / 2, size - 6, size / 2);
                        g2.drawLine(6, size / 2 + 4, size - 6, size / 2 + 4);
                        break;
                    case "doc":
                        g2.drawRoundRect(3, 2, size - 8, size - 4, 3, 3);
                        g2.drawLine(size - 5, 2, size - 5, 7);
                        g2.drawLine(size - 5, 7, size, 7);
                        g2.drawLine(6, size / 2 - 2, size - 8, size / 2 - 2);
                        g2.drawLine(6, size / 2 + 3, size - 8, size / 2 + 3);
                        break;
                    case "gear":
                        g2.drawOval(3, 3, size - 6, size - 6);
                        g2.drawOval(size / 2 - 3, size / 2 - 3, 6, 6);
                        break;
                    case "check":
                        g2.drawRoundRect(2, 2, size - 4, size - 4, 4, 4);
                        g2.drawLine(5, size / 2, size / 2 - 1, size - 6);
                        g2.drawLine(size / 2 - 1, size - 6, size - 5, 5);
                        break;
                    case "pencil":
                        g2.drawLine(4, size - 4, size - 6, 4);
                        g2.drawLine(size - 6, 4, size - 3, 7);
                        g2.drawLine(size - 3, 7, 6, size - 2);
                        g2.drawLine(4, size - 4, 6, size - 2);
                        break;
                }
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private void mostrarMenuNotificaciones(Component invoker, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(2, 0, 6, 0)
        ));

        JLabel lblTitulo = new JLabel("Notificaciones");
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 14f));
        lblTitulo.setForeground(AZUL_HEADER);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 16, 8, 16));
        popup.add(lblTitulo);
        popup.addSeparator();

        String rol = Sesion.haySesion() ? Sesion.getUsuarioActual().getRol() : "TODOS";
        List<Notificacion> notifs = notificacionDAO.listarPorRol(rol);

        if (notifs.isEmpty()) {
            JLabel lblMensaje = new JLabel("<html><body style='width:230px;'>Aquí aparecerán tus notificaciones.<br>Por el momento no tienes ninguna.</body></html>");
            lblMensaje.setFont(Tema.fuente(Font.PLAIN, 13f));
            lblMensaje.setForeground(new Color(110, 118, 125));
            lblMensaje.setBorder(BorderFactory.createEmptyBorder(10, 16, 12, 16));
            popup.add(lblMensaje);
        } else {
            int max = Math.min(notifs.size(), 8);
            for (int i = 0; i < max; i++) {
                Notificacion n = notifs.get(i);

                JPanel fila = new JPanel();
                fila.setLayout(new BoxLayout(fila, BoxLayout.Y_AXIS));
                fila.setBackground(Color.WHITE);
                fila.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
                fila.setAlignmentX(Component.LEFT_ALIGNMENT);
                fila.setMaximumSize(new Dimension(260, 200));

                JLabel lblMsg = new JLabel("<html><body style='width:220px;'>" + n.getMensaje() + "</body></html>");
                lblMsg.setFont(Tema.fuente(n.isLeida() ? Font.PLAIN : Font.BOLD, 12f));
                lblMsg.setForeground(new Color(40, 44, 50));
                fila.add(lblMsg);

                if (!"PENDIENTE".equals(n.getEstado())) {
                    JLabel lblEstado = new JLabel("Estado: " + n.getEstado());
                    lblEstado.setFont(Tema.fuente(Font.PLAIN, 10f));
                    lblEstado.setForeground(new Color(140, 148, 156));
                    fila.add(lblEstado);
                }

                popup.add(fila);
                if (i < max - 1) popup.addSeparator();
            }
        }

        popup.show(invoker, x - 260, y + 8);
    }

    private JButton crearBotonMenuPerfil(String texto, Color colorFondo, Color colorTexto, boolean habilitado) {
        Color colorHover = new Color(
            Math.max(0, colorFondo.getRed() - 20),
            Math.max(0, colorFondo.getGreen() - 20),
            Math.max(0, colorFondo.getBlue() - 20)
        );
        final boolean[] hover = {false};

        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor((hover[0] && isEnabled()) ? colorHover : colorFondo);
                float arco = getHeight();
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arco, arco));
                g2.setFont(getFont());
                g2.setColor(colorTexto);
                FontMetrics fm = g2.getFontMetrics();
                int x = 18;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        boton.setFont(Tema.fuente(Font.BOLD, 13f));
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setEnabled(habilitado);
        boton.setCursor(Cursor.getPredefinedCursor(habilitado ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(500, 40));
        boton.setPreferredSize(new Dimension(210, 40));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) { hover[0] = true; boton.repaint(); }
            }
            @Override
            public void mouseExited(MouseEvent e) { hover[0] = false; boton.repaint(); }
        });
        return boton;
    }

    private void mostrarMenuPerfil(Component invoker, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(240, 242, 245));
        popup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel panelMenu = new JPanel();
        panelMenu.setOpaque(false);
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));

        JLabel lblNombre = new JLabel("Victor Miranda");
        lblNombre.setFont(Tema.fuente(Font.BOLD, 16f));
        lblNombre.setForeground(new Color(25, 28, 32));
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMenu.add(lblNombre);

        JLabel lblRol = new JLabel("Administrador");
        lblRol.setFont(Tema.fuente(Font.PLAIN, 13f));
        lblRol.setForeground(new Color(130, 138, 146));
        lblRol.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMenu.add(lblRol);

        panelMenu.add(Box.createVerticalStrut(14));

        JSeparator separador = new JSeparator();
        separador.setForeground(new Color(215, 219, 224));
        separador.setAlignmentX(Component.LEFT_ALIGNMENT);
        separador.setMaximumSize(new Dimension(500, 1));
        panelMenu.add(separador);

        panelMenu.add(Box.createVerticalStrut(14));

        JButton btnProyectos = crearBotonMenuPerfil("Proyectos", new Color(222, 225, 229), new Color(60, 65, 70), true);
        btnProyectos.addActionListener(ev -> {
            new Proyectos().setVisible(true);
            dispose();
        });
        panelMenu.add(btnProyectos);

        panelMenu.add(Box.createVerticalStrut(10));

        JButton btnCerrarSesion = crearBotonMenuPerfil("Cerrar Sesión", new Color(222, 225, 229), new Color(196, 40, 40), true);
        btnCerrarSesion.addActionListener(ev -> {
            int opcion = JOptionPane.showConfirmDialog(
                this, "¿Estás seguro que deseas cerrar sesión?", "Cerrar sesión",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
            );
            if (opcion == JOptionPane.YES_OPTION) {
                new frontend().setVisible(true);
                dispose();
            }
        });
        panelMenu.add(btnCerrarSesion);

        popup.add(panelMenu);
        popup.show(invoker, x - 240, y + 8);
    }

    private void abrirModulo(String nombreBoton) {
        try {
            JFrame pantalla;
            switch (nombreBoton) {
                case "INVENTARIO": pantalla = new Inventario(); break;
                case "EMPLEADOS": pantalla = new Empleados(); break;
                case "MAQUINARIA": pantalla = new Maquinaria(); break;
                default: return;
            }
            pantalla.setVisible(true);
            this.setVisible(false);
            this.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo abrir el módulo " + nombreBoton + ".\n" + ex.getMessage(), "Error al abrir módulo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Color colorFila(int row, boolean isSelected) {
        return Tema.colorFila(row, isSelected, row == filaHover);
    }

    private JLabel crearBadgeEstado(String texto, Color color) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setOpaque(false);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(Tema.fuente(Font.BOLD, 12f));
        lbl.setBorder(new EmptyBorder(5, 14, 5, 14));
        return lbl;
    }

    private Color colorPorEstado(String estado) {
        if ("Completado".equals(estado)) return VERDE_BADGE;
        if ("Planificación".equals(estado)) return DORADO_BADGE;
        return AZUL_BADGE;
    }

    private class TextCellRenderer extends DefaultTableCellRenderer {
        private final boolean negrita;
        TextCellRenderer(boolean negrita) { this.negrita = negrita; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(colorFila(row, isSelected));
            setForeground(new Color(30, 35, 40));
            setFont(Tema.fuente(negrita ? Font.BOLD : Font.PLAIN, 15f));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    private class EstadoCellRenderer extends JPanel implements TableCellRenderer {
        EstadoCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 11));
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            String estado = value != null ? value.toString() : "";
            add(crearBadgeEstado(estado, colorPorEstado(estado)));
            setBackground(colorFila(row, isSelected));
            return this;
        }
    }

    private class ProgresoCellRenderer extends JPanel implements TableCellRenderer {
        private int progreso = 0;
        private Color colorBarra = AZUL_BADGE;
        private Color fondoCelda = Color.WHITE;

        ProgresoCellRenderer() { setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            progreso = (value instanceof Integer) ? (Integer) value : 0;
            Object estadoVal = table.getValueAt(row, 3);
            colorBarra = colorPorEstado(estadoVal != null ? estadoVal.toString() : "");
            fondoCelda = colorFila(row, isSelected);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondoCelda);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int barW = Math.max(30, getWidth() - 70);
            int barH = 10;
            int barX = 10;
            int barY = (getHeight() - barH) / 2;

            g2.setColor(new Color(220, 224, 228));
            g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, barH, barH));

            int relleno = (int) (barW * (progreso / 100.0));
            if (relleno > 0) {
                g2.setColor(colorBarra);
                g2.fill(new RoundRectangle2D.Float(barX, barY, relleno, barH, barH, barH));
            }

            g2.setColor(new Color(30, 35, 40));
            g2.setFont(Tema.fuente(Font.PLAIN, 13f));
            String texto = progreso + "%";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, getWidth() - fm.stringWidth(texto) - 6, (getHeight() + fm.getAscent()) / 2 - 2);

            g2.dispose();
        }
    }

    public Proyectos() {

        if (controlador.Sesion.haySesion() && "Administrador".equals(controlador.Sesion.getUsuarioActual().getRol())) {
            accesoDenegado = true;
            return;
        }

        Tema.instalar();
        setTitle("CisConstructora - Proyectos");
        setSize(1120, 820);
        setMinimumSize(new Dimension(1020, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(EstadoVentana.isMaximizada() ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
        addWindowStateListener(ev -> {
            boolean maximizadaAhora = (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
            EstadoVentana.setMaximizada(maximizadaAhora);
        });
        setLocationRelativeTo(null);
        setResizable(true);

        Tema.instalar();

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(AZUL_MARINO_FONDO);

        JPanel panelHeader = Chrome.encabezado(() -> notificacionesNoLeidas, new Chrome.Acciones() {
            @Override public void inicio() { /* ya estás en el inicio */ }
            @Override public void campana(Component origen, int x, int y) {
                mostrarMenuNotificaciones(origen, x, y);
                if (notificacionesNoLeidas > 0) {
                    notificacionDAO.marcarTodasLeidas(Sesion.haySesion() ? Sesion.getUsuarioActual().getRol() : "TODOS");
                    notificacionesNoLeidas = 0;
                    origen.repaint();
                }
            }
            @Override public void perfil(Component origen, int x, int y) { mostrarMenuPerfil(origen, x, y); }
        });
        panelHeaderRef = panelHeader;
        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        JPanel panelMenuLateral = Chrome.menuLateral("PROYECTOS", m -> Chrome.abrir(this, m));
        panelPrincipal.add(panelMenuLateral, BorderLayout.WEST);

        JPanel panelContenido = new JPanel();
        panelContenido.setBackground(AZUL_MARINO_FONDO);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBorder(new EmptyBorder(26, 34, 30, 34));

        JPanel panelTituloSeccion = Chrome.titulo("Obras", "Registra tus obras con los datos del cliente y su supervisor");
        panelTituloSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelTituloSeccion.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        panelContenido.add(panelTituloSeccion);
        panelContenido.add(Box.createVerticalStrut(20));

        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(6, 54, 83, 16));
                g2.fill(new RoundRectangle2D.Float(2, 3, getWidth() - 3, getHeight() - 3, 20, 20));
                g2.setColor(GRIS_TARJETA);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 20, 20));
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(24, 30, 24, 30));
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        JLabel lblListado = new JLabel("Listado de proyectos activos");
        lblListado.setIcon(crearIconoMini("list", new Color(20, 25, 30), 22));
        lblListado.setIconTextGap(10);
        lblListado.setFont(Tema.fuente(Font.BOLD, 20f));
        lblListado.setForeground(new Color(20, 25, 30));
        lblListado.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(lblListado);
        tarjeta.add(Box.createVerticalStrut(16));

        JPanel panelToolbar = new JPanel(new BorderLayout());
        panelToolbar.setOpaque(false);
        panelToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelToolbar.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        txtBuscarProyecto = new JTextField(PLACEHOLDER_BUSCAR);
        txtBuscarProyecto.setForeground(Color.GRAY);
        txtBuscarProyecto.setFont(Tema.fuente(Font.PLAIN, 13f));
        Tema.estilizarBuscador(txtBuscarProyecto);
        txtBuscarProyecto.setPreferredSize(new Dimension(300, 38));
        txtBuscarProyecto.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtBuscarProyecto.getText().equals(PLACEHOLDER_BUSCAR)) {
                    txtBuscarProyecto.setText("");
                    txtBuscarProyecto.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtBuscarProyecto.getText().isEmpty()) {
                    txtBuscarProyecto.setForeground(Color.GRAY);
                    txtBuscarProyecto.setText(PLACEHOLDER_BUSCAR);
                }
            }
        });
        txtBuscarProyecto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        JPanel panelBuscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelBuscador.setOpaque(false);
        panelBuscador.add(txtBuscarProyecto);
        panelToolbar.add(panelBuscador, BorderLayout.WEST);

        JPanel panelBotonesCrud = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotonesCrud.setOpaque(false);

        JButton btnModificarProyecto = crearBotonAccionProyecto("Modificar", AZUL_ACCIONES);
        btnModificarProyecto.addActionListener(e -> abrirModalModificarProyecto());
        panelBotonesCrud.add(btnModificarProyecto);

        JButton btnEliminarProyecto = crearBotonAccionProyecto("Eliminar", ROJO_ACCION);
        btnEliminarProyecto.addActionListener(e -> abrirModalEliminarProyecto());
        panelBotonesCrud.add(btnEliminarProyecto);

        JButton btnNuevoProyecto = crearBotonAccionProyecto("+ Nuevo", VERDE_BADGE);
        btnNuevoProyecto.addActionListener(e -> abrirModalAgregarProyecto());
        panelBotonesCrud.add(btnNuevoProyecto);

        panelToolbar.add(panelBotonesCrud, BorderLayout.EAST);
        tarjeta.add(panelToolbar);
        tarjeta.add(Box.createVerticalStrut(14));

        String[] columnas = {"ID", "Obra", "Ubicación", "Estado", "Progreso", "Cliente", "Supervisor"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? Integer.class : String.class;
            }
        };

        tablaProyectos = new JTable(modeloTabla);
        tablaProyectos.setRowHeight(42);
        tablaProyectos.setShowVerticalLines(false);
        tablaProyectos.setGridColor(new Color(225, 228, 232));
        tablaProyectos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaProyectos.setRowSelectionAllowed(true);
        tablaProyectos.setFont(Tema.fuente(Font.PLAIN, 15f));

        sorter = new TableRowSorter<>(modeloTabla);
        tablaProyectos.setRowSorter(sorter);

        tablaProyectos.getColumnModel().getColumn(0).setPreferredWidth(90);
        tablaProyectos.getColumnModel().getColumn(1).setPreferredWidth(260);
        tablaProyectos.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaProyectos.getColumnModel().getColumn(3).setPreferredWidth(130);
        tablaProyectos.getColumnModel().getColumn(4).setPreferredWidth(160);
        tablaProyectos.getColumnModel().getColumn(5).setPreferredWidth(170);
        tablaProyectos.getColumnModel().getColumn(6).setPreferredWidth(160);
        tablaProyectos.getColumnModel().getColumn(5).setCellRenderer(new TextCellRenderer(false));
        tablaProyectos.getColumnModel().getColumn(6).setCellRenderer(new TextCellRenderer(false));

        tablaProyectos.getColumnModel().getColumn(0).setCellRenderer(new TextCellRenderer(true));
        tablaProyectos.getColumnModel().getColumn(1).setCellRenderer(new TextCellRenderer(false));
        tablaProyectos.getColumnModel().getColumn(2).setCellRenderer(new TextCellRenderer(false));
        tablaProyectos.getColumnModel().getColumn(3).setCellRenderer(new EstadoCellRenderer());
        tablaProyectos.getColumnModel().getColumn(4).setCellRenderer(new ProgresoCellRenderer());

        JTableHeader headerTabla = tablaProyectos.getTableHeader();
        headerTabla.setBackground(AZUL_HEADER);
        headerTabla.setForeground(Color.WHITE);
        headerTabla.setFont(Tema.fuente(Font.BOLD, 14f));
        headerTabla.setPreferredSize(new Dimension(headerTabla.getWidth(), 38));
        headerTabla.setReorderingAllowed(false);

        tablaProyectos.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tablaProyectos.rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    tablaProyectos.repaint();
                }
            }
        });
        tablaProyectos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                filaHover = -1;
                tablaProyectos.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaProyectos.rowAtPoint(e.getPoint());
                    if (fila != -1) abrirModalDetalle(fila);
                }
            }
        });

        JScrollPane scrollTablaProyectos = new JScrollPane(tablaProyectos);
        Tema.estilizarTabla(tablaProyectos, scrollTablaProyectos);
        scrollTablaProyectos.setBorder(BorderFactory.createLineBorder(Tema.BORDE));

        panelEstadoVacio = crearPanelEstadoVacio();
        panelEstadoVacio.setBorder(BorderFactory.createLineBorder(Tema.BORDE));

        cardLayoutTabla = new CardLayout();
        panelTablaContenedor = new JPanel(cardLayoutTabla);
        panelTablaContenedor.setOpaque(false);
        panelTablaContenedor.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelTablaContenedor.setPreferredSize(new Dimension(900, 292));
        panelTablaContenedor.setMaximumSize(new Dimension(Short.MAX_VALUE, 340));
        panelTablaContenedor.add(scrollTablaProyectos, "tabla");
        panelTablaContenedor.add(panelEstadoVacio, "vacio");
        tarjeta.add(panelTablaContenedor);
        tarjeta.add(Box.createVerticalStrut(20));

        JSeparator separadorTabla = new JSeparator();
        separadorTabla.setForeground(new Color(200, 205, 210));
        separadorTabla.setAlignmentX(Component.LEFT_ALIGNMENT);
        separadorTabla.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        tarjeta.add(separadorTabla);
        tarjeta.add(Box.createVerticalStrut(20));

        JLabel lblResumenTitulo = new JLabel("Resumen global de proyectos:");
        lblResumenTitulo.setFont(Tema.fuente(Font.BOLD, 16f));
        lblResumenTitulo.setForeground(new Color(20, 25, 30));
        lblResumenTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(lblResumenTitulo);
        tarjeta.add(Box.createVerticalStrut(14));

        botonesResumen = new JButton[4];
        botonesResumen[0] = crearBotonResumen("doc", null);
        botonesResumen[1] = crearBotonResumen("gear", "En curso");
        botonesResumen[2] = crearBotonResumen("check", "Completado");
        botonesResumen[3] = crearBotonResumen("pencil", "Planificación");

        JPanel panelResumen = new JPanel(new GridLayout(1, 4, 20, 0));
        panelResumen.setOpaque(false);
        panelResumen.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelResumen.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        for (JButton b : botonesResumen) panelResumen.add(b);
        tarjeta.add(panelResumen);

        panelContenido.add(tarjeta);
        panelContenido.add(Box.createVerticalStrut(25));



        JScrollPane scrollContenido = new JScrollPane(panelContenido);
        scrollContenido.setBorder(null);
        scrollContenido.getViewport().setBackground(AZUL_MARINO_FONDO);
        scrollContenido.setBackground(AZUL_MARINO_FONDO);
        scrollContenido.getVerticalScrollBar().setUnitIncrement(16);

        panelPrincipal.add(scrollContenido, BorderLayout.CENTER);
        add(panelPrincipal);

        cargarDatosDesdeBD();
    }

    private JPanel crearPanelEstadoVacio() {
        JPanel panel = new JPanel();
        panel.setBackground(GRIS_TARJETA);
        panel.setLayout(new GridBagLayout());

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel icono = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(190, 196, 202));
                g2.setStroke(new BasicStroke(2.2f));
                int w = getWidth(), h = getHeight();
                g2.drawRoundRect(6, h / 2 - 4, w - 12, h / 2 - 4, 6, 6);
                g2.drawLine(6, h / 2 - 4, w / 2, 6);
                g2.drawLine(w / 2, 6, w - 6, h / 2 - 4);
                g2.dispose();
            }
        };
        icono.setOpaque(false);
        icono.setPreferredSize(new Dimension(70, 60));
        icono.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(icono);
        contenido.add(Box.createVerticalStrut(14));

        JLabel lblMensaje = new JLabel("Aún no hay proyectos registrados");
        lblMensaje.setFont(Tema.fuente(Font.BOLD, 16f));
        lblMensaje.setForeground(new Color(80, 88, 96));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblMensaje);
        contenido.add(Box.createVerticalStrut(4));

        JLabel lblSubmensaje = new JLabel("Registra la primera obra con los datos de su cliente y su supervisor");
        lblSubmensaje.setFont(Tema.fuente(Font.PLAIN, 13f));
        lblSubmensaje.setForeground(new Color(140, 148, 156));
        lblSubmensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblSubmensaje);
        contenido.add(Box.createVerticalStrut(20));

        JButton btnAgregar = crearBotonAccionProyecto("+ Nuevo proyecto", AZUL_ACCIONES);
        btnAgregar.setPreferredSize(new Dimension(170, 36));
        btnAgregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregar.addActionListener(e -> abrirModalAgregarProyecto());
        contenido.add(btnAgregar);

        panel.add(contenido);
        return panel;
    }

    private void actualizarVisibilidadTabla() {
        boolean hayDatos = modeloTabla.getRowCount() > 0;
        cardLayoutTabla.show(panelTablaContenedor, hayDatos ? "tabla" : "vacio");
    }

    private void cargarDatosDesdeBD() {
        proyectosActuales = proyectoDAO.listar();
        modeloTabla.setRowCount(0);
        for (Proyecto proy : proyectosActuales) {
            String idFormateado = String.format("%03d", proy.getId());
            modeloTabla.addRow(new Object[]{
                idFormateado, proy.getNombre(), proy.getUbicacion(), proy.getEstado(), proy.getProgreso(),
                proy.getClienteNombre(), proy.getSupervisorNombre().isEmpty() ? "Sin asignar" : proy.getSupervisorNombre()
            });
        }
        actualizarResumen();

        String rol = Sesion.haySesion() ? Sesion.getUsuarioActual().getRol() : "TODOS";
        notificacionesNoLeidas = notificacionDAO.contarNoLeidas(rol);
        if (panelHeaderRef != null) panelHeaderRef.repaint();
        actualizarVisibilidadTabla();
    }

    private JButton crearBotonAccionProyecto(String texto, Color color) {
        return Tema.botonAccion(texto, color);
    }

    private JButton crearBotonResumen(String tipoIcono, String estadoFiltro) {
        JButton boton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean activo = Objects.equals(estadoFiltro, filtroEstadoActivo);
                if (activo) {
                    g2.setColor(new Color(213, 224, 235));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setIconTextGap(8);
        boton.setFont(Tema.fuente(Font.BOLD, 14f));
        boton.setForeground(new Color(20, 25, 30));
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setIcon(crearIconoMini(tipoIcono, new Color(20, 25, 30), 18));
        boton.addActionListener(e -> {
            filtroEstadoActivo = Objects.equals(estadoFiltro, filtroEstadoActivo) ? null : estadoFiltro;
            aplicarFiltros();
            for (JButton b : botonesResumen) b.repaint();
        });
        return boton;
    }

    private void aplicarFiltros() {
        RowFilter<DefaultTableModel, Integer> filtro = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String texto = txtBuscarProyecto.getText().equals(PLACEHOLDER_BUSCAR)
                        ? "" : txtBuscarProyecto.getText().trim().toLowerCase();
                boolean coincideTexto = texto.isEmpty()
                        || entry.getStringValue(1).toLowerCase().contains(texto)
                        || entry.getStringValue(2).toLowerCase().contains(texto);
                boolean coincideEstado = filtroEstadoActivo == null || entry.getStringValue(3).equals(filtroEstadoActivo);
                return coincideTexto && coincideEstado;
            }
        };
        sorter.setRowFilter(filtro);
    }

    private void actualizarResumen() {
        int total = modeloTabla.getRowCount();
        int enCurso = 0, completados = 0, planificacion = 0;
        for (int i = 0; i < total; i++) {
            String estado = String.valueOf(modeloTabla.getValueAt(i, 3));
            if ("En curso".equals(estado)) enCurso++;
            else if ("Completado".equals(estado)) completados++;
            else if ("Planificación".equals(estado)) planificacion++;
        }
        botonesResumen[0].setText("Total: " + total);
        botonesResumen[1].setText("En curso: " + enCurso);
        botonesResumen[2].setText("Completados: " + completados);
        botonesResumen[3].setText("Planificacion: " + planificacion);
    }

    private String generarNuevoId() {
        int maxId = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            try {
                int id = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(i, 0)));
                if (id > maxId) maxId = id;
            } catch (NumberFormatException ignored) { }
        }
        return String.format("%03d", maxId + 1);
    }

    /** Formulario único para registrar o modificar una obra (cliente + supervisor). */
    private void abrirFormularioProyecto(Proyecto existente) {
        final boolean editando = existente != null;
        final Proyecto proy = editando ? existente : new Proyecto("", "", "En curso", 0);
        final int ancho = 420, alto = editando ? 640 : 570;

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(ancho, alto);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, ancho, alto, 24, 24));

        JPanel panelFondo = new JPanel(null);
        panelFondo.setBackground(new Color(233, 238, 243));

        JButton btnCerrar = CampoUtils.crearBotonCerrarModal();
        btnCerrar.setBounds(ancho - 50, 15, 35, 35);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.addActionListener(e -> modal.dispose());
        panelFondo.add(btnCerrar);

        JLabel lblTitulo = new JLabel(editando ? "Modificar obra" : "Registrar nueva obra", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, ancho - 40, 30);
        panelFondo.add(lblTitulo);

        JTextField txtNombre = new JTextField(proy.getNombre());
        JTextField txtUbicacion = new JTextField(proy.getUbicacion());
        JTextField txtCliente = new JTextField(proy.getClienteNombre());
        JTextField txtTelefono = new JTextField(proy.getClienteTelefono());
        JTextField txtCorreo = new JTextField(proy.getClienteCorreo());

        // Supervisores disponibles (cuentas con rol Supervisor)
        List<modelo.Cuenta> supervisores = new controlador.CuentaDAO().listarSupervisores();
        JComboBox<String> comboSupervisor = new JComboBox<>();
        comboSupervisor.addItem("Sin asignar");
        int seleccion = 0;
        for (int i = 0; i < supervisores.size(); i++) {
            comboSupervisor.addItem(supervisores.get(i).getNombre());
            if (proy.getIdSupervisor() != null && supervisores.get(i).getId() == proy.getIdSupervisor()) seleccion = i + 1;
        }
        comboSupervisor.setSelectedIndex(seleccion);

        JComboBox<String> comboEstado = new JComboBox<>(new String[]{"En curso", "Completado", "Planificación"});
        comboEstado.setSelectedItem(proy.getEstado());
        JTextField txtProgreso = new JTextField(String.valueOf(proy.getProgreso()));
        CampoUtils.soloEnteroPositivo(txtProgreso);
        comboEstado.addActionListener(ev -> {
            if ("Completado".equals(comboEstado.getSelectedItem())) txtProgreso.setText("100");
        });

        java.util.List<String> etiquetas = new java.util.ArrayList<>();
        java.util.List<JComponent> campos = new java.util.ArrayList<>();
        etiquetas.add("Nombre de la obra");        campos.add(txtNombre);
        etiquetas.add("Ubicación");                campos.add(txtUbicacion);
        etiquetas.add("Nombre del cliente");       campos.add(txtCliente);
        etiquetas.add("Teléfono del cliente");     campos.add(txtTelefono);
        etiquetas.add("Correo del cliente");       campos.add(txtCorreo);
        etiquetas.add("Supervisor responsable");   campos.add(comboSupervisor);
        if (editando) {
            etiquetas.add("Estado");               campos.add(comboEstado);
            etiquetas.add("Progreso (0-100)");     campos.add(txtProgreso);
        }

        int y = 58;
        for (int i = 0; i < campos.size(); i++) {
            JLabel lbl = new JLabel(etiquetas.get(i));
            lbl.setFont(Tema.fuente(Font.BOLD, 12f));
            lbl.setBounds(60, y, 300, 18);
            panelFondo.add(lbl);
            campos.get(i).setBounds(60, y + 18, 300, 30);
            panelFondo.add(campos.get(i));
            y += 64;
        }

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds((ancho - 140) / 2, y + 6, 140, 35);
        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String ubicacion = txtUbicacion.getText().trim();
            String cliente = txtCliente.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String correo = txtCorreo.getText().trim();

            if (nombre.isEmpty() || ubicacion.isEmpty() || cliente.isEmpty()) {
                JOptionPane.showMessageDialog(modal, "Escribe al menos el nombre de la obra, la ubicación y el nombre del cliente.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!correo.isEmpty() && !correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
                JOptionPane.showMessageDialog(modal, "El correo del cliente no tiene un formato válido.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!telefono.isEmpty() && !telefono.matches("[0-9 +()-]{7,20}")) {
                JOptionPane.showMessageDialog(modal, "El teléfono solo puede llevar números (de 7 a 20 caracteres).", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int progreso = 0;
            String estado = "En curso";
            if (editando) {
                estado = (String) comboEstado.getSelectedItem();
                try {
                    progreso = Integer.parseInt(txtProgreso.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(modal, "El progreso debe ser un número entero.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (progreso < 0 || progreso > 100) {
                    JOptionPane.showMessageDialog(modal, "El progreso debe estar entre 0 y 100.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            proy.setNombre(nombre);
            proy.setUbicacion(ubicacion);
            proy.setClienteNombre(cliente);
            proy.setClienteTelefono(telefono);
            proy.setClienteCorreo(correo);
            int idx = comboSupervisor.getSelectedIndex();
            proy.setIdSupervisor(idx <= 0 ? null : supervisores.get(idx - 1).getId());
            proy.setEstado(estado);
            proy.setProgreso(progreso);

            boolean ok = editando ? proyectoDAO.modificar(proy) : proyectoDAO.registrar(proy);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, editando ? "Obra actualizada correctamente." : "Obra registrada correctamente.");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo guardar la obra. Revisa la conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelFondo.add(btnGuardar);
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        CampoUtils.estilizarBotonPrimario(btnGuardar, AZUL_ACCIONES);
        modal.getRootPane().setDefaultButton(btnGuardar);

        modal.add(panelFondo);
        modal.setVisible(true);
    }

    private void abrirModalAgregarProyecto() {
        abrirFormularioProyecto(null);
    }

    private void abrirModalModificarProyecto() {
        int filaSeleccionada = tablaProyectos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero una obra de la tabla para modificar.", "Ninguna obra seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaProyectos.convertRowIndexToModel(filaSeleccionada);
        abrirFormularioProyecto(proyectosActuales.get(fila));
    }

    private void abrirModalEliminarProyecto() {
        int filaSeleccionada = tablaProyectos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un proyecto de la tabla para eliminar.", "Ningún proyecto seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaProyectos.convertRowIndexToModel(filaSeleccionada);
        Proyecto proySeleccionado = proyectosActuales.get(fila);

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Estás a punto de eliminar el proyecto \"" + proySeleccionado.getNombre() + "\".\n" +
            "Esta acción NO se puede deshacer.\n\n¿Deseas continuar?",
            "Advertencia: acción irreversible",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean ok = proyectoDAO.eliminar(proySeleccionado.getId());
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(this, "Proyecto eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el proyecto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModalDetalle(int filaVista) {
        int fila = tablaProyectos.convertRowIndexToModel(filaVista);
        String id = String.valueOf(modeloTabla.getValueAt(fila, 0));
        String nombre = String.valueOf(modeloTabla.getValueAt(fila, 1));
        String ubicacion = String.valueOf(modeloTabla.getValueAt(fila, 2));
        String estado = String.valueOf(modeloTabla.getValueAt(fila, 3));
        int progreso = (Integer) modeloTabla.getValueAt(fila, 4);
        Color colorBadge = colorPorEstado(estado);

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(420, 490);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 420, 490, 24, 24));

        JPanel panelFondo = new JPanel(null);
        panelFondo.setBackground(new Color(233, 238, 243));

        JButton btnCerrar = CampoUtils.crearBotonCerrarModal();
        btnCerrar.setBounds(370, 15, 32, 32);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> modal.dispose());
        panelFondo.add(btnCerrar);

        JLabel lblTitulo = new JLabel("Detalle del proyecto", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 380, 30);
        panelFondo.add(lblTitulo);

        Proyecto pd = proyectosActuales.get(fila);
        String[] etiquetas = {"ID de proyecto", "Nombre", "Ubicación", "Cliente", "Supervisor"};
        String[] valores = {id, nombre, ubicacion,
            pd.getClienteNombre() + (pd.getClienteTelefono().isEmpty() ? "" : "  ·  " + pd.getClienteTelefono()),
            pd.getSupervisorNombre().isEmpty() ? "Sin asignar" : pd.getSupervisorNombre()};
        int y = 70;
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(Tema.fuente(Font.BOLD, 12f));
            lbl.setBounds(50, y, 320, 18);
            panelFondo.add(lbl);

            JLabel val = new JLabel(valores[i]);
            val.setFont(Tema.fuente(Font.PLAIN, 15f));
            val.setBounds(50, y + 18, 320, 24);
            panelFondo.add(val);
            y += 55;
        }

        JLabel lblEstadoTitulo = new JLabel("Estado");
        lblEstadoTitulo.setFont(Tema.fuente(Font.BOLD, 12f));
        lblEstadoTitulo.setBounds(50, y, 320, 18);
        panelFondo.add(lblEstadoTitulo);

        JLabel badge = crearBadgeEstado(estado, colorBadge);
        badge.setBounds(50, y + 20, 130, 26);
        panelFondo.add(badge);
        y += 60;

        JLabel lblProgresoTitulo = new JLabel("Progreso: " + progreso + "%");
        lblProgresoTitulo.setFont(Tema.fuente(Font.BOLD, 12f));
        lblProgresoTitulo.setBounds(50, y, 320, 18);
        panelFondo.add(lblProgresoTitulo);

        final int progresoFinal = progreso;
        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 224, 228));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                int relleno = (int) (getWidth() * (progresoFinal / 100.0));
                if (relleno > 0) {
                    g2.setColor(colorBadge);
                    g2.fill(new RoundRectangle2D.Float(0, 0, relleno, getHeight(), getHeight(), getHeight()));
                }
                g2.dispose();
            }
        };
        barra.setOpaque(false);
        barra.setBounds(50, y + 20, 320, 14);
        panelFondo.add(barra);

        modal.add(panelFondo);
        modal.setVisible(true);
    }

    @Override
    public void setVisible(boolean visible) {
        if (accesoDenegado && visible) {
            JOptionPane.showMessageDialog(null, "El módulo de Proyectos es exclusivo del Dueño de la obra, quien registra las obras y asigna a su supervisor.", "Acceso restringido", JOptionPane.WARNING_MESSAGE);
            new Inventario().setVisible(true);
            this.dispose();
            return;
        }
        super.setVisible(visible);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Proyectos().setVisible(true));
    }
}
