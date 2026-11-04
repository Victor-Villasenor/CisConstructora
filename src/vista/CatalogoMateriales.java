package vista;

import java.util.LinkedHashMap;
import java.util.Map;

public class CatalogoMateriales {

    public static class DatosMaterial {
        public final String unidad;
        public final double precio;
        public final String origen;

        public DatosMaterial(String unidad, double precio, String origen) {
            this.unidad = unidad;
            this.precio = precio;
            this.origen = origen;
        }
    }

    private static final Map<String, DatosMaterial> MATERIALES = new LinkedHashMap<>();
    static {
        MATERIALES.put("Cemento gris",         new DatosMaterial("Sacos",      185.0, "Aglomerante"));
        MATERIALES.put("Cal hidratada",        new DatosMaterial("Sacos",       95.0, "Aglomerante"));
        MATERIALES.put("Yeso",                 new DatosMaterial("Sacos",      110.0, "Mineral"));
        MATERIALES.put("Arena",                new DatosMaterial("Toneladas",  350.0, "Pétreo"));
        MATERIALES.put("Grava",                new DatosMaterial("Toneladas",  380.0, "Pétreo"));
        MATERIALES.put("Piedra brasa",         new DatosMaterial("Toneladas",  420.0, "Pétreo"));
        MATERIALES.put("Tepetate",             new DatosMaterial("Toneladas",  280.0, "Pétreo"));
        MATERIALES.put("Block hueco",          new DatosMaterial("Piezas",      12.0, "Cerámico"));
        MATERIALES.put("Ladrillo rojo",        new DatosMaterial("Piezas",       6.0, "Cerámico"));
        MATERIALES.put("Varilla 3/8\"",        new DatosMaterial("Piezas",      95.0, "Metálico"));
        MATERIALES.put("Varilla 1/2\"",        new DatosMaterial("Piezas",     145.0, "Metálico"));
        MATERIALES.put("Malla electrosoldada", new DatosMaterial("Piezas",     280.0, "Metálico"));
        MATERIALES.put("Alambre recocido",     new DatosMaterial("Piezas",      65.0, "Metálico"));
    }

    public static String[] getNombresMateriales() {
        return MATERIALES.keySet().toArray(new String[0]);
    }

    public static DatosMaterial getDatos(String nombreMaterial) {
        return MATERIALES.get(nombreMaterial);
    }

    public static boolean esMaterialConocido(String nombreMaterial) {
        return MATERIALES.containsKey(nombreMaterial);
    }
}
