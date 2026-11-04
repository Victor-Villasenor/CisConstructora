package vista;

import java.util.LinkedHashMap;
import java.util.Map;

public class CatalogoMaquinaria {

    public static class DatosMaquinaria {
        public final String tipo;
        public final double costo;

        public DatosMaquinaria(String tipo, double costo) {
            this.tipo = tipo;
            this.costo = costo;
        }
    }

    private static final Map<String, DatosMaquinaria> ACTIVOS = new LinkedHashMap<>();
    static {
        ACTIVOS.put("Excavadora",              new DatosMaquinaria("Pesada",  850000.0));
        ACTIVOS.put("Retroexcavadora",         new DatosMaquinaria("Pesada",  620000.0));
        ACTIVOS.put("Revolvedora de concreto", new DatosMaquinaria("Pesada",   45000.0));
        ACTIVOS.put("Montacargas",             new DatosMaquinaria("Pesada",  380000.0));
        ACTIVOS.put("Grúa torre",              new DatosMaquinaria("Pesada", 1200000.0));
        ACTIVOS.put("Vibrador de concreto",    new DatosMaquinaria("Manual",    8500.0));
        ACTIVOS.put("Rotomartillo",            new DatosMaquinaria("Manual",    4200.0));
        ACTIVOS.put("Taladro industrial",      new DatosMaquinaria("Manual",    2800.0));
        ACTIVOS.put("Mazo",                    new DatosMaquinaria("Manual",     350.0));
        ACTIVOS.put("Carretilla",              new DatosMaquinaria("Manual",     900.0));
        ACTIVOS.put("Andamio",                 new DatosMaquinaria("Manual",    3500.0));
        ACTIVOS.put("Cortadora de varilla",    new DatosMaquinaria("Manual",    6000.0));
    }

    public static String[] getNombresActivos() {
        return ACTIVOS.keySet().toArray(new String[0]);
    }

    public static DatosMaquinaria getDatos(String nombreActivo) {
        return ACTIVOS.get(nombreActivo);
    }

    public static boolean esActivoConocido(String nombreActivo) {
        return ACTIVOS.containsKey(nombreActivo);
    }
}
