package vista;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class CampoUtils {

    public static void soloDecimalPositivo(JTextField campo) {
        ((PlainDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String textoResultante = construirTextoResultante(fb, offset, 0, string);
                if (esDecimalPositivoValido(textoResultante)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String textoResultante = construirTextoResultante(fb, offset, length, text);
                if (esDecimalPositivoValido(textoResultante)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    public static void soloEnteroPositivo(JTextField campo) {
        ((PlainDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String textoResultante = construirTextoResultante(fb, offset, 0, string);
                if (textoResultante.matches("\\d*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String textoResultante = construirTextoResultante(fb, offset, length, text);
                if (textoResultante.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private static boolean esDecimalPositivoValido(String texto) {

        return texto.matches("\\d*\\.?\\d*");
    }

    private static String construirTextoResultante(DocumentFilter.FilterBypass fb, int offset, int length, String textoNuevo) throws BadLocationException {
        StringBuilder actual = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        actual.replace(offset, offset + length, textoNuevo == null ? "" : textoNuevo);
        return actual.toString();
    }

    public static void estilizarBotonPrimario(javax.swing.JButton boton, java.awt.Color colorBase) {
        java.awt.Color colorHover = colorBase.darker();
        boton.setBackground(colorBase);
        boton.setForeground(java.awt.Color.WHITE);
        boton.setFont(Tema.fuente(java.awt.Font.BOLD, 14f));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(colorHover);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(colorBase);
            }
        });
    }

    public static javax.swing.JButton crearBotonCerrarModal() {
        javax.swing.JButton boton = new javax.swing.JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new java.awt.Color(196, 60, 60) : new java.awt.Color(90, 98, 106));
                g2.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int margen = (int) (getWidth() * 0.32);
                g2.drawLine(margen, margen, getWidth() - margen, getHeight() - margen);
                g2.drawLine(getWidth() - margen, margen, margen, getHeight() - margen);
                g2.dispose();
            }
        };
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return boton;
    }
}
