package vista;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import controlador.CubicacionDAO;
import controlador.ProyectoDAO;
import controlador.Sesion;
import modelo.Cubicacion;
import modelo.Cubicacion.Resultado;
import modelo.Cuenta;
import modelo.Proyecto;

/**
 * Cubicación de la obra del Supervisor: captura las medidas y el sistema calcula,
 * en vivo, cuánto material necesita cada etapa. Al guardar queda como el tope
 * de la obra y la versión anterior pasa al historial.
 */
public class PantallaCubicacion extends JFrame {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CubicacionDAO dao = new CubicacionDAO();
    private Proyecto obra;
    private Cubicacion vigente;

    private final JTextField fM2 = campo(), fMlCim = campo(), fAncho = campo(), fAlto = campo(),
        fMlMuro = campo(), fAltMuro = campo(), fPiezas = campo(), fEspesor = campo(),
        fAgua = campo(), fArena = campo(), fCemento = campo();
    private final JComboBox<String> cTipoMuro = new JComboBox<>(new String[]{Cubicacion.MURO_BLOCK, Cubicacion.MURO_LADRILLO});
    private final JComboBox<String> cEnjarre = new JComboBox<>(new String[]{
        Cubicacion.ENJARRE_AMBAS, Cubicacion.ENJARRE_EXTERIOR, Cubicacion.ENJARRE_INTERIOR});
    private final JComboBox<String> cCalidad = new JComboBox<>(new String[]{
        "f'c 150 kg/cm²", "f'c 200 kg/cm²", "f'c 250 kg/cm²", "f'c 300 kg/cm²"});

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
        new String[]{"Material", "Cimentación", "Muros", "Enjarre", "Techo", "TOTAL", "Unidad"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JLabel lblEstado = new JLabel();
    private final JLabel lblAviso = new JLabel();
    private final JLabel[] kpi = new JLabel[4];
    private boolean cargando;

    public PantallaCubicacion() {
        this(buscarObraDelSupervisor());
        if (obra != null) {
            Cubicacion c = dao.obtenerActiva(obra.getId());
            if (c != null) { vigente = c; cargar(c); }
            actualizarBanner();
        }
    }

    /** Constructor también usado para pruebas con una obra ya armada. */
    PantallaCubicacion(Proyecto obra) {
        this.obra = obra;
        setTitle("CisConstructora - Cubicación");
        Tema.instalar();
        setSize(1240, 780);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(Chrome.encabezado(() -> 0, Chrome.accionesBasicas(this)), BorderLayout.NORTH);
        raiz.add(Chrome.menuLateral("CUBICACION", m -> Chrome.abrir(this, m)), BorderLayout.WEST);
        raiz.add(obra == null ? sinObra() : contenido(), BorderLayout.CENTER);
        setContentPane(raiz);
        EstadoVentana.seguir(this);
        EstadoVentana.aplicar(this, true);
        if (obra != null) { cargarPorDefecto(); recalcular(); }
    }

    private static Proyecto buscarObraDelSupervisor() {
        Cuenta c = Sesion.haySesion() ? Sesion.getUsuarioActual() : null;
        return c == null ? null : new ProyectoDAO().buscarPorSupervisor(c.getId());
    }

    // ------------------------------------------------------------ construcción
    private static JTextField campo() {
        JTextField f = new JTextField();
        f.setHorizontalAlignment(JTextField.RIGHT);
        return f;
    }

    private JPanel sinObra() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel("<html><div style='text-align:center'><b style='font-size:20px'>Todavía no tienes una obra asignada</b>"
            + "<br><br>Cuando el Dueño te asigne una podrás cubicarla aquí.</div></html>", SwingConstants.CENTER);
        l.setFont(Tema.fuente(Font.PLAIN, 15f));
        l.setForeground(Tema.TEXTO_SUAVE);
        p.add(l);
        return p;
    }

    private JPanel contenido() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(18, 28, 22, 28));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(Chrome.titulo("Cubicación", "Obra: " + obra.getNombre() + " · calcula el material que necesita la casa"), BorderLayout.WEST);
        lblEstado.setFont(Tema.fuente(Font.BOLD, 12.5f));
        lblEstado.setHorizontalAlignment(SwingConstants.RIGHT);
        arriba.add(lblEstado, BorderLayout.EAST);
        p.add(arriba, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH; g.weighty = 1; g.gridy = 0;
        g.gridx = 0; g.weightx = 0.44; g.insets = new Insets(0, 0, 0, 14);
        centro.add(formulario(), g);
        g.gridx = 1; g.weightx = 0.56; g.insets = new Insets(0, 0, 0, 0);
        centro.add(resultados(), g);
        p.add(centro, BorderLayout.CENTER);

        // eventos
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalcular(); }
            public void removeUpdate(DocumentEvent e) { recalcular(); }
            public void changedUpdate(DocumentEvent e) { recalcular(); }
        };
        for (JTextField f : new JTextField[]{fM2, fMlCim, fAncho, fAlto, fMlMuro, fAltMuro, fPiezas, fEspesor, fAgua, fArena, fCemento})
            f.getDocument().addDocumentListener(dl);
        cTipoMuro.addActionListener(e -> {
            if (!cargando) { fPiezas.setText(fmt(Cubicacion.piezasSugeridas((String) cTipoMuro.getSelectedItem()))); }
            recalcular();
        });
        cEnjarre.addActionListener(e -> recalcular());
        cCalidad.addActionListener(e -> recalcular());
        return p;
    }

    private JComponent formulario() {
        Tema.Tarjeta t = new Tema.Tarjeta(18);
        t.setLayout(new BorderLayout());

        class Formulario extends JPanel implements Scrollable {
            Formulario() { super(new GridBagLayout()); }
            public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
            public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 80; }
            public boolean getScrollableTracksViewportWidth() { return true; }
            public boolean getScrollableTracksViewportHeight() { return false; }
        }
        JPanel f = new Formulario();
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(3, 0, 3, 0);

        seccion(f, g, "MEDIDAS DE LA CASA");
        fila(f, g, "Superficie total (m²)", fM2);
        seccion(f, g, "CIMENTACIÓN");
        fila(f, g, "Cimentación (ml)", fMlCim);
        fila(f, g, "Ancho (m)", fAncho);
        fila(f, g, "Profundidad (m)", fAlto);
        seccion(f, g, "MUROS");
        fila(f, g, "Muro (ml)", fMlMuro);
        fila(f, g, "Altura del muro (m)", fAltMuro);
        fila(f, g, "Tipo de muro", cTipoMuro);
        fila(f, g, "Piezas por m²", fPiezas);
        seccion(f, g, "ENJARRE (mezcla que define el arquitecto)");
        fila(f, g, "Enjarre en", cEnjarre);
        fila(f, g, "Agua (L por m²)", fAgua);
        fila(f, g, "Arena (cubetas/m²)", fArena);
        fila(f, g, "Cemento (kg por m²)", fCemento);
        seccion(f, g, "TECHO");
        fila(f, g, "Espesor (cm)", fEspesor);
        fila(f, g, "Calidad del concreto", cCalidad);

        g.gridx = 0; g.gridwidth = 2; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        JPanel relleno = new JPanel(); relleno.setOpaque(false);
        f.add(relleno, g);
        JScrollPane sp = new JScrollPane(f, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        t.add(sp, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> sp.getVerticalScrollBar().setValue(0));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botones.setOpaque(false);
        JButton ejemplo = Tema.botonAccion("Usar ejemplo", Tema.AZUL_MEDIO);
        JButton limpiar = Tema.botonAccion("Limpiar", new Color(120, 135, 148));
        ejemplo.addActionListener(e -> usarEjemplo());
        limpiar.addActionListener(e -> limpiar());
        botones.add(limpiar);
        botones.add(ejemplo);
        t.add(botones, BorderLayout.SOUTH);
        return t;
    }

    private void seccion(JPanel f, GridBagConstraints g, String titulo) {
        JLabel l = new JLabel(titulo);
        l.setFont(Tema.fuente(Font.BOLD, 11f));
        l.setForeground(Tema.DORADO);
        g.gridx = 0; g.gridwidth = 2; g.weightx = 1; g.insets = new Insets(12, 0, 2, 0);
        f.add(l, g);
        g.gridy++;
        g.gridwidth = 1; g.insets = new Insets(3, 0, 3, 0);
    }

    private void fila(JPanel f, GridBagConstraints g, String etiqueta, JComponent c) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(Tema.fuente(Font.PLAIN, 13f));
        l.setForeground(Tema.TEXTO);
        g.gridx = 0; g.weightx = 1;
        f.add(l, g);
        c.setPreferredSize(new Dimension(150, 34));
        g.gridx = 1; g.weightx = 0;
        f.add(c, g);
        g.gridy++;
    }

    private JComponent resultados() {
        JPanel col = new JPanel(new BorderLayout(0, 14));
        col.setOpaque(false);

        // KPIs
        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        String[] titulos = {"Block / ladrillo", "Sacos de cemento", "Concreto total", "Área de enjarre"};
        for (int i = 0; i < 4; i++) {
            JPanel k = Tema.tarjetaKpi(titulos[i], "—");
            k.setPreferredSize(new Dimension(150, 92));
            kpi[i] = (JLabel) k.getComponent(1);
            kpis.add(k);
        }
        col.add(kpis, BorderLayout.NORTH);

        // tabla
        Tema.Tarjeta t = new Tema.Tarjeta(18);
        t.setLayout(new BorderLayout());
        JLabel tit = new JLabel("MATERIAL NECESARIO POR ETAPA");
        tit.setFont(Tema.fuente(Font.BOLD, 11f));
        tit.setForeground(Tema.DORADO);
        tit.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 20));
        t.add(tit, BorderLayout.NORTH);

        JTable tabla = new JTable(modeloTabla);
        JScrollPane sp = new JScrollPane(tabla);
        Tema.estilizarTabla(tabla, sp);
        sp.setPreferredSize(new Dimension(100, 100));
        sp.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        tabla.setRowHeight(40);
        javax.swing.table.DefaultTableCellRenderer der = new javax.swing.table.DefaultTableCellRenderer();
        der.setHorizontalAlignment(SwingConstants.RIGHT);
        der.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        for (int c = 1; c <= 5; c++) tabla.getColumnModel().getColumn(c).setCellRenderer(der);
        javax.swing.table.DefaultTableCellRenderer izq = new javax.swing.table.DefaultTableCellRenderer();
        izq.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        tabla.getColumnModel().getColumn(0).setCellRenderer(izq);
        tabla.getColumnModel().getColumn(6).setCellRenderer(izq);
        t.add(sp, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(6, 20, 10, 14));
        lblAviso.setFont(Tema.fuente(Font.PLAIN, 12.5f));
        lblAviso.setForeground(Tema.TEXTO_SUAVE);
        pie.add(lblAviso, BorderLayout.CENTER);
        JPanel bts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bts.setOpaque(false);
        JButton hist = Tema.botonAccion("Historial", Tema.AZUL_MEDIO);
        JButton guardar = Tema.botonAccion("Guardar cubicación", Tema.AZUL);
        hist.addActionListener(e -> mostrarHistorial());
        guardar.addActionListener(e -> guardar());
        bts.add(hist);
        bts.add(guardar);
        pie.add(bts, BorderLayout.EAST);
        t.add(pie, BorderLayout.SOUTH);
        col.add(t, BorderLayout.CENTER);
        return col;
    }

    // ------------------------------------------------------------------ lógica
    static String fmt(double v) {
        if (v == Math.rint(v)) return String.valueOf((long) v);
        return String.format(java.util.Locale.US, "%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static double num(JTextField f) {
        try {
            String s = f.getText().trim().replace(',', '.');
            return s.isEmpty() ? 0 : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private Cubicacion leer() {
        Cubicacion c = new Cubicacion();
        c.setM2Totales(num(fM2));
        c.setMlCimentacion(num(fMlCim));
        c.setAnchoCimentacion(num(fAncho));
        c.setAlturaCimentacion(num(fAlto));
        c.setMlMuro(num(fMlMuro));
        c.setAlturaMuro(num(fAltMuro));
        c.setTipoMuro((String) cTipoMuro.getSelectedItem());
        c.setPiezasPorM2(num(fPiezas));
        c.setTipoEnjarre((String) cEnjarre.getSelectedItem());
        c.setEspesorTechoCm(num(fEspesor));
        c.setCalidadTecho(Cubicacion.CALIDADES[Math.max(0, cCalidad.getSelectedIndex())]);
        c.setAguaPorM2(num(fAgua));
        c.setArenaPorM2(num(fArena));
        c.setCementoPorM2(num(fCemento));
        if (obra != null) c.setIdProyecto(obra.getId());
        if (Sesion.haySesion()) c.setIdUsuario(Sesion.getUsuarioActual().getId());
        return c;
    }

    private void cargar(Cubicacion c) {
        cargando = true;
        fM2.setText(fmt(c.getM2Totales()));
        fMlCim.setText(fmt(c.getMlCimentacion()));
        fAncho.setText(fmt(c.getAnchoCimentacion()));
        fAlto.setText(fmt(c.getAlturaCimentacion()));
        fMlMuro.setText(fmt(c.getMlMuro()));
        fAltMuro.setText(fmt(c.getAlturaMuro()));
        cTipoMuro.setSelectedItem(c.getTipoMuro());
        fPiezas.setText(fmt(c.getPiezasPorM2()));
        cEnjarre.setSelectedItem(c.getTipoEnjarre());
        fEspesor.setText(fmt(c.getEspesorTechoCm()));
        for (int i = 0; i < Cubicacion.CALIDADES.length; i++)
            if (Cubicacion.CALIDADES[i] == c.getCalidadTecho()) cCalidad.setSelectedIndex(i);
        fAgua.setText(fmt(c.getAguaPorM2()));
        fArena.setText(fmt(c.getArenaPorM2()));
        fCemento.setText(fmt(c.getCementoPorM2()));
        cargando = false;
        recalcular();
    }

    private void cargarPorDefecto() {
        cargando = true;
        for (JTextField f : new JTextField[]{fM2, fMlCim, fMlMuro, fAltMuro, fEspesor, fAgua, fArena, fCemento}) f.setText("");
        fAncho.setText("0.4");
        fAlto.setText("0.3");
        fPiezas.setText(fmt(Cubicacion.piezasSugeridas(Cubicacion.MURO_BLOCK)));
        cTipoMuro.setSelectedIndex(0);
        cEnjarre.setSelectedIndex(0);
        cCalidad.setSelectedIndex(1);
        cargando = false;
    }

    private void limpiar() {
        cargarPorDefecto();
        recalcular();
    }

    /** Casa de 8 x 10 m con muros de 3 m de altura. */
    private void usarEjemplo() {
        Cubicacion c = new Cubicacion();
        c.setM2Totales(80); c.setMlCimentacion(46); c.setAnchoCimentacion(0.4); c.setAlturaCimentacion(0.3);
        c.setMlMuro(46); c.setAlturaMuro(3); c.setTipoMuro(Cubicacion.MURO_BLOCK); c.setPiezasPorM2(12.5);
        c.setTipoEnjarre(Cubicacion.ENJARRE_AMBAS); c.setEspesorTechoCm(10); c.setCalidadTecho(200);
        c.setAguaPorM2(9); c.setArenaPorM2(1.5); c.setCementoPorM2(4.4);
        cargar(c);
    }

    private static String n(double v, int dec) {
        return String.format(java.util.Locale.US, "%,." + dec + "f", v);
    }

    private Resultado ultimo;

    private void recalcular() {
        if (cargando) return;
        modeloTabla.setRowCount(0);
        Cubicacion c = leer();
        String err = nanTest(c) ? "Revisa que todos los campos tengan solo números." : c.validar();
        if (err != null) {
            ultimo = null;
            for (JLabel k : kpi) k.setText("—");
            lblAviso.setText(err);
            return;
        }
        Resultado r = c.calcular();
        ultimo = r;
        for (int m = 0; m < Resultado.MATERIALES.length; m++) {
            int dec = m == 0 ? 0 : (m == 2 || m == 3 ? 2 : 1);
            Object[] fila = new Object[7];
            fila[0] = Resultado.MATERIALES[m];
            for (int e = 0; e < 4; e++) fila[e + 1] = r.cantidad[m][e] == 0 ? "—" : n(r.cantidad[m][e], dec);
            fila[5] = n(m == 0 ? Math.ceil(r.total(m)) : r.total(m), dec);
            fila[6] = "   " + Resultado.UNIDADES[m];
            modeloTabla.addRow(fila);
        }
        kpi[0].setText(n(Math.ceil(r.total(0)), 0) + " pzas");
        kpi[1].setText(n(Math.ceil(r.sacosCemento()), 0) + " sacos");
        kpi[2].setText(n(r.concretoTotal(), 2) + " m³");
        kpi[3].setText(n(r.areaEnjarre, 1) + " m²");
        lblAviso.setText("<html>Muro: " + n(r.areaMuro, 1) + " m² · Enjarre: " + n(r.areaEnjarre, 1)
            + " m² · Los totales se redondean hacia arriba.</html>");
    }

    private static boolean nanTest(Cubicacion c) {
        double[] v = {c.getM2Totales(), c.getMlCimentacion(), c.getAnchoCimentacion(), c.getAlturaCimentacion(), c.getMlMuro(),
            c.getAlturaMuro(), c.getPiezasPorM2(), c.getEspesorTechoCm(), c.getAguaPorM2(), c.getArenaPorM2(), c.getCementoPorM2()};
        for (double d : v) if (Double.isNaN(d)) return true;
        return false;
    }

    private void actualizarBanner() {
        if (vigente != null && vigente.getFecha() != null) {
            lblEstado.setForeground(Tema.VERDE);
            lblEstado.setText("● Cubicación vigente · " + vigente.getFecha().format(FECHA)
                + (vigente.getNombreUsuario().isEmpty() ? "" : " · " + vigente.getNombreUsuario()));
        } else if (vigente != null) {
            lblEstado.setForeground(Tema.VERDE);
            lblEstado.setText("● Cubicación vigente guardada");
        } else {
            lblEstado.setForeground(Tema.DORADO);
            lblEstado.setText("● Esta obra todavía no está cubicada");
        }
    }

    private void guardar() {
        Cubicacion c = leer();
        String err = nanTest(c) ? "Revisa que todos los campos tengan solo números." : c.validar();
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String msg = vigente == null
            ? "¿Guardar esta cubicación? Será el tope de material de la obra."
            : "Esta obra ya tiene una cubicación vigente.\nSi guardas, la nueva la reemplaza y la anterior queda en el historial.\n\n¿Continuar?";
        if (JOptionPane.showConfirmDialog(this, msg, "Guardar cubicación", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (dao.guardar(c)) {
            vigente = dao.obtenerActiva(obra.getId());
            actualizarBanner();
            JOptionPane.showMessageDialog(this, "Cubicación guardada.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo guardar. Revisa la conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarHistorial() {
        List<Cubicacion> lista = dao.historial(obra.getId());
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todavía no hay cubicaciones guardadas para esta obra.");
            return;
        }
        DefaultTableModel m = new DefaultTableModel(new String[]{"Fecha", "Guardó", "m²", "Block / ladrillo", "Sacos de cemento", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int i = 0; i < lista.size(); i++) {
            Cubicacion c = lista.get(i);
            LocalDateTime f = c.getFecha();
            String blk = "—", sacos = "—";
            if (c.validar() == null) {
                Resultado r = c.calcular();
                blk = n(Math.ceil(r.total(0)), 0);
                sacos = n(Math.ceil(r.sacosCemento()), 0);
            }
            m.addRow(new Object[]{f == null ? "—" : f.format(FECHA), c.getNombreUsuario(), fmt(c.getM2Totales()), blk, sacos,
                i == 0 ? "Vigente" : "Anterior"});
        }
        JTable t = new JTable(m);
        JScrollPane sp = new JScrollPane(t);
        Tema.estilizarTabla(t, sp);
        sp.setPreferredSize(new Dimension(680, 280));
        JOptionPane.showMessageDialog(this, sp, "Historial de cubicaciones", JOptionPane.PLAIN_MESSAGE);
    }
}
