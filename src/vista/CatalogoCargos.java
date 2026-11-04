package vista;

import java.util.LinkedHashMap;
import java.util.Map;

public class CatalogoCargos {

    private static final Map<String, Double> CARGOS = new LinkedHashMap<>();
    static {
        CARGOS.put("Gerente de Obra", 15000.0);
        CARGOS.put("Ingeniero Civil", 12000.0);
        CARGOS.put("Arquitecto", 11000.0);
        CARGOS.put("Jefe de Obra", 9000.0);
        CARGOS.put("Electricista", 7500.0);
        CARGOS.put("Operador de Maquinaria", 7000.0);
        CARGOS.put("Plomero", 7000.0);
        CARGOS.put("Maestro de Obra", 8000.0);
        CARGOS.put("Administrativo", 6500.0);
        CARGOS.put("Albañil", 6000.0);
        CARGOS.put("Chofer", 5500.0);
        CARGOS.put("Chalán", 4500.0);
    }

    public static String[] getNombresCargos() {
        return CARGOS.keySet().toArray(new String[0]);
    }

    public static double getSalario(String cargo) {
        return CARGOS.getOrDefault(cargo, 0.0);
    }

    public static boolean esCargoConocido(String cargo) {
        return CARGOS.containsKey(cargo);
    }
}
