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
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;

import controlador.MaquinariaDAO;
import controlador.NotificacionDAO;
import controlador.ProyectoDAO;
import modelo.Notificacion;
import modelo.Proyecto;

public class Maquinaria extends JFrame {

    private boolean accesoDenegado = false;

    private final Color AZUL_HEADER = new Color(6, 54, 83);
    private final Color AMARILLO_MENU = new Color(175, 140, 21);
    private final Color BLANCO_FONDO = Color.WHITE;
    private final Color AZUL_TARJETAS = new Color(13, 93, 142);
    private final Color AZUL_ACCIONES = Tema.AZUL_MEDIO;
    private final Color VERDE_BADGE = new Color(34, 139, 74);
    private final Color DORADO_BADGE = new Color(214, 158, 46);
    private final Color ROJO_BADGE = new Color(196, 60, 60);

    private final String PLACEHOLDER_BUSCAR = "Buscar activo...";

    private final MaquinariaDAO maquinariaDAO = new MaquinariaDAO();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private List<modelo.Maquinaria> maquinariaActual;

    private JTable tablaMaquinaria;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;
    private int filaHover = -1;

    private JPanel panelTablaContenedor;
    private CardLayout cardLayoutTabla;
    private JScrollPane scrollTabla;
    private JPanel panelEstadoVacio;

    private JLabel lblValorTotal;
    private JLabel lblValorMantenimiento;
    private JLabel lblValorReciente;

    private Image imgLogo;
    private Image imgCampana;
    private int notificacionesNoLeidas = 0;
    private JPanel headerRef;
    private Image imgUsuario;
    private Image[] iconosMenu = new Image[3];

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

        List<Notificacion> notifs = notificacionDAO.listarPorRol("ADMINISTRADOR");

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
                    Color colorEstado = "APROBADA".equals(n.getEstado()) ? new Color(34, 139, 74) : new Color(196, 60, 60);
                    JLabel lblEstado = new JLabel("Estado: " + n.getEstado());
                    lblEstado.setFont(Tema.fuente(Font.PLAIN, 10f));
                    lblEstado.setForeground(colorEstado);
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
            int opcion = JOptionPane.showConfirmDialog(this, "¿Estás seguro que deseas cerrar sesión?", "Cerrar sesión", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opcion == JOptionPane.YES_OPTION) {
                new frontend().setVisible(true);
                dispose();
            }
        });
        panelMenu.add(btnCerrarSesion);

        popup.add(panelMenu);
        popup.show(invoker, x - 240, y + 8);
    }

    private Color colorFila(int row, boolean isSelected) {
        return Tema.colorFila(row, isSelected, row == filaHover);
    }

    private Color colorPorEstadoActivo(String estado) {
        if ("Disponible".equalsIgnoreCase(estado)) return VERDE_BADGE;
        if ("En uso".equalsIgnoreCase(estado)) return DORADO_BADGE;
        return ROJO_BADGE;
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

    private class TextCellRenderer extends DefaultTableCellRenderer {
        private final boolean negrita;
        TextCellRenderer(boolean negrita) { this.negrita = negrita; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(colorFila(row, isSelected));
            setForeground(new Color(30, 35, 40));
            setFont(Tema.fuente(negrita ? Font.BOLD : Font.PLAIN, 14f));
            setHorizontalAlignment(SwingConstants.CENTER);
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
            add(crearBadgeEstado(estado, colorPorEstadoActivo(estado)));
            setBackground(colorFila(row, isSelected));
            return this;
        }
    }

    private JPanel crearPanelEstadoVacio() {
        JPanel panel = new JPanel();
        panel.setBackground(BLANCO_FONDO);
        panel.setLayout(new GridBagLayout());

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel icono = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 206, 212));
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

        JLabel lblMensaje = new JLabel("Aún no hay activos registrados");
        lblMensaje.setFont(Tema.fuente(Font.BOLD, 16f));
        lblMensaje.setForeground(new Color(90, 98, 106));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblMensaje);
        contenido.add(Box.createVerticalStrut(4));

        JLabel lblSubmensaje = new JLabel("Agrega la primera máquina o herramienta para empezar");
        lblSubmensaje.setFont(Tema.fuente(Font.PLAIN, 13f));
        lblSubmensaje.setForeground(new Color(150, 158, 166));
        lblSubmensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblSubmensaje);
        contenido.add(Box.createVerticalStrut(20));

        JButton btnAgregar = crearBotonAccion("+ Nuevo activo", AZUL_ACCIONES);
        btnAgregar.setPreferredSize(new Dimension(150, 36));
        btnAgregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregar.addActionListener(e -> abrirModalAgregar());
        contenido.add(btnAgregar);

        panel.add(contenido);
        return panel;
    }

    private void actualizarVisibilidadTabla() {
        cardLayoutTabla.show(panelTablaContenedor, modeloTabla.getRowCount() == 0 ? "vacio" : "tabla");
    }

    public Maquinaria() {

        if (controlador.Sesion.haySesion() && "Dueno".equals(controlador.Sesion.getUsuarioActual().getRol())) {
            accesoDenegado = true;
            return;
        }

        Tema.instalar();
        setTitle("CisConstructora - Panel de Maquinaria y Herramientas");
        setSize(1150, 680);
        setMinimumSize(new Dimension(1020, 580));
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
        panelPrincipal.setBackground(Tema.FONDO);

        JPanel panelHeader = Chrome.encabezado(() -> notificacionesNoLeidas, new Chrome.Acciones() {
            @Override public void inicio() { Chrome.abrir(Maquinaria.this, "INVENTARIO"); }
            @Override public void campana(Component origen, int x, int y) {
                mostrarMenuNotificaciones(origen, x, y);
                if (notificacionesNoLeidas > 0) {
                    notificacionDAO.marcarTodasLeidas("ADMINISTRADOR");
                    notificacionesNoLeidas = 0;
                    origen.repaint();
                }
            }
            @Override public void perfil(Component origen, int x, int y) { mostrarMenuPerfil(origen, x, y); }
        });
        headerRef = panelHeader;
        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        JPanel panelMenuLateral = Chrome.menuLateral("MAQUINARIA", m -> Chrome.abrir(this, m));
        panelPrincipal.add(panelMenuLateral, BorderLayout.WEST);

        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setOpaque(false);
        panelContenido.setBorder(new EmptyBorder(26, 34, 30, 34));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTituloSeccion = new JLabel("Modulo de Maquinaria / Herramientas");
        lblTituloSeccion.setFont(Tema.fuente(Font.BOLD, 26f));
        lblTituloSeccion.setForeground(Color.BLACK);
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 18, 0);
        panelContenido.add(Chrome.titulo("Maquinaria y herramientas", "Equipo disponible, en uso y en reparación"), gbc);

        JPanel panelTarjetas = new JPanel(new GridLayout(1, 3, 20, 0));
        panelTarjetas.setOpaque(false);

        JPanel tarjetaTotal = crearTarjetaKpi("Total de activos", "0");
        lblValorTotal = (JLabel) tarjetaTotal.getComponent(1);
        panelTarjetas.add(tarjetaTotal);

        JPanel tarjetaMantenimiento = crearTarjetaKpi("En reparación", "0");
        lblValorMantenimiento = (JLabel) tarjetaMantenimiento.getComponent(1);
        panelTarjetas.add(tarjetaMantenimiento);

        JPanel tarjetaReciente = crearTarjetaKpi("Recientemente añadido", "—");
        lblValorReciente = (JLabel) tarjetaReciente.getComponent(1);
        panelTarjetas.add(tarjetaReciente);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 22, 0);
        panelContenido.add(panelTarjetas, gbc);

        JPanel panelAccionesTabla = new JPanel(new BorderLayout());
        panelAccionesTabla.setOpaque(false);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setOpaque(false);

        JLabel lblSubtitulo = new JLabel("Lista de Activos");
        lblSubtitulo.setFont(Tema.fuente(Font.BOLD, 15f));
        lblSubtitulo.setForeground(Color.BLACK);
        panelBusqueda.add(lblSubtitulo);

        txtBuscar = new JTextField(PLACEHOLDER_BUSCAR);
        Tema.estilizarBuscador(txtBuscar);
        txtBuscar.setPreferredSize(new Dimension(290, 38));
        txtBuscar.setFont(Tema.fuente(Font.PLAIN, 13f));
        txtBuscar.setForeground(Color.GRAY);
        txtBuscar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtBuscar.getText().equals(PLACEHOLDER_BUSCAR)) {
                    txtBuscar.setText("");
                    txtBuscar.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtBuscar.getText().isEmpty()) {
                    txtBuscar.setForeground(Color.GRAY);
                    txtBuscar.setText(PLACEHOLDER_BUSCAR);
                }
            }
        });
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        panelBusqueda.add(txtBuscar);
        panelAccionesTabla.add(panelBusqueda, BorderLayout.WEST);

        JPanel panelBotonesDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotonesDerecha.setOpaque(false);

        JButton btnModificar = crearBotonAccion("Modificar", AZUL_ACCIONES);
        btnModificar.addActionListener(e -> abrirModalModificar());
        panelBotonesDerecha.add(btnModificar);

        JButton btnEliminar = crearBotonAccion("Eliminar", ROJO_BADGE);
        btnEliminar.addActionListener(e -> abrirModalEliminar());
        panelBotonesDerecha.add(btnEliminar);

        JButton btnNuevo = crearBotonAccion("+ Nuevo", VERDE_BADGE);
        btnNuevo.addActionListener(e -> abrirModalAgregar());
        panelBotonesDerecha.add(btnNuevo);

        panelAccionesTabla.add(panelBotonesDerecha, BorderLayout.EAST);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        panelContenido.add(panelAccionesTabla, gbc);

        String[] columnas = {"ID Equipo", "Descripción", "Tipo", "Estado", "Último Mantenimiento"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaMaquinaria = new JTable(modeloTabla);
        tablaMaquinaria.setRowHeight(38);
        tablaMaquinaria.setFont(Tema.fuente(Font.BOLD, 14f));
        tablaMaquinaria.setGridColor(new Color(230, 230, 230));
        tablaMaquinaria.setShowVerticalLines(false);
        tablaMaquinaria.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(modeloTabla);
        tablaMaquinaria.setRowSorter(sorter);

        for (int i = 0; i < tablaMaquinaria.getColumnCount(); i++) {
            if (i == 3) {
                tablaMaquinaria.getColumnModel().getColumn(i).setCellRenderer(new EstadoCellRenderer());
            } else {
                tablaMaquinaria.getColumnModel().getColumn(i).setCellRenderer(new TextCellRenderer(i == 0));
            }
        }

        JTableHeader headerTabla = tablaMaquinaria.getTableHeader();
        headerTabla.setBackground(AZUL_HEADER);
        headerTabla.setForeground(Color.WHITE);
        headerTabla.setFont(Tema.fuente(Font.BOLD, 14f));
        headerTabla.setPreferredSize(new Dimension(headerTabla.getWidth(), 40));
        headerTabla.setReorderingAllowed(false);

        tablaMaquinaria.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tablaMaquinaria.rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    tablaMaquinaria.repaint();
                }
            }
        });
        tablaMaquinaria.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                filaHover = -1;
                tablaMaquinaria.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaMaquinaria.rowAtPoint(e.getPoint());
                    if (fila != -1) abrirModalDetalle(fila);
                }
            }
        });

        scrollTabla = new JScrollPane(tablaMaquinaria);
        scrollTabla.getViewport().setBackground(BLANCO_FONDO);
        Tema.estilizarTabla(tablaMaquinaria, scrollTabla);
        tablaMaquinaria.setPreferredScrollableViewportSize(new Dimension(100, 160));

        panelEstadoVacio = crearPanelEstadoVacio();
        

        cardLayoutTabla = new CardLayout();
        panelTablaContenedor = new JPanel(cardLayoutTabla);
        panelTablaContenedor.setOpaque(false);
        panelTablaContenedor.add(scrollTabla, "tabla");
        panelTablaContenedor.add(panelEstadoVacio, "vacio");

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        Tema.Tarjeta tarjetaTabla = new Tema.Tarjeta();
        tarjetaTabla.setLayout(new BorderLayout());
        tarjetaTabla.add(panelTablaContenedor, BorderLayout.CENTER);
        panelContenido.add(tarjetaTabla, gbc);

        panelPrincipal.add(panelContenido, BorderLayout.CENTER);
        add(panelPrincipal);

        cargarDatosDesdeBD();
        actualizarVisibilidadTabla();
    }

    private void cargarDatosDesdeBD() {
        maquinariaActual = maquinariaDAO.listar();
        modeloTabla.setRowCount(0);
        for (modelo.Maquinaria maq : maquinariaActual) {
            String prefijo = "Pesada".equalsIgnoreCase(maq.getTipo()) ? "MAQ" : "HER";
            String idFormateado = String.format("#%s%03d", prefijo, maq.getId());
            String fecha = (maq.getUltimoMantenimiento() != null) ? maq.getUltimoMantenimiento().toString() : "N/A";
            modeloTabla.addRow(new Object[]{
                idFormateado, maq.getDescripcion(), maq.getTipo(), maq.getEstado(), fecha
            });
        }
        actualizarResumen();

        notificacionesNoLeidas = notificacionDAO.contarNoLeidas("ADMINISTRADOR");
        if (headerRef != null) headerRef.repaint();
    }

    private void aplicarFiltro() {
        RowFilter<DefaultTableModel, Integer> filtro = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String texto = txtBuscar.getText().equals(PLACEHOLDER_BUSCAR)
                        ? "" : txtBuscar.getText().trim().toLowerCase();
                if (texto.isEmpty()) return true;
                return entry.getStringValue(1).toLowerCase().contains(texto)
                        || entry.getStringValue(2).toLowerCase().contains(texto);
            }
        };
        sorter.setRowFilter(filtro);
    }

    private void actualizarResumen() {
        int total = modeloTabla.getRowCount();
        int enReparacion = 0;
        for (int i = 0; i < total; i++) {
            String estado = String.valueOf(modeloTabla.getValueAt(i, 3));
            if ("En reparación".equalsIgnoreCase(estado)) enReparacion++;
        }
        lblValorTotal.setText(String.valueOf(total));
        lblValorMantenimiento.setText(enReparacion + (enReparacion == 1 ? " unidad" : " unidades"));
        if (total > 0) {
            String descripcion = String.valueOf(modeloTabla.getValueAt(total - 1, 1));
            lblValorReciente.setText(descripcion);
        } else {
            lblValorReciente.setText("—");
        }
        actualizarVisibilidadTabla();
    }

    private String generarNuevoId(String tipo) {
        String prefijo = "Pesada".equalsIgnoreCase(tipo) ? "MAQ" : "HER";
        int maxId = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String idTexto = String.valueOf(modeloTabla.getValueAt(i, 0));
            if (idTexto.startsWith("#" + prefijo)) {
                try {
                    int id = Integer.parseInt(idTexto.replaceAll("[^0-9]", ""));
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException ignored) { }
            }
        }
        return String.format("#%s%03d", prefijo, maxId + 1);
    }

    private JPanel crearTarjetaKpi(String titulo, String valor) {
        return Tema.tarjetaKpi(titulo, valor);
    }

    private JButton crearBotonAccion(String texto, Color color) {
        return Tema.botonAccion(texto, color);
    }

    private void abrirModalAgregar() {
        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(400, 530);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 400, 530, 24, 24));

        JPanel panelFondo = new JPanel(null);
        panelFondo.setBackground(new Color(233, 238, 243));

        JButton btnCerrar = CampoUtils.crearBotonCerrarModal();
        btnCerrar.setBounds(350, 15, 35, 35);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.addActionListener(e -> modal.dispose());
        panelFondo.add(btnCerrar);

        JLabel lblTitulo = new JLabel("Agregar nuevo activo", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 360, 30);
        panelFondo.add(lblTitulo);

        JComboBox<String> comboDescripcion = new JComboBox<>(CatalogoMaquinaria.getNombresActivos());
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Pesada", "Manual"});
        comboTipo.setEnabled(false);
        JComboBox<String> comboEstado = new JComboBox<>(new String[]{"Disponible", "En uso", "En reparación"});
        JTextField txtCosto = new JTextField();
        CampoUtils.soloDecimalPositivo(txtCosto);
        txtCosto.setEditable(false);
        txtCosto.setBackground(new Color(224, 228, 232));

        Runnable aplicarDatosActivo = () -> {
            String activoElegido = (String) comboDescripcion.getSelectedItem();
            CatalogoMaquinaria.DatosMaquinaria datos = CatalogoMaquinaria.getDatos(activoElegido);
            if (datos != null) {
                comboTipo.setSelectedItem(datos.tipo);
                txtCosto.setText(String.format("%.2f", datos.costo));
            }
        };
        comboDescripcion.addActionListener(ev -> aplicarDatosActivo.run());
        aplicarDatosActivo.run();

        List<Proyecto> proyectosDisponibles = proyectoDAO.listar();
        Vector<String> nombresProyectos = new Vector<>();
        nombresProyectos.add("Ninguno (no asignar a proyecto)");
        for (Proyecto p : proyectosDisponibles) nombresProyectos.add(p.getNombre());
        JComboBox<String> comboProyecto = new JComboBox<>(nombresProyectos);

        String[] etiquetas = {"Activo", "Tipo", "Estado", "Costo ($)", "Cargar al proyecto"};
        JComponent[] campos = {comboDescripcion, comboTipo, comboEstado, txtCosto, comboProyecto};

        int y = 60;
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(Tema.fuente(Font.BOLD, 12f));
            lbl.setBounds(60, y, 280, 20);
            panelFondo.add(lbl);
            campos[i].setBounds(60, y + 20, 280, 30);
            panelFondo.add(campos[i]);
            y += 65;
        }

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(130, y + 10, 140, 35);
        btnGuardar.addActionListener(e -> {
            String descripcion = (String) comboDescripcion.getSelectedItem();
            String tipo = (String) comboTipo.getSelectedItem();
            String estado = (String) comboEstado.getSelectedItem();
            String costoTexto = txtCosto.getText().trim();
            int indiceProyecto = comboProyecto.getSelectedIndex();

            double costo;
            try {
                costo = Double.parseDouble(costoTexto.replace(",", "."));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(modal, "El costo debe ser un número válido.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            modelo.Maquinaria nuevo = new modelo.Maquinaria(descripcion, tipo, estado);
            nuevo.setCosto(costo);

            if (indiceProyecto == 0) {
                boolean ok = maquinariaDAO.registrar(nuevo);
                if (ok) {
                    cargarDatosDesdeBD();
                    JOptionPane.showMessageDialog(modal, "Activo registrado correctamente.");
                    modal.dispose();
                } else {
                    JOptionPane.showMessageDialog(modal, "No se pudo registrar el activo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            Proyecto proyecto = proyectosDisponibles.get(indiceProyecto - 1);
            nuevo.setIdProyecto(proyecto.getId());
            boolean ok = maquinariaDAO.registrar(nuevo);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, "Activo registrado y asignado a la obra \"" + proyecto.getNombre() + "\".");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo registrar el activo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelFondo.add(btnGuardar);
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        CampoUtils.estilizarBotonPrimario(btnGuardar, AZUL_ACCIONES);
        modal.getRootPane().setDefaultButton(btnGuardar);

        modal.add(panelFondo);
        modal.setVisible(true);
    }

    private void abrirModalModificar() {
        int filaSeleccionada = tablaMaquinaria.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un activo de la tabla para modificar.", "Ningún activo seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaMaquinaria.convertRowIndexToModel(filaSeleccionada);
        modelo.Maquinaria maqSeleccionada = maquinariaActual.get(fila);

        String prefijo = "Pesada".equalsIgnoreCase(maqSeleccionada.getTipo()) ? "MAQ" : "HER";
        String idActual = String.format("#%s%03d", prefijo, maqSeleccionada.getId());
        String estadoActual = maqSeleccionada.getEstado();

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(400, 500);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 400, 500, 24, 24));

        JPanel panelFondo = new JPanel(null);
        panelFondo.setBackground(new Color(233, 238, 243));

        JButton btnCerrar = CampoUtils.crearBotonCerrarModal();
        btnCerrar.setBounds(350, 15, 35, 35);
        btnCerrar.addActionListener(e -> modal.dispose());
        panelFondo.add(btnCerrar);

        JLabel lblTitulo = new JLabel("Modificar Activo", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 360, 30);
        panelFondo.add(lblTitulo);

        JTextField txtId = new JTextField(idActual);
        txtId.setEditable(false);
        txtId.setBackground(new Color(220, 220, 220));

        JTextField txtDescripcion = new JTextField(maqSeleccionada.getDescripcion());
        txtDescripcion.setEditable(false);
        txtDescripcion.setBackground(new Color(220, 220, 220));

        JTextField txtTipo = new JTextField(maqSeleccionada.getTipo());
        txtTipo.setEditable(false);
        txtTipo.setBackground(new Color(220, 220, 220));

        JTextField txtCosto = new JTextField(String.format("%.2f", maqSeleccionada.getCosto()));
        txtCosto.setEditable(false);
        txtCosto.setBackground(new Color(220, 220, 220));

        JComboBox<String> comboEstado = new JComboBox<>(new String[]{"Disponible", "En uso", "En reparación"});
        comboEstado.setSelectedItem(estadoActual);

        List<Proyecto> proyectosDisponibles = proyectoDAO.listar();
        Vector<String> nombresProyectos = new Vector<>();
        nombresProyectos.add("Ninguno (no asignar a proyecto)");
        for (Proyecto p : proyectosDisponibles) nombresProyectos.add(p.getNombre());
        JComboBox<String> comboProyecto = new JComboBox<>(nombresProyectos);
        int indiceActual = 0;
        if (maqSeleccionada.getIdProyecto() != null) {
            for (int i = 0; i < proyectosDisponibles.size(); i++) {
                if (proyectosDisponibles.get(i).getId() == maqSeleccionada.getIdProyecto()) {
                    indiceActual = i + 1;
                    break;
                }
            }
        }
        comboProyecto.setSelectedIndex(indiceActual);

        String[] etiquetas = {"ID Equipo", "Descripción", "Tipo", "Costo ($)", "Estado", "Proyecto asignado"};
        JComponent[] campos = {txtId, txtDescripcion, txtTipo, txtCosto, comboEstado, comboProyecto};

        int y = 60;
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(Tema.fuente(Font.BOLD, 12f));
            lbl.setBounds(60, y, 280, 20);
            panelFondo.add(lbl);
            campos[i].setBounds(60, y + 20, 280, 30);
            panelFondo.add(campos[i]);
            y += 60;
        }

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(130, y + 10, 140, 35);
        btnGuardar.addActionListener(e -> {
            String estado = (String) comboEstado.getSelectedItem();
            int indiceNuevoProyecto = comboProyecto.getSelectedIndex();
            Integer idProyectoNuevo = indiceNuevoProyecto == 0 ? null : proyectosDisponibles.get(indiceNuevoProyecto - 1).getId();
            Integer idProyectoAnterior = maqSeleccionada.getIdProyecto();


            maqSeleccionada.setEstado(estado);
            maqSeleccionada.setIdProyecto(idProyectoNuevo);

            boolean ok = maquinariaDAO.modificar(maqSeleccionada);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, "Activo actualizado exitosamente.");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo actualizar el activo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelFondo.add(btnGuardar);
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        CampoUtils.estilizarBotonPrimario(btnGuardar, AZUL_ACCIONES);
        modal.getRootPane().setDefaultButton(btnGuardar);

        modal.add(panelFondo);
        modal.setVisible(true);
    }

    private void abrirModalEliminar() {
        int filaSeleccionada = tablaMaquinaria.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un activo de la tabla para eliminar.", "Ningún activo seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaMaquinaria.convertRowIndexToModel(filaSeleccionada);
        modelo.Maquinaria maqSeleccionada = maquinariaActual.get(fila);

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Estás a punto de eliminar \"" + maqSeleccionada.getDescripcion() + "\".\n" +
            "Esta acción NO se puede deshacer.\n\n¿Deseas continuar?",
            "Advertencia: acción irreversible",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean ok = maquinariaDAO.eliminar(maqSeleccionada.getId());
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(this, "Activo eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el activo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModalDetalle(int filaVista) {
        int fila = tablaMaquinaria.convertRowIndexToModel(filaVista);
        String id = String.valueOf(modeloTabla.getValueAt(fila, 0));
        String descripcion = String.valueOf(modeloTabla.getValueAt(fila, 1));
        String tipo = String.valueOf(modeloTabla.getValueAt(fila, 2));
        String estado = String.valueOf(modeloTabla.getValueAt(fila, 3));
        String fecha = String.valueOf(modeloTabla.getValueAt(fila, 4));

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(420, 400);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 420, 400, 24, 24));

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

        JLabel lblTitulo = new JLabel("Detalle del activo", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 380, 30);
        panelFondo.add(lblTitulo);

        String[] etiquetas = {"ID Equipo", "Descripción", "Tipo", "Último mantenimiento"};
        String[] valores = {id, descripcion, tipo, fecha};

        int y = 65;
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

        JLabel badge = crearBadgeEstado(estado, colorPorEstadoActivo(estado));
        badge.setBounds(50, y + 20, 130, 26);
        panelFondo.add(badge);

        modal.add(panelFondo);
        modal.setVisible(true);
    }

    @Override
    public void setVisible(boolean visible) {
        if (accesoDenegado && visible) {
            JOptionPane.showMessageDialog(null, "Como Dueño de la obra, tu acceso está limitado al panel de Proyectos.", "Acceso restringido", JOptionPane.WARNING_MESSAGE);
            new Proyectos().setVisible(true);
            this.dispose();
            return;
        }
        super.setVisible(visible);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Maquinaria().setVisible(true));
    }
}
