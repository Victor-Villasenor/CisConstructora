package modelo;

import java.time.LocalDateTime;

/**
 * Cubicación de una obra: con las medidas que captura el Supervisor se calcula
 * cuánto material necesita la casa. Ese resultado es el "tope" contra el que
 * se comparan las salidas de material.
 *
 * Los datos de la mezcla (agua, arena y cemento por m²) y la calidad del techo
 * los define el arquitecto o ingeniero; el sistema solo los pregunta.
 */
public class Cubicacion {

    // ---------------------------------------------------------------- constantes
    /** Una cubeta de arena (19 litros) en metros cúbicos. */
    public static final double M3_POR_CUBETA = 0.019;
    /** Un saco de cemento pesa 50 kg. */
    public static final double KG_POR_SACO = 50.0;

    public static final String MURO_BLOCK = "Block";
    public static final String MURO_LADRILLO = "Ladrillo";

    public static final String ENJARRE_AMBAS = "Interior y exterior";
    public static final String ENJARRE_EXTERIOR = "Solo exterior";
    public static final String ENJARRE_INTERIOR = "Solo interior";

    /** Calidades de concreto (f'c en kg/cm²). */
    public static final int[] CALIDADES = {150, 200, 250, 300};

    /** Materiales por m³ de concreto, valores de referencia: {cemento kg, arena m³, grava m³, agua L}. */
    private static final double[][] CONCRETO_POR_M3 = {
        {300, 0.55, 0.75, 205},   // f'c 150
        {350, 0.52, 0.72, 200},   // f'c 200
        {400, 0.50, 0.70, 195},   // f'c 250
        {450, 0.48, 0.68, 190}    // f'c 300
    };

    /** Piezas por m² que se sugieren según el tipo de muro (se pueden cambiar). */
    public static double piezasSugeridas(String tipoMuro) {
        return MURO_LADRILLO.equals(tipoMuro) ? 20.0 : 12.5;
    }

    // -------------------------------------------------------------------- datos
    private int id;
    private int idProyecto;
    private int idUsuario;
    private String nombreUsuario = "";
    private LocalDateTime fecha;

    // medidas que captura el Supervisor
    private double m2Totales;
    private double mlCimentacion;
    private double anchoCimentacion = 0.40;   // m
    private double alturaCimentacion = 0.30;  // m
    private double mlMuro;
    private double alturaMuro;                // m (altura de los huecos)
    private String tipoMuro = MURO_BLOCK;
    private double piezasPorM2 = 12.5;
    private String tipoEnjarre = ENJARRE_AMBAS;
    private double espesorTechoCm;
    private int calidadTecho = 200;

    // mezcla que define el arquitecto o ingeniero (por m² de enjarre)
    private double aguaPorM2;      // litros
    private double arenaPorM2;     // cubetas
    private double cementoPorM2;   // kg

    // ------------------------------------------------------------ resultado
    /** Cantidades por etapa. Índices de etapa: 0 cimentación, 1 muros, 2 enjarre, 3 techo. */
    public static class Resultado {
        public static final String[] ETAPAS = {"Cimentación", "Muros", "Enjarre", "Techo"};
        public static final String[] MATERIALES = {"Block / ladrillo", "Cemento", "Arena", "Grava", "Agua"};
        public static final String[] UNIDADES = {"piezas", "kg", "m³", "m³", "litros"};

        public double areaMuro;       // m²
        public double areaEnjarre;    // m²
        public double concretoCimentacion; // m³
        public double concretoTecho;       // m³
        /** [material][etapa] */
        public final double[][] cantidad = new double[MATERIALES.length][ETAPAS.length];

        public double total(int material) {
            double t = 0;
            for (double v : cantidad[material]) t += v;
            return t;
        }
        public double sacosCemento() { return total(1) / KG_POR_SACO; }
        public double concretoTotal() { return concretoCimentacion + concretoTecho; }
    }

    /** Revisa que los datos sean válidos. Devuelve el mensaje de lo que falta, o null si todo está bien. */
    public String validar() {
        if (!(m2Totales > 0)) return "Escribe los metros cuadrados totales.";
        if (!(mlCimentacion > 0)) return "Escribe los metros lineales de cimentación.";
        if (!(anchoCimentacion > 0) || !(alturaCimentacion > 0)) return "Escribe el ancho y la profundidad de la cimentación.";
        if (!(mlMuro > 0)) return "Escribe los metros lineales de muro.";
        if (!(alturaMuro > 0)) return "Escribe la altura de los muros.";
        if (!(piezasPorM2 > 0)) return "Escribe las piezas por metro cuadrado.";
        if (!(espesorTechoCm > 0)) return "Escribe el espesor del techo.";
        if (!(aguaPorM2 > 0)) return "Escribe los litros de agua por metro cuadrado.";
        if (!(arenaPorM2 > 0)) return "Escribe las cubetas de arena por metro cuadrado.";
        if (!(cementoPorM2 > 0)) return "Escribe los kilos de cemento por metro cuadrado.";
        return null;
    }

    private static int indiceCalidad(int fc) {
        for (int i = 0; i < CALIDADES.length; i++) if (CALIDADES[i] == fc) return i;
        return 1;
    }

    private int carasEnjarre() {
        return ENJARRE_AMBAS.equals(tipoEnjarre) ? 2 : 1;
    }

    /** Hace el cálculo. Se debe llamar solo cuando validar() devuelve null. */
    public Resultado calcular() {
        Resultado r = new Resultado();
        r.areaMuro = mlMuro * alturaMuro;
        r.areaEnjarre = r.areaMuro * carasEnjarre();
        r.concretoCimentacion = mlCimentacion * anchoCimentacion * alturaCimentacion;
        r.concretoTecho = m2Totales * (espesorTechoCm / 100.0);

        double[] mezcla = CONCRETO_POR_M3[indiceCalidad(calidadTecho)];

        // etapa 0: cimentación (concreto)
        // etapa 3: techo (concreto)
        double[] concretos = {r.concretoCimentacion, 0, 0, r.concretoTecho};
        for (int etapa : new int[]{0, 3}) {
            double m3 = concretos[etapa];
            r.cantidad[1][etapa] = m3 * mezcla[0];  // cemento kg
            r.cantidad[2][etapa] = m3 * mezcla[1];  // arena m³
            r.cantidad[3][etapa] = m3 * mezcla[2];  // grava m³
            r.cantidad[4][etapa] = m3 * mezcla[3];  // agua L
        }

        // etapa 1: muros (piezas)
        r.cantidad[0][1] = r.areaMuro * piezasPorM2;

        // etapa 2: enjarre (mezcla por m² que da el arquitecto)
        r.cantidad[1][2] = r.areaEnjarre * cementoPorM2;
        r.cantidad[2][2] = r.areaEnjarre * arenaPorM2 * M3_POR_CUBETA;
        r.cantidad[4][2] = r.areaEnjarre * aguaPorM2;
        return r;
    }

    // ------------------------------------------------------- getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String n) { this.nombreUsuario = n == null ? "" : n; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getM2Totales() { return m2Totales; }
    public void setM2Totales(double v) { this.m2Totales = v; }
    public double getMlCimentacion() { return mlCimentacion; }
    public void setMlCimentacion(double v) { this.mlCimentacion = v; }
    public double getAnchoCimentacion() { return anchoCimentacion; }
    public void setAnchoCimentacion(double v) { this.anchoCimentacion = v; }
    public double getAlturaCimentacion() { return alturaCimentacion; }
    public void setAlturaCimentacion(double v) { this.alturaCimentacion = v; }
    public double getMlMuro() { return mlMuro; }
    public void setMlMuro(double v) { this.mlMuro = v; }
    public double getAlturaMuro() { return alturaMuro; }
    public void setAlturaMuro(double v) { this.alturaMuro = v; }
    public String getTipoMuro() { return tipoMuro; }
    public void setTipoMuro(String v) { this.tipoMuro = v; }
    public double getPiezasPorM2() { return piezasPorM2; }
    public void setPiezasPorM2(double v) { this.piezasPorM2 = v; }
    public String getTipoEnjarre() { return tipoEnjarre; }
    public void setTipoEnjarre(String v) { this.tipoEnjarre = v; }
    public double getEspesorTechoCm() { return espesorTechoCm; }
    public void setEspesorTechoCm(double v) { this.espesorTechoCm = v; }
    public int getCalidadTecho() { return calidadTecho; }
    public void setCalidadTecho(int v) { this.calidadTecho = v; }
    public double getAguaPorM2() { return aguaPorM2; }
    public void setAguaPorM2(double v) { this.aguaPorM2 = v; }
    public double getArenaPorM2() { return arenaPorM2; }
    public void setArenaPorM2(double v) { this.arenaPorM2 = v; }
    public double getCementoPorM2() { return cementoPorM2; }
    public void setCementoPorM2(double v) { this.cementoPorM2 = v; }
}
