package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.regex.Pattern;

public class RecuperarContraseña extends JFrame {

    private final Color AZUL_FONDO_INICIO = new Color(6, 54, 83);     // #063653
    private final Color AZUL_FONDO_FIN = new Color(3, 27, 43);        // Azul oscuro profundo
    private final Color GRIS_TARJETA = new Color(217, 224, 230);      // #D9E0E6
    private final Color AZUL_BOTON = new Color(93, 170, 226);        // #5DAAE2
    private final Color AZUL_BOTON_HOVER = new Color(74, 149, 203);
    private final Color GRIS_TEXTO_PLACEHOLDER = new Color(117, 134, 148);

    private boolean hoverBoton = false;
    private final JFrame ventanaLogin; 

    // Variables para las animaciones (Botón y Fondo)
    private Timer animadorTimer;
    private double progresoAnimacion = 0.0; 
    private Timer animadorFondoTimer;
    private double desplazamientoFondo = 0.0;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public RecuperarContraseña(JFrame login) {
        this.ventanaLogin = login;

        setTitle("CisConstructora - Recuperar Contraseña");
        setSize(850, 550);
        setMinimumSize(new Dimension(800, 500)); 
        setResizable(true); 

        if (ventanaLogin != null) {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    ventanaLogin.setVisible(true);
                }
            });
        } else {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        
        setLocationRelativeTo(null);

        // =========================================================================
        // PANEL PRINCIPAL: CON LA MISMA ANIMACIÓN DE FONDO GEOMÉTRICO FLUIDO
        // =========================================================================
        JPanel panelFondo = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 1. Base del degradado dinámico
                GradientPaint degradado = new GradientPaint(
                    0, 0, AZUL_FONDO_INICIO, 
                    getWidth(), getHeight(), AZUL_FONDO_FIN
                );
                g2.setPaint(degradado);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // 2. Líneas geométricas con movimiento sutil (estilo plano/estructura)
                g2.setColor(new Color(255, 255, 255, 10)); 
                g2.setStroke(new BasicStroke(1.5f));
                
                int offsetDinamico = (int) (desplazamientoFondo);

                // Primer set de diagonales concurrentes
                for (int i = -500; i < getWidth() + 500; i += 300) {
                    g2.drawLine(i + offsetDinamico, 0, i - 300 + offsetDinamico, getHeight());
                }
                
                // Segundo set de líneas cruzadas (sentido opuesto)
                g2.setColor(new Color(255, 255, 255, 6)); 
                for (int i = -500; i < getWidth() + 500; i += 400) {
                    g2.drawLine(i - offsetDinamico / 2, 0, i + 400 - offsetDinamico / 2, getHeight());
                }

                g2.dispose();
            }
        };
        panelFondo.setFocusable(true);

        // MOTOR DE LA ANIMACIÓN DEL FONDO (Ciclo continuo)
        animadorFondoTimer = new Timer(20, e -> {
            desplazamientoFondo += 0.4; 
            if (desplazamientoFondo >= 600) {
                desplazamientoFondo = 0;
            }
            panelFondo.repaint(); 
        });
        animadorFondoTimer.start();

        // Contenedor interno vertical
        JPanel contenedorCentral = new JPanel();
        contenedorCentral.setOpaque(false);
        contenedorCentral.setLayout(new BoxLayout(contenedorCentral, BoxLayout.Y_AXIS));
        contenedorCentral.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ==========================================
        // FILA SUPERIOR: LOGO + TÍTULO
        // ==========================================
        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelEncabezado.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLogo = new JLabel();
        try {
            URL logoURL = getClass().getResource("/vista/LogoSinFondo.png");
            if (logoURL != null) {
                ImageIcon iconOriginal = new ImageIcon(logoURL);
                Image imgEscalada = iconOriginal.getImage().getScaledInstance(85, 85, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(imgEscalada));
            } else {
                lblLogo.setText("[Logo]");
                lblLogo.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            lblLogo.setText("[Logo]");
            lblLogo.setForeground(Color.WHITE);
        }
        
        JLabel lblTitulo = new JLabel("¿Olvidaste tu contraseña?");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));

        panelEncabezado.add(lblLogo);
        panelEncabezado.add(lblTitulo);
        contenedorCentral.add(panelEncabezado);

        contenedorCentral.add(Box.createVerticalStrut(25));

        // ==========================================
        // TEXTO DE INSTRUCCIONES
        // ==========================================
        JLabel lblInstrucciones = new JLabel("<html><center>Ingresa la dirección de correo electrónico para<br>recibir instrucciones para restablecer la contraseña</center></html>", SwingConstants.CENTER);
        lblInstrucciones.setForeground(new Color(180, 195, 205));
        lblInstrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInstrucciones.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedorCentral.add(lblInstrucciones);

        contenedorCentral.add(Box.createVerticalStrut(30));

        // ==========================================
        // INPUT: CORREO ELECTRÓNICO (Con Sombra)
        // ==========================================
        JPanel cardEmail = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                // Sombra sutil trasera
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fill(new RoundRectangle2D.Float(1, 2, getWidth() - 2, getHeight() - 2, 16, 16));
                
                // Cuerpo
                g2.setColor(GRIS_TARJETA);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(80, 95, 105));
                g2.drawRoundRect(25, 16, 20, 14, 2, 2);
                
                Path2D.Float sobre = new Path2D.Float();
                sobre.moveTo(25, 17);
                sobre.lineTo(35, 24);
                sobre.lineTo(45, 17);
                g2.draw(sobre);
                
                g2.dispose();
            }
        };
        cardEmail.setOpaque(false);
        cardEmail.setLayout(null);
        cardEmail.setPreferredSize(new Dimension(370, 48));
        cardEmail.setMaximumSize(new Dimension(370, 48));
        cardEmail.setMinimumSize(new Dimension(370, 48));
        cardEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtEmail = new JTextField("Correo electrónico");
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtEmail.setForeground(GRIS_TEXTO_PLACEHOLDER);
        txtEmail.setBorder(null);
        txtEmail.setBackground(GRIS_TARJETA);
        txtEmail.setBounds(60, 11, 290, 25);
        
        txtEmail.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtEmail.getText().equals("Correo electrónico")) {
                    txtEmail.setText("");
                    txtEmail.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtEmail.getText().trim().isEmpty()) {
                    txtEmail.setText("Correo electrónico");
                    txtEmail.setForeground(GRIS_TEXTO_PLACEHOLDER);
                }
            }
        });
        cardEmail.add(txtEmail);
        contenedorCentral.add(cardEmail);

        contenedorCentral.add(Box.createVerticalStrut(30));

        // ==========================================
        // BOTÓN: RESTABLECER (CON ANIMACIÓN AVANZADA)
        // ==========================================
        JButton btnRestablecer = new JButton("Restablecer contraseña") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Sombra del botón
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(1, 2, getWidth() - 2, getHeight() - 2, 14, 14));

                // Interpolación HSB exacta para el degradado del color hover
                float[] hsbBase = Color.RGBtoHSB(AZUL_BOTON.getRed(), AZUL_BOTON.getGreen(), AZUL_BOTON.getBlue(), null);
                float[] hsbHover = Color.RGBtoHSB(AZUL_BOTON_HOVER.getRed(), AZUL_BOTON_HOVER.getGreen(), AZUL_BOTON_HOVER.getBlue(), null);
                float h = hsbBase[0] + (float)((hsbHover[0] - hsbBase[0]) * progresoAnimacion);
                float s = hsbBase[1] + (float)((hsbHover[1] - hsbBase[1]) * progresoAnimacion);
                float b = hsbBase[2] + (float)((hsbHover[2] - hsbBase[2]) * progresoAnimacion);
                
                g2.setColor(Color.getHSBColor(h, s, b));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));

                String texto = getText();
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(texto)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(Color.BLACK);
                g2.drawString(texto, x, y);
                g2.dispose();
            }
        };
        btnRestablecer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRestablecer.setContentAreaFilled(false);
        btnRestablecer.setBorderPainted(false);
        btnRestablecer.setFocusPainted(false);
        btnRestablecer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Dimensiones iniciales del Layout
        btnRestablecer.setPreferredSize(new Dimension(250, 44));
        btnRestablecer.setMaximumSize(new Dimension(250, 44));
        btnRestablecer.setMinimumSize(new Dimension(250, 44));
        btnRestablecer.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedorCentral.add(btnRestablecer);

        // Motor de animación del botón a 60 FPS
        animadorTimer = new Timer(15, e -> {
            if (hoverBoton) {
                progresoAnimacion += 0.12;
                if (progresoAnimacion >= 1.0) { progresoAnimacion = 1.0; animadorTimer.stop(); }
            } else {
                progresoAnimacion -= 0.12;
                if (progresoAnimacion <= 0.0) { progresoAnimacion = 0.0; animadorTimer.stop(); }
            }

            // Calculo fluido de escalado adaptado al contenedor BoxLayout
            int anchoDinamico = (int) (250 + (20 * progresoAnimacion));
            int altoDinamico = (int) (44 + (6 * progresoAnimacion));
            int fuenteDinamica = (int) (14 + (1 * progresoAnimacion));

            btnRestablecer.setPreferredSize(new Dimension(anchoDinamico, altoDinamico));
            btnRestablecer.setMaximumSize(new Dimension(anchoDinamico, altoDinamico));
            btnRestablecer.setFont(new Font("Segoe UI", Font.BOLD, fuenteDinamica));
            
            // Revalida el layout para reflejar el cambio de tamaño suavemente
            contenedorCentral.revalidate();
            btnRestablecer.repaint();
        });

        btnRestablecer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { hoverBoton = true; animadorTimer.start(); }
            @Override
            public void mouseExited(MouseEvent e) { hoverBoton = false; animadorTimer.start(); }
        });

        btnRestablecer.addActionListener(e -> {
            String emailIngresado = txtEmail.getText().trim();

            if (emailIngresado.equals("Correo electrónico") || emailIngresado.isEmpty()) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
                JOptionPane.showMessageDialog(this, "Por favor, ingresa tu dirección de correo electrónico.", "Campo vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!EMAIL_PATTERN.matcher(emailIngresado).matches()) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
                JOptionPane.showMessageDialog(this, "La estructura del correo electrónico ingresado no es válida.\nEjemplo: usuario@dominio.com", "Correo inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("OptionPane.titleFont", new Font("Segoe UI", Font.BOLD, 15));

            String mensajeHtml = "<html><body style='width: 280px;'>"
                    + "<b>¡Se ha enviado un correo!</b><br><br>"
                    + "Por favor, sigue los pasos que se te indicarán en el mensaje "
                    + "para restablecer tu contraseña correctamente."
                    + "</body></html>";

            JOptionPane.showMessageDialog(this, mensajeHtml, "Verifica tu correo", JOptionPane.INFORMATION_MESSAGE);

            if (ventanaLogin != null) {
                ventanaLogin.setVisible(true);
            }
            dispose();
        });

        contenedorCentral.add(Box.createVerticalStrut(35));

        // ==========================================
        // ENLACE: INGRESAR (BOTÓN RETORNO)
        // ==========================================
        JButton btnIngresar = new JButton("Ingresar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnIngresar.setContentAreaFilled(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIngresar.setPreferredSize(new Dimension(400, 35));
        btnIngresar.setMaximumSize(new Dimension(400, 35));
        btnIngresar.setMinimumSize(new Dimension(400, 35));
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedorCentral.add(btnIngresar);

        btnIngresar.addActionListener(e -> {
            if (ventanaLogin != null) {
                ventanaLogin.setVisible(true);
            }
            dispose();
        });

        btnIngresar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 20)); btnIngresar.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 18)); btnIngresar.repaint(); }
        });

        // Configuración para mantener el bloque centrado en toda condición de pantalla
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panelFondo.add(contenedorCentral, gbc);

        add(panelFondo);
        SwingUtilities.invokeLater(() -> panelFondo.requestFocusInWindow());
    }
}