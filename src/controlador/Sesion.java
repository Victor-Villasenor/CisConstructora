package controlador;

import modelo.Cuenta;

public class Sesion {

    private static Cuenta usuarioActual;

    public static Cuenta getUsuarioActual() {
        return usuarioActual;
    }

    public static void iniciar(Cuenta cuenta) {
        usuarioActual = cuenta;
    }

    public static void cerrar() {
        usuarioActual = null;
    }

    public static boolean haySesion() {
        return usuarioActual != null;
    }

    public static String getRol() {
        return usuarioActual.getRol();
    }
}
