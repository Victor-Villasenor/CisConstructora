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

import controlador.EmpleadoDAO;
import controlador.NotificacionDAO;
import controlador.ProyectoDAO;
import modelo.Empleado;
import modelo.Notificacion;
import modelo.Proyecto;

public class Empleados extends JFrame {

    private boolean accesoDenegado = false;

    private final Color AZUL_HEADER = new Color(6, 54, 83);
    private final Color AMARILLO_MENU = new Color(175, 140, 21);
    private final Color BLANCO_FONDO = Color.WHITE;
    private final Color AZUL_TARJETAS = new Color(13, 93, 142);
    private final Color AZUL_ACCIONES = Tema.AZUL_MEDIO;
    private final Color VERDE_BADGE = new Color(34, 139, 74);
    private final Color DORADO_BADGE = new Color(214, 158, 46);
    private final Color ROJO_BADGE = new Color(196, 60, 60);
    private final Color GRIS_BADGE = new Color(120, 128, 136);
    private final Color MORADO_ASIGNAR = new Color(90, 65, 150);

    private final String PLACEHOLDER_BUSCAR = "Buscar empleado...";
    private String[] PROYECTOS_DISPONIBLES = {};
    private List<Proyecto> proyectosDisponiblesObjetos = new java.util.ArrayList<>();

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private List<Empleado> empleadosActuales;

    private JTable tablaEmpleados;
    private DefaultTableModel modeloTabla;
    private CardLayout cardLayoutTabla;
    private JPanel panelTablaContenedor;
    private JPanel panelEstadoVacio;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;
    private int filaHover = -1;

    private JLabel lblValorTotal;
    private JLabel lblValorActivos;
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

    private Color colorPorEstadoEmpleado(String estado) {
        if ("Activo".equalsIgnoreCase(estado)) return VERDE_BADGE;
        if ("Ocupado".equalsIgnoreCase(estado)) return DORADO_BADGE;
        return GRIS_BADGE;
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
            add(crearBadgeEstado(estado, colorPorEstadoEmpleado(estado)));
            setBackground(colorFila(row, isSelected));
            return this;
        }
    }

    public Empleados() {

        if (controlador.Sesion.haySesion() && "Dueno".equals(controlador.Sesion.getUsuarioActual().getRol())) {
            accesoDenegado = true;
            return;
        }

        Tema.instalar();
        setTitle("CisConstructora - Panel de Empleados");
        setSize(1180, 650);
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
            @Override public void inicio() { Chrome.abrir(Empleados.this, "INVENTARIO"); }
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

        JPanel panelMenuLateral = Chrome.menuLateral("EMPLEADOS", m -> Chrome.abrir(this, m));
        panelPrincipal.add(panelMenuLateral, BorderLayout.WEST);

        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setOpaque(false);
        panelContenido.setBorder(new EmptyBorder(26, 34, 30, 34));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTituloSeccion = new JLabel("Empleados");
        lblTituloSeccion.setFont(Tema.fuente(Font.BOLD, 26f));
        lblTituloSeccion.setForeground(Color.BLACK);
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 18, 0);
        panelContenido.add(Chrome.titulo("Empleados", "Personal que trabaja en las obras"), gbc);

        JPanel panelTarjetas = new JPanel(new GridLayout(1, 3, 20, 0));
        panelTarjetas.setOpaque(false);

        JPanel tarjetaTotal = crearTarjetaKpi("Total de empleados", "0");
        lblValorTotal = (JLabel) tarjetaTotal.getComponent(1);
        panelTarjetas.add(tarjetaTotal);

        JPanel tarjetaActivos = crearTarjetaKpi("Empleados activos", "0");
        lblValorActivos = (JLabel) tarjetaActivos.getComponent(1);
        panelTarjetas.add(tarjetaActivos);

        JPanel tarjetaReciente = crearTarjetaKpi("Nuevas contrataciones", "—");
        lblValorReciente = (JLabel) tarjetaReciente.getComponent(1);
        panelTarjetas.add(tarjetaReciente);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 22, 0);
        panelContenido.add(panelTarjetas, gbc);

        JPanel panelAccionesTabla = new JPanel(new BorderLayout());
        panelAccionesTabla.setOpaque(false);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setOpaque(false);

        JLabel lblSubtitulo = new JLabel("Lista de empleados");
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

        JButton btnAsignarProyecto = crearBotonAccion("Asignar a Proyecto", MORADO_ASIGNAR);
        btnAsignarProyecto.setPreferredSize(new Dimension(150, 30));
        btnAsignarProyecto.addActionListener(e -> abrirModalAsignarProyecto());
        panelBotonesDerecha.add(btnAsignarProyecto);

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

        String[] columnas = {"ID del Empleado", "Nombre Completo", "Cargo", "Estado", "Salario", "Ultima Actualizacion", "Telefono", "Direccion", "ProyectoAsignado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaEmpleados = new JTable(modeloTabla);
        tablaEmpleados.setRowHeight(38);
        tablaEmpleados.setFont(Tema.fuente(Font.BOLD, 14f));
        tablaEmpleados.setGridColor(new Color(230, 230, 230));
        tablaEmpleados.setShowVerticalLines(false);
        tablaEmpleados.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        for (int col : new int[]{6, 7, 8}) {
            tablaEmpleados.getColumnModel().getColumn(col).setMinWidth(0);
            tablaEmpleados.getColumnModel().getColumn(col).setMaxWidth(0);
            tablaEmpleados.getColumnModel().getColumn(col).setWidth(0);
        }

        sorter = new TableRowSorter<>(modeloTabla);
        tablaEmpleados.setRowSorter(sorter);

        for (int i = 0; i < 6; i++) {
            if (i == 3) {
                tablaEmpleados.getColumnModel().getColumn(i).setCellRenderer(new EstadoCellRenderer());
            } else {
                tablaEmpleados.getColumnModel().getColumn(i).setCellRenderer(new TextCellRenderer(i == 0));
            }
        }

        JTableHeader headerTabla = tablaEmpleados.getTableHeader();
        headerTabla.setBackground(AZUL_HEADER);
        headerTabla.setForeground(Color.WHITE);
        headerTabla.setFont(Tema.fuente(Font.BOLD, 14f));
        headerTabla.setPreferredSize(new Dimension(headerTabla.getWidth(), 40));
        headerTabla.setReorderingAllowed(false);

        tablaEmpleados.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tablaEmpleados.rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    tablaEmpleados.repaint();
                }
            }
        });
        tablaEmpleados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                filaHover = -1;
                tablaEmpleados.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaEmpleados.rowAtPoint(e.getPoint());
                    if (fila != -1) abrirModalDetalle(fila);
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaEmpleados);
        scrollTabla.getViewport().setBackground(BLANCO_FONDO);
        Tema.estilizarTabla(tablaEmpleados, scrollTabla);
        tablaEmpleados.setPreferredScrollableViewportSize(new Dimension(100, 160));

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

        JLabel lblMensaje = new JLabel("Aún no hay empleados registrados");
        lblMensaje.setFont(Tema.fuente(Font.BOLD, 16f));
        lblMensaje.setForeground(new Color(90, 98, 106));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblMensaje);
        contenido.add(Box.createVerticalStrut(4));

        JLabel lblSubmensaje = new JLabel("Agrega el primero para empezar a llevar el control de tu personal");
        lblSubmensaje.setFont(Tema.fuente(Font.PLAIN, 13f));
        lblSubmensaje.setForeground(new Color(150, 158, 166));
        lblSubmensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblSubmensaje);
        contenido.add(Box.createVerticalStrut(20));

        JButton btnAgregar = crearBotonAccion("+ Nuevo empleado", AZUL_ACCIONES);
        btnAgregar.setPreferredSize(new Dimension(160, 36));
        btnAgregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregar.addActionListener(e -> abrirModalAgregar());
        contenido.add(btnAgregar);

        panel.add(contenido);
        return panel;
    }

    private void actualizarVisibilidadTabla() {
        boolean hayDatos = modeloTabla.getRowCount() > 0;
        cardLayoutTabla.show(panelTablaContenedor, hayDatos ? "tabla" : "vacio");
    }

    private void cargarDatosDesdeBD() {
        empleadosActuales = empleadoDAO.listar();
        modeloTabla.setRowCount(0);
        for (Empleado emp : empleadosActuales) {
            String idFormateado = String.format("#EMP%03d", emp.getId());
            String salarioFormateado = "$" + String.format("%,.0f", emp.getSalario());
            String fecha = (emp.getUltimaActualizacion() != null) ? emp.getUltimaActualizacion().toString() : "N/A";
            modeloTabla.addRow(new Object[]{
                idFormateado, emp.getNombre(), emp.getCargo(), emp.getEstado(), salarioFormateado,
                fecha, emp.getTelefono(), emp.getDireccion(), emp.getProyectoAsignado()
            });
        }

        List<Proyecto> proyectos = proyectoDAO.listar();
        proyectosDisponiblesObjetos = proyectos;
        PROYECTOS_DISPONIBLES = new String[proyectos.size()];
        for (int i = 0; i < proyectos.size(); i++) {
            PROYECTOS_DISPONIBLES[i] = proyectos.get(i).getNombre();
        }

        actualizarResumen();
        actualizarVisibilidadTabla();

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
        int activos = 0;
        for (int i = 0; i < total; i++) {
            String estado = String.valueOf(modeloTabla.getValueAt(i, 3));
            if ("Activo".equalsIgnoreCase(estado)) activos++;
        }
        lblValorTotal.setText(String.valueOf(total));
        lblValorActivos.setText(String.valueOf(activos));
        if (total > 0) {
            String nombre = String.valueOf(modeloTabla.getValueAt(total - 1, 1));
            String cargo = String.valueOf(modeloTabla.getValueAt(total - 1, 2));
            lblValorReciente.setText(nombre + " (" + cargo + ")");
        } else {
            lblValorReciente.setText("—");
        }
    }

    private String generarNuevoId() {
        int maxId = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String idTexto = String.valueOf(modeloTabla.getValueAt(i, 0)).replaceAll("[^0-9]", "");
            try {
                int id = Integer.parseInt(idTexto);
                if (id > maxId) maxId = id;
            } catch (NumberFormatException ignored) { }
        }
        return String.format("#EMP%03d", maxId + 1);
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
        modal.setSize(400, 520);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 400, 520, 24, 24));

        JPanel panelFondo = new JPanel(null);
        panelFondo.setBackground(new Color(233, 238, 243));

        JButton btnCerrar = CampoUtils.crearBotonCerrarModal();
        btnCerrar.setBounds(350, 15, 35, 35);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.addActionListener(e -> modal.dispose());
        panelFondo.add(btnCerrar);

        JLabel lblTitulo = new JLabel("Agregar nuevo empleado", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 360, 30);
        panelFondo.add(lblTitulo);

        JTextField txtNombre = new JTextField();
        JComboBox<String> comboCargo = new JComboBox<>(CatalogoCargos.getNombresCargos());
        JComboBox<String> comboEstado = new JComboBox<>(new String[]{"Activo", "Ocupado", "Ausente"});
        JTextField txtSalario = new JTextField();
        CampoUtils.soloDecimalPositivo(txtSalario);
        txtSalario.setEditable(false);
        txtSalario.setBackground(new Color(224, 228, 232));
        JTextField txtTelefono = new JTextField();
        JTextField txtDireccion = new JTextField();

        comboCargo.addActionListener(ev -> {
            String cargoElegido = (String) comboCargo.getSelectedItem();
            txtSalario.setText(String.format("%.0f", CatalogoCargos.getSalario(cargoElegido)));
        });
        comboCargo.setSelectedIndex(0);
        txtSalario.setText(String.format("%.0f", CatalogoCargos.getSalario((String) comboCargo.getSelectedItem())));

        String[] etiquetas = {"Nombre completo", "Cargo", "Estado", "Salario (según cargo)", "Teléfono", "Dirección"};
        JComponent[] campos = {txtNombre, comboCargo, comboEstado, txtSalario, txtTelefono, txtDireccion};

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
            String nombre = txtNombre.getText().trim();
            String cargo = (String) comboCargo.getSelectedItem();
            String estado = (String) comboEstado.getSelectedItem();
            String salarioTexto = txtSalario.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();

            if (nombre.isEmpty() || salarioTexto.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                JOptionPane.showMessageDialog(modal, "Por favor completa todos los campos.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int salario;
            try {
                salario = Integer.parseInt(salarioTexto.replace("$", "").trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(modal, "El salario debe ser un número válido.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Empleado nuevo = new Empleado(nombre, telefono, direccion, cargo, estado, salario);
            boolean ok = empleadoDAO.registrar(nuevo);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, "Empleado registrado correctamente.");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo registrar el empleado. Revisa la conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
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
        int filaSeleccionada = tablaEmpleados.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un empleado de la tabla para modificar.", "Ningún empleado seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaEmpleados.convertRowIndexToModel(filaSeleccionada);
        Empleado empSeleccionado = empleadosActuales.get(fila);

        String idActual = String.format("#EMP%03d", empSeleccionado.getId());
        String nombreActual = empSeleccionado.getNombre();
        String cargoActual = empSeleccionado.getCargo();
        String estadoActual = empSeleccionado.getEstado();
        String salarioActual = String.valueOf((long) empSeleccionado.getSalario());
        String telefonoActual = empSeleccionado.getTelefono();
        String direccionActual = empSeleccionado.getDireccion();

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(400, 560);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 400, 560, 24, 24));

        JPanel panelFondo = new JPanel(null);
        panelFondo.setBackground(new Color(233, 238, 243));

        JButton btnCerrar = CampoUtils.crearBotonCerrarModal();
        btnCerrar.setBounds(350, 15, 35, 35);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.addActionListener(e -> modal.dispose());
        panelFondo.add(btnCerrar);

        JLabel lblTitulo = new JLabel("Modificar Empleado", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 360, 30);
        panelFondo.add(lblTitulo);

        JTextField txtId = new JTextField(idActual);
        txtId.setEditable(false);
        txtId.setBackground(new Color(220, 220, 220));

        JTextField txtNombre = new JTextField(nombreActual);
        JComboBox<String> comboCargo = new JComboBox<>(CatalogoCargos.getNombresCargos());
        if (CatalogoCargos.esCargoConocido(cargoActual)) {
            comboCargo.setSelectedItem(cargoActual);
        }
        JComboBox<String> comboEstado = new JComboBox<>(new String[]{"Activo", "Ocupado", "Ausente"});
        comboEstado.setSelectedItem(estadoActual);
        JTextField txtSalario = new JTextField(salarioActual);
        CampoUtils.soloDecimalPositivo(txtSalario);
        txtSalario.setEditable(false);
        txtSalario.setBackground(new Color(224, 228, 232));
        JTextField txtTelefono = new JTextField(telefonoActual);
        JTextField txtDireccion = new JTextField(direccionActual);

        comboCargo.addActionListener(ev -> {
            String cargoElegido = (String) comboCargo.getSelectedItem();
            txtSalario.setText(String.format("%.0f", CatalogoCargos.getSalario(cargoElegido)));
        });

        if (CatalogoCargos.esCargoConocido(cargoActual)) {
            txtSalario.setText(String.format("%.0f", CatalogoCargos.getSalario(cargoActual)));
        }

        String[] etiquetas = {"ID del empleado", "Nombre completo", "Cargo", "Estado", "Salario (según cargo)", "Teléfono", "Dirección"};
        JComponent[] campos = {txtId, txtNombre, comboCargo, comboEstado, txtSalario, txtTelefono, txtDireccion};

        int y = 60;
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(Tema.fuente(Font.BOLD, 12f));
            lbl.setBounds(60, y, 280, 20);
            panelFondo.add(lbl);
            campos[i].setBounds(60, y + 20, 280, 30);
            panelFondo.add(campos[i]);
            y += 62;
        }

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(130, y + 8, 140, 35);
        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String cargo = (String) comboCargo.getSelectedItem();
            String estado = (String) comboEstado.getSelectedItem();
            String salarioTexto = txtSalario.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();

            if (nombre.isEmpty() || salarioTexto.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                JOptionPane.showMessageDialog(modal, "Por favor completa todos los campos.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int salario;
            try {
                salario = Integer.parseInt(salarioTexto.replace("$", "").trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(modal, "El salario debe ser un número válido.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            empSeleccionado.setNombre(nombre);
            empSeleccionado.setCargo(cargo);
            empSeleccionado.setEstado(estado);
            empSeleccionado.setSalario(salario);
            empSeleccionado.setTelefono(telefono);
            empSeleccionado.setDireccion(direccion);
            if (!"Ocupado".equals(estado)) {
                empSeleccionado.setProyectoAsignado("");
            }

            boolean ok = empleadoDAO.modificar(empSeleccionado);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, "Empleado actualizado exitosamente.");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo actualizar el empleado.", "Error", JOptionPane.ERROR_MESSAGE);
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
        int filaSeleccionada = tablaEmpleados.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un empleado de la tabla para eliminar.", "Ningún empleado seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaEmpleados.convertRowIndexToModel(filaSeleccionada);
        Empleado empSeleccionado = empleadosActuales.get(fila);

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Estás a punto de eliminar a \"" + empSeleccionado.getNombre() + "\".\n" +
            "Esta acción NO se puede deshacer.\n\n¿Deseas continuar?",
            "Advertencia: acción irreversible",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean ok = empleadoDAO.eliminar(empSeleccionado.getId());
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(this, "Empleado eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el empleado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModalAsignarProyecto() {
        int filaSeleccionada = tablaEmpleados.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un empleado de la tabla.", "Ningún empleado seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaEmpleados.convertRowIndexToModel(filaSeleccionada);
        Empleado empSeleccionado = empleadosActuales.get(fila);
        String nombre = empSeleccionado.getNombre();
        String estado = empSeleccionado.getEstado();
        String proyectoActual = empSeleccionado.getProyectoAsignado();

        if ("Ocupado".equals(estado)) {
            int opcion = JOptionPane.showConfirmDialog(
                this,
                nombre + " ya está asignado al proyecto \"" + proyectoActual + "\".\n\n¿Deseas liberarlo (volver su estado a Activo)?",
                "Empleado ya asignado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (opcion == JOptionPane.YES_OPTION) {
                boolean ok = empleadoDAO.actualizarAsignacion(empSeleccionado.getId(), "Activo", "");
                if (ok) {
                    cargarDatosDesdeBD();
                    JOptionPane.showMessageDialog(this, nombre + " fue liberado del proyecto y vuelve a estar Activo.");
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar al empleado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }

        if (!"Activo".equals(estado)) {
            JOptionPane.showMessageDialog(
                this,
                "Solo puedes asignar a un proyecto a un empleado con estado \"Activo\".\nEste empleado está: " + estado + ".",
                "No disponible para asignar",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (PROYECTOS_DISPONIBLES.length == 0) {
            JOptionPane.showMessageDialog(this, "Todavía no hay proyectos registrados. Ve al módulo de Proyectos y agrega uno primero.", "Sin proyectos disponibles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(420, 260);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 420, 260, 24, 24));

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

        JLabel lblTitulo = new JLabel("Asignar a un proyecto", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 19f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 380, 30);
        panelFondo.add(lblTitulo);

        JLabel lblSubtitulo = new JLabel(nombre, SwingConstants.CENTER);
        lblSubtitulo.setFont(Tema.fuente(Font.PLAIN, 13f));
        lblSubtitulo.setForeground(new Color(110, 118, 125));
        lblSubtitulo.setBounds(20, 50, 380, 20);
        panelFondo.add(lblSubtitulo);

        JLabel lblProyecto = new JLabel("Proyecto");
        lblProyecto.setFont(Tema.fuente(Font.BOLD, 12f));
        lblProyecto.setBounds(50, 90, 320, 18);
        panelFondo.add(lblProyecto);

        JComboBox<String> comboProyecto = new JComboBox<>(PROYECTOS_DISPONIBLES);
        comboProyecto.setFont(Tema.fuente(Font.PLAIN, 14f));
        comboProyecto.setBounds(50, 110, 320, 36);
        panelFondo.add(comboProyecto);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(140, 175, 140, 35);
        btnGuardar.addActionListener(e -> {
            int indiceElegido = comboProyecto.getSelectedIndex();
            String proyectoElegido = (String) comboProyecto.getSelectedItem();
            Proyecto proyecto = proyectosDisponiblesObjetos.get(indiceElegido);

            boolean ok = empleadoDAO.actualizarAsignacion(empSeleccionado.getId(), "Ocupado", proyectoElegido);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, nombre + " fue asignado a \"" + proyectoElegido + "\".");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo asignar al empleado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelFondo.add(btnGuardar);
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        CampoUtils.estilizarBotonPrimario(btnGuardar, AZUL_ACCIONES);
        modal.getRootPane().setDefaultButton(btnGuardar);

        modal.add(panelFondo);
        modal.setVisible(true);
    }

    private void abrirModalDetalle(int filaVista) {
        int fila = tablaEmpleados.convertRowIndexToModel(filaVista);
        String id = String.valueOf(modeloTabla.getValueAt(fila, 0));
        String nombre = String.valueOf(modeloTabla.getValueAt(fila, 1));
        String cargo = String.valueOf(modeloTabla.getValueAt(fila, 2));
        String estado = String.valueOf(modeloTabla.getValueAt(fila, 3));
        String salario = String.valueOf(modeloTabla.getValueAt(fila, 4));
        String fecha = String.valueOf(modeloTabla.getValueAt(fila, 5));
        String telefono = String.valueOf(modeloTabla.getValueAt(fila, 6));
        String direccion = String.valueOf(modeloTabla.getValueAt(fila, 7));
        String proyecto = String.valueOf(modeloTabla.getValueAt(fila, 8));

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(420, 570);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 420, 570, 24, 24));

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

        JLabel lblTitulo = new JLabel("Detalle del empleado", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 380, 30);
        panelFondo.add(lblTitulo);

        String[] etiquetas = {"ID del empleado", "Nombre", "Cargo", "Salario", "Última actualización", "Teléfono", "Dirección"};
        String[] valores = {id, nombre, cargo, salario, fecha, telefono, direccion};

        int y = 65;
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(Tema.fuente(Font.BOLD, 12f));
            lbl.setBounds(50, y, 340, 18);
            panelFondo.add(lbl);

            JLabel val = new JLabel(valores[i]);
            val.setFont(Tema.fuente(Font.PLAIN, 15f));
            val.setBounds(50, y + 18, 340, 24);
            panelFondo.add(val);
            y += 52;
        }

        JLabel lblEstadoTitulo = new JLabel("Estado");
        lblEstadoTitulo.setFont(Tema.fuente(Font.BOLD, 12f));
        lblEstadoTitulo.setBounds(50, y, 340, 18);
        panelFondo.add(lblEstadoTitulo);

        JLabel badge = crearBadgeEstado(estado, colorPorEstadoEmpleado(estado));
        badge.setBounds(50, y + 20, 120, 26);
        panelFondo.add(badge);
        y += 60;

        JLabel lblProyectoTitulo = new JLabel("Proyecto asignado");
        lblProyectoTitulo.setFont(Tema.fuente(Font.BOLD, 12f));
        lblProyectoTitulo.setBounds(50, y, 340, 18);
        panelFondo.add(lblProyectoTitulo);

        JLabel lblProyectoValor = new JLabel(proyecto.isEmpty() ? "—" : proyecto);
        lblProyectoValor.setFont(Tema.fuente(Font.PLAIN, 15f));
        lblProyectoValor.setBounds(50, y + 18, 340, 24);
        panelFondo.add(lblProyectoValor);

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
        SwingUtilities.invokeLater(() -> new Empleados().setVisible(true));
    }
}
