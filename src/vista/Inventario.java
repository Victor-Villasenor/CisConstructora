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

import controlador.MaterialDAO;
import controlador.NotificacionDAO;
import controlador.ProyectoDAO;
import controlador.Sesion;
import modelo.Material;
import modelo.Notificacion;
import modelo.Proyecto;

public class Inventario extends JFrame {

    private boolean accesoDenegado = false;

    private final Color AZUL_HEADER = new Color(6, 54, 83);
    private final Color AMARILLO_MENU = new Color(175, 140, 21);
    private final Color BLANCO_FONDO = Color.WHITE;
    private final Color AZUL_TARJETAS = new Color(13, 93, 148);
    private final Color AZUL_ACCIONES = Tema.AZUL_MEDIO;
    private final Color NARANJA_GUARDAR = new Color(240, 155, 25);
    private final Color ROJO_BAJO_STOCK = new Color(196, 60, 60);
    private final int UMBRAL_BAJO_STOCK = 10;
    private final String PLACEHOLDER_BUSCAR = "Buscar material u origen...";

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private List<Material> materialesActuales;

    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;
    private int filaHover = -1;

    private JPanel panelTablaContenedor;
    private CardLayout cardLayoutTabla;
    private JScrollPane scrollTabla;
    private JPanel panelEstadoVacio;

    private JLabel lblValorTotal;
    private JLabel lblValorBajoStock;
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
            if (nombreBoton.equals("INVENTARIO")) return;
            JFrame pantalla;
            switch (nombreBoton) {
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

    private class CantidadCellRenderer extends JPanel implements TableCellRenderer {
        CantidadCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 14));
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            int cantidad = (value instanceof Integer) ? (Integer) value : 0;

            JLabel lblNumero = new JLabel(String.valueOf(cantidad));
            lblNumero.setFont(Tema.fuente(Font.BOLD, 14f));
            lblNumero.setForeground(new Color(30, 35, 40));
            add(lblNumero);

            if (cantidad < UMBRAL_BAJO_STOCK) {
                JLabel badge = new JLabel("Bajo stock") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(ROJO_BAJO_STOCK);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badge.setOpaque(false);
                badge.setForeground(Color.WHITE);
                badge.setFont(Tema.fuente(Font.BOLD, 10f));
                badge.setBorder(new EmptyBorder(3, 8, 3, 8));
                add(badge);
            }

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

        JLabel lblMensaje = new JLabel("Aún no hay materiales registrados");
        lblMensaje.setFont(Tema.fuente(Font.BOLD, 16f));
        lblMensaje.setForeground(new Color(90, 98, 106));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblMensaje);
        contenido.add(Box.createVerticalStrut(4));

        JLabel lblSubmensaje = new JLabel("Agrega el primero para empezar a llevar el control de tu stock");
        lblSubmensaje.setFont(Tema.fuente(Font.PLAIN, 13f));
        lblSubmensaje.setForeground(new Color(150, 158, 166));
        lblSubmensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblSubmensaje);
        contenido.add(Box.createVerticalStrut(20));

        JButton btnAgregar = crearBotonAccion("+ Nuevo material", AZUL_ACCIONES);
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

    public Inventario() {

        if (controlador.Sesion.haySesion() && "Dueno".equals(controlador.Sesion.getUsuarioActual().getRol())) {
            accesoDenegado = true;
            return;
        }

        Tema.instalar();
        setTitle("CisConstructora - Panel de Inventario");
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
            @Override public void inicio() { /* ya estás en el inicio */ }
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

        JPanel panelMenuLateral = Chrome.menuLateral("INVENTARIO", m -> Chrome.abrir(this, m));
        panelPrincipal.add(panelMenuLateral, BorderLayout.WEST);

        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setOpaque(false);
        panelContenido.setBorder(new EmptyBorder(26, 34, 30, 34));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JPanel panelEncabezadoSeccion = new JPanel(new BorderLayout());
        panelEncabezadoSeccion.setOpaque(false);

        JLabel lblTituloSeccion = new JLabel("Inventario");
        lblTituloSeccion.setFont(Tema.fuente(Font.BOLD, 26f));
        lblTituloSeccion.setForeground(Color.BLACK);
        panelEncabezadoSeccion.add(Chrome.titulo("Inventario", "Existencias de material de todas las obras"), BorderLayout.WEST);

        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 18, 0);
        panelContenido.add(panelEncabezadoSeccion, gbc);

        JPanel panelTarjetas = new JPanel(new GridLayout(1, 3, 20, 0));
        panelTarjetas.setOpaque(false);

        JPanel tarjetaTotal = crearTarjetaKpi("Total de materiales", "0 tipos");
        lblValorTotal = (JLabel) tarjetaTotal.getComponent(1);
        panelTarjetas.add(tarjetaTotal);

        JPanel tarjetaBajoStock = crearTarjetaKpi("Materiales bajo stock", "0 materiales");
        lblValorBajoStock = (JLabel) tarjetaBajoStock.getComponent(1);
        panelTarjetas.add(tarjetaBajoStock);

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

        JLabel lblSubtitulo = new JLabel("Nivel de stock actual");
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

        JButton btnEliminar = crearBotonAccion("Eliminar", Tema.ROJO);
        btnEliminar.addActionListener(e -> abrirModalEliminar());
        panelBotonesDerecha.add(btnEliminar);

        JButton btnNuevo = crearBotonAccion("+ Nuevo", AZUL_ACCIONES);
        btnNuevo.addActionListener(e -> abrirModalAgregar());
        panelBotonesDerecha.add(btnNuevo);
        panelAccionesTabla.add(panelBotonesDerecha, BorderLayout.EAST);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        panelContenido.add(panelAccionesTabla, gbc);
        //Aqui se modifica para agregar una nueva vista en el inventario (paso 1)
        String[] columnas = {"Id Material", "Nombre", "Cantidad", "Unidad", "Precio", "Origen", "Ultima actualizacion"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Integer.class : String.class;
            }
        };

        tablaInventario = new JTable(modeloTabla);
        tablaInventario.setRowHeight(38);
        tablaInventario.setFont(Tema.fuente(Font.BOLD, 14f));
        tablaInventario.setGridColor(new Color(230, 230, 230));
        tablaInventario.setShowVerticalLines(false);
        tablaInventario.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(modeloTabla);
        tablaInventario.setRowSorter(sorter);

        for (int i = 0; i < tablaInventario.getColumnCount(); i++) {
            if (i == 2) {
                tablaInventario.getColumnModel().getColumn(i).setCellRenderer(new CantidadCellRenderer());
            } else {
                tablaInventario.getColumnModel().getColumn(i).setCellRenderer(new TextCellRenderer(i == 0));
            }
        }

        JTableHeader headerTabla = tablaInventario.getTableHeader();
        headerTabla.setBackground(AZUL_HEADER);
        headerTabla.setForeground(Color.WHITE);
        headerTabla.setFont(Tema.fuente(Font.BOLD, 14f));
        headerTabla.setPreferredSize(new Dimension(headerTabla.getWidth(), 40));
        headerTabla.setReorderingAllowed(false);

        tablaInventario.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tablaInventario.rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    tablaInventario.repaint();
                }
            }
        });
        tablaInventario.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                filaHover = -1;
                tablaInventario.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaInventario.rowAtPoint(e.getPoint());
                    if (fila != -1) abrirModalDetalle(fila);
                }
            }
        });

        scrollTabla = new JScrollPane(tablaInventario);
        scrollTabla.getViewport().setBackground(BLANCO_FONDO);
        Tema.estilizarTabla(tablaInventario, scrollTabla);
        tablaInventario.setPreferredScrollableViewportSize(new Dimension(100, 160));

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
    //Aqui el orden debe conincidir con el de arriba (2do paso)
    private void cargarDatosDesdeBD() {
        materialesActuales = materialDAO.listar();
        modeloTabla.setRowCount(0);
        for (Material mat : materialesActuales) {
            String idFormateado = String.format("MAT-%03d", mat.getId());
            String precioFormateado = String.format("$%.2f", mat.getPrecio());
            String fecha = (mat.getUltimaActualizacion() != null) ? mat.getUltimaActualizacion().toString() : "N/A";
            modeloTabla.addRow(new Object[]{
                idFormateado, mat.getNombre(), mat.getCantidad(), mat.getUnidad(),
                precioFormateado, mat.getOrigen(), fecha
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
                        || entry.getStringValue(5).toLowerCase().contains(texto);
            }
        };
        sorter.setRowFilter(filtro);
    }

    private void actualizarResumen() {
        int total = modeloTabla.getRowCount();
        int bajoStock = 0;
        for (int i = 0; i < total; i++) {
            Object valor = modeloTabla.getValueAt(i, 2);
            int cantidad = (valor instanceof Integer) ? (Integer) valor : 0;
            if (cantidad < UMBRAL_BAJO_STOCK) bajoStock++;
        }
        lblValorTotal.setText(total + (total == 1 ? " tipo" : " tipos"));
        lblValorBajoStock.setText(bajoStock + (bajoStock == 1 ? " material" : " materiales"));
        if (total > 0) {
            String nombre = String.valueOf(modeloTabla.getValueAt(total - 1, 1));
            Object cantidadVal = modeloTabla.getValueAt(total - 1, 2);
            String unidad = String.valueOf(modeloTabla.getValueAt(total - 1, 3));
            lblValorReciente.setText(nombre + " (" + cantidadVal + " " + unidad + ")");
        } else {
            lblValorReciente.setText("—");
        }
        actualizarVisibilidadTabla();
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
        return String.format("MAT-%03d", maxId + 1);
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

        JLabel lblTitulo = new JLabel("Agregar nuevo material", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 360, 30);
        panelFondo.add(lblTitulo);
        //Esta es la parte para la cajita de texto xd
        JComboBox<String> comboNombre = new JComboBox<>(CatalogoMateriales.getNombresMateriales());
        JTextField txtCantidad = new JTextField();
        CampoUtils.soloEnteroPositivo(txtCantidad);
        JComboBox<String> comboUnidad = new JComboBox<>(new String[]{"Sacos", "Toneladas", "Piezas"});
        comboUnidad.setEnabled(false);
        JTextField txtPrecio = new JTextField();
        CampoUtils.soloDecimalPositivo(txtPrecio);
        txtPrecio.setEditable(false);
        txtPrecio.setBackground(new Color(224, 228, 232));
        JTextField txtOrigen = new JTextField();
        txtOrigen.setEditable(false);
        txtOrigen.setBackground(new Color(224, 228, 232));

        Runnable aplicarDatosMaterial = () -> {
            String materialElegido = (String) comboNombre.getSelectedItem();
            CatalogoMateriales.DatosMaterial datos = CatalogoMateriales.getDatos(materialElegido);
            if (datos != null) {
                comboUnidad.setSelectedItem(datos.unidad);
                txtPrecio.setText(String.format("%.2f", datos.precio));
                txtOrigen.setText(datos.origen);
            }
        };
        comboNombre.addActionListener(ev -> aplicarDatosMaterial.run());
        aplicarDatosMaterial.run();

        List<Proyecto> proyectosDisponibles = proyectoDAO.listar();
        Vector<String> nombresProyectos = new Vector<>();
        nombresProyectos.add("Ninguno (stock general)");
        for (Proyecto p : proyectosDisponibles) nombresProyectos.add(p.getNombre());
        JComboBox<String> comboProyecto = new JComboBox<>(nombresProyectos);

        String[] etiquetas = {"Material", "Cantidad", "Unidad", "Precio unitario ($)", "Origen", "Cargar al proyecto"};
        JComponent[] campos = {comboNombre, txtCantidad, comboUnidad, txtPrecio, txtOrigen, comboProyecto};

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
            String nombre = (String) comboNombre.getSelectedItem();
            String cantidadTexto = txtCantidad.getText().trim();
            String unidad = (String) comboUnidad.getSelectedItem();
            String precioTexto = txtPrecio.getText().trim();
            String origen = txtOrigen.getText().trim();
            int indiceProyecto = comboProyecto.getSelectedIndex();

            if (cantidadTexto.isEmpty()) {
                JOptionPane.showMessageDialog(modal, "Por favor indica la cantidad.", "Campo incompleto", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cantidad;
            double precio;
            try {
                cantidad = Integer.parseInt(cantidadTexto);
                precio = Double.parseDouble(precioTexto.replace(",", "."));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(modal, "Cantidad y precio deben ser números válidos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(modal, "La cantidad debe ser mayor a 0.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Material nuevo = new Material(nombre, cantidad, unidad, precio, origen);

            if (indiceProyecto == 0) {

                guardarMaterial(modal, nuevo);
                return;
            }

            Proyecto proyecto = proyectosDisponibles.get(indiceProyecto - 1);
            nuevo.setIdProyecto(proyecto.getId());
            boolean ok = materialDAO.registrar(nuevo);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, "Material registrado y asignado a la obra \"" + proyecto.getNombre() + "\".");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo registrar el material.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelFondo.add(btnGuardar);
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        CampoUtils.estilizarBotonPrimario(btnGuardar, AZUL_ACCIONES);
        modal.getRootPane().setDefaultButton(btnGuardar);

        modal.add(panelFondo);
        modal.setVisible(true);
    }

    private void guardarMaterial(JDialog modal, Material nuevo) {
        boolean ok = materialDAO.registrar(nuevo);
        if (ok) {
            cargarDatosDesdeBD();
            JOptionPane.showMessageDialog(modal, "Material registrado correctamente.");
            modal.dispose();
        } else {
            JOptionPane.showMessageDialog(modal, "No se pudo registrar el material.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalModificar() {
        int filaSeleccionada = tablaInventario.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un material de la tabla para modificar.", "Ningún material seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaInventario.convertRowIndexToModel(filaSeleccionada);
        Material matSeleccionado = materialesActuales.get(fila);

        String idActual = String.format("MAT-%03d", matSeleccionado.getId());
        String nombreActual = matSeleccionado.getNombre();
        String cantidadActual = String.valueOf(matSeleccionado.getCantidad());
        String unidadActual = matSeleccionado.getUnidad();
        String precioActual = String.valueOf(matSeleccionado.getPrecio());
        String origenActual = matSeleccionado.getOrigen();

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(400, 550);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 400, 550, 24, 24));

        JPanel panelFondo = new JPanel(null);
        panelFondo.setBackground(new Color(233, 238, 243));

        JButton btnCerrar = CampoUtils.crearBotonCerrarModal();
        btnCerrar.setBounds(350, 15, 35, 35);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.addActionListener(e -> modal.dispose());
        panelFondo.add(btnCerrar);

        JLabel lblTitulo = new JLabel("Modificar Material", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 360, 30);
        panelFondo.add(lblTitulo);

        JTextField txtId = new JTextField(idActual);
        txtId.setEditable(false);
        txtId.setBackground(new Color(220, 220, 220));

        JComboBox<String> comboNombre = new JComboBox<>(CatalogoMateriales.getNombresMateriales());
        if (CatalogoMateriales.esMaterialConocido(nombreActual)) {
            comboNombre.setSelectedItem(nombreActual);
        }
        JTextField txtCantidad = new JTextField(cantidadActual);
        CampoUtils.soloEnteroPositivo(txtCantidad);
        JComboBox<String> comboUnidad = new JComboBox<>(new String[]{"Sacos", "Toneladas", "Piezas"});
        comboUnidad.setSelectedItem(unidadActual);
        comboUnidad.setEnabled(false);
        JTextField txtPrecio = new JTextField(precioActual);
        CampoUtils.soloDecimalPositivo(txtPrecio);
        txtPrecio.setEditable(false);
        txtPrecio.setBackground(new Color(224, 228, 232));
        JTextField txtOrigen = new JTextField(origenActual);
        txtOrigen.setEditable(false);
        txtOrigen.setBackground(new Color(224, 228, 232));

        comboNombre.addActionListener(ev -> {
            String materialElegido = (String) comboNombre.getSelectedItem();
            CatalogoMateriales.DatosMaterial datos = CatalogoMateriales.getDatos(materialElegido);
            if (datos != null) {
                comboUnidad.setSelectedItem(datos.unidad);
                txtPrecio.setText(String.format("%.2f", datos.precio));
                txtOrigen.setText(datos.origen);
            }
        });

        String[] etiquetas = {"Id Material", "Material", "Cantidad", "Unidad", "Precio unitario ($)", "Origen"};
        JComponent[] campos = {txtId, comboNombre, txtCantidad, comboUnidad, txtPrecio, txtOrigen};

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
            String nombre = (String) comboNombre.getSelectedItem();
            String cantidadTexto = txtCantidad.getText().trim();
            String unidad = (String) comboUnidad.getSelectedItem();
            String precioTexto = txtPrecio.getText().trim();
            String origen = txtOrigen.getText().trim();

            if (cantidadTexto.isEmpty()) {
                JOptionPane.showMessageDialog(modal, "Por favor indica la cantidad.", "Campo incompleto", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cantidad;
            double precio;
            try {
                cantidad = Integer.parseInt(cantidadTexto);
                precio = Double.parseDouble(precioTexto.replace(",", "."));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(modal, "Cantidad y precio deben ser números válidos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(modal, "La cantidad debe ser mayor a 0.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            matSeleccionado.setNombre(nombre);
            matSeleccionado.setCantidad(cantidad);
            matSeleccionado.setUnidad(unidad);
            matSeleccionado.setPrecio(precio);
            matSeleccionado.setOrigen(origen);

            boolean ok = materialDAO.modificar(matSeleccionado);
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(modal, "Material actualizado exitosamente.");
                modal.dispose();
            } else {
                JOptionPane.showMessageDialog(modal, "No se pudo actualizar el material.", "Error", JOptionPane.ERROR_MESSAGE);
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
        int filaSeleccionada = tablaInventario.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un material de la tabla para eliminar.", "Ningún material seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaInventario.convertRowIndexToModel(filaSeleccionada);
        Material matSeleccionado = materialesActuales.get(fila);

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Estás a punto de eliminar \"" + matSeleccionado.getNombre() + "\".\n" +
            "Esta acción NO se puede deshacer.\n\n¿Deseas continuar?",
            "Advertencia: acción irreversible",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean ok = materialDAO.eliminar(matSeleccionado.getId());
            if (ok) {
                cargarDatosDesdeBD();
                JOptionPane.showMessageDialog(this, "Material eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el material.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModalDetalle(int filaVista) {
        int fila = tablaInventario.convertRowIndexToModel(filaVista);
        String id = String.valueOf(modeloTabla.getValueAt(fila, 0));
        String nombre = String.valueOf(modeloTabla.getValueAt(fila, 1));
        int cantidad = (Integer) modeloTabla.getValueAt(fila, 2);
        String unidad = String.valueOf(modeloTabla.getValueAt(fila, 3));
        String precio = String.valueOf(modeloTabla.getValueAt(fila, 4));
        String origen = String.valueOf(modeloTabla.getValueAt(fila, 5));
        String fecha = String.valueOf(modeloTabla.getValueAt(fila, 6));

        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setSize(420, 480);
        modal.setLocationRelativeTo(this);
        modal.setShape(new RoundRectangle2D.Double(0, 0, 420, 480, 24, 24));

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

        JLabel lblTitulo = new JLabel("Detalle del material", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(6, 54, 83));
        lblTitulo.setBounds(20, 20, 380, 30);
        panelFondo.add(lblTitulo);

        String[] etiquetas = {"Id Material", "Nombre", "Precio", "Origen", "Última actualización"};
        String[] valores = {id, nombre, precio, origen, fecha};

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
            y += 55;
        }

        JLabel lblCantidadTitulo = new JLabel("Cantidad disponible");
        lblCantidadTitulo.setFont(Tema.fuente(Font.BOLD, 12f));
        lblCantidadTitulo.setBounds(50, y, 340, 18);
        panelFondo.add(lblCantidadTitulo);

        JPanel panelCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelCantidad.setOpaque(false);
        panelCantidad.setBounds(50, y + 18, 340, 30);

        JLabel lblCantidadValor = new JLabel(cantidad + " " + unidad);
        lblCantidadValor.setFont(Tema.fuente(Font.BOLD, 16f));
        lblCantidadValor.setForeground(new Color(30, 35, 40));
        panelCantidad.add(lblCantidadValor);

        if (cantidad < UMBRAL_BAJO_STOCK) {
            JLabel badge = new JLabel("Bajo stock") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ROJO_BAJO_STOCK);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setForeground(Color.WHITE);
            badge.setFont(Tema.fuente(Font.BOLD, 11f));
            badge.setBorder(new EmptyBorder(4, 10, 4, 10));
            panelCantidad.add(badge);
        }
        panelFondo.add(panelCantidad);

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
        SwingUtilities.invokeLater(() -> new Inventario().setVisible(true));
    }
}
