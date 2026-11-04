package vista;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;

/**
 * Recuerda si la ventana está en pantalla completa (y su tamaño normal) para que
 * al cambiar de pantalla se conserve igual.
 */
public class EstadoVentana {

    private static boolean maximizada = false;
    private static boolean definido = false;
    private static Rectangle limites;

    public static boolean isMaximizada() {
        return maximizada;
    }

    public static void setMaximizada(boolean valor) {
        maximizada = valor;
        definido = true;
    }

    /** Hace que la ventana vaya guardando su estado cada vez que cambia. */
    public static void seguir(JFrame f) {
        f.addWindowStateListener(e ->
            setMaximizada((e.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH));
        f.addComponentListener(new ComponentAdapter() {
            private void guardar() {
                if (!f.isShowing() || (f.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) return;
                Rectangle pantalla = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                Rectangle b = f.getBounds();
                // si ocupa toda la pantalla es que se está maximizando: no es un tamaño "normal"
                if (b.width >= pantalla.width && b.height >= pantalla.height) return;
                limites = b;
            }
            @Override public void componentResized(ComponentEvent e) { guardar(); }
            @Override public void componentMoved(ComponentEvent e) { guardar(); }
        });
    }

    /** Pone la ventana igual que la anterior (pantalla completa o tamaño normal). */
    public static void aplicar(JFrame f, boolean maximizadaPorDefecto) {
        if (limites != null) {
            f.setBounds(limites);
        } else {
            f.setLocationRelativeTo(null);
        }
        boolean max = definido ? maximizada : maximizadaPorDefecto;
        f.setExtendedState(max ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
    }
}
