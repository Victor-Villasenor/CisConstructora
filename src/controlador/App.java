package controlador;

import vista.frontend;

public class App {
    public static void main(String[] args) throws Exception {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frontend ventanaLogin = new frontend();
                ventanaLogin.setVisible(true);
            }
        });
    }
}
