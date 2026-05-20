package com.programacion.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ImagePreviewPanel extends JPanel {
    private BufferedImage image;
    private double scale = 1.0;
    private double fitScale = 1.0;
    private boolean fitToScreen = true;

    public ImagePreviewPanel() {
        setBackground(UIManager.getColor("Panel.background"));
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        // Rueda del ratón para Zoom
        addMouseWheelListener(e -> {
            if (image == null) return;
            fitToScreen = false;

            if (e.getWheelRotation() < 0) {
                scale *= 1.15; // Acercar
            } else {
                scale /= 1.15; // Alejar
            }

            // Limitar zoom
            if (scale < 0.05) scale = 0.05;
            if (scale > 20.0) scale = 20.0;

            revalidate();
            repaint();
        });

        // Doble clic para ajustar a pantalla
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && image != null) {
                    fitToScreen = true;
                    calculateFitScale();
                    scale = fitScale;
                    revalidate();
                    repaint();
                }
            }
        });

        // Manejar redimensionamiento de ventana
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (fitToScreen) {
                    calculateFitScale();
                    scale = fitScale;
                    revalidate();
                    repaint();
                }
            }
        });
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        if (fitToScreen || scale <= 0) {
            calculateFitScale();
            scale = fitScale;
        }
        revalidate();
        repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    private void calculateFitScale() {
        if (image == null) return;
        Container parent = getParent();
        if (parent instanceof JViewport) {
            parent = parent.getParent(); // Obtener el JScrollPane si es posible
        }
        if (parent != null) {
            int viewWidth = parent.getWidth() - 20; // Margen para scrollbars
            int viewHeight = parent.getHeight() - 20;

            if (viewWidth <= 0) viewWidth = 800;
            if (viewHeight <= 0) viewHeight = 600;

            double scaleX = (double) viewWidth / image.getWidth();
            double scaleY = (double) viewHeight / image.getHeight();
            fitScale = Math.min(scaleX, scaleY);
            if (fitScale > 1.0) fitScale = 1.0; // No agrandar imágenes pequeñas por defecto
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (image != null) {
            int w = (int) (image.getWidth() * scale);
            int h = (int) (image.getHeight() * scale);
            return new Dimension(w, h);
        }
        return super.getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (image != null) {
            int w = (int) (image.getWidth() * scale);
            int h = (int) (image.getHeight() * scale);

            // Centrar la imagen si el panel es más grande que la imagen redimensionada
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;

            g2d.drawImage(image, Math.max(0, x), Math.max(0, y), w, h, null);

            // Mostrar texto flotante con nivel de zoom si no está ajustado a la pantalla
            if (!fitToScreen) {
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(10, 10, 80, 25, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.format("Zoom: %d%%", (int)(scale * 100)), 20, 27);
            }
        } else {
            g2d.setColor(UIManager.getColor("Label.disabledForeground"));
            String text = "Sin imagen (Cargue una imagen para editar)";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(text, x, y);
        }
    }
}
