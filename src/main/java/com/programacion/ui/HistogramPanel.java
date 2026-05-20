package com.programacion.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class HistogramPanel extends JPanel {
    private BufferedImage image;
    private int[] histoRed = new int[256];
    private int[] histoGreen = new int[256];
    private int[] histoBlue = new int[256];
    private int maxAbsoluto = 0;
    private boolean isOverlay = false;
    private Thread calcThread = null;

    public enum Mode { RGB, RED, GREEN, BLUE }
    private Mode currentMode = Mode.RGB;

    public HistogramPanel() {
        setPreferredSize(new Dimension(450, 300));
        setBackground(UIManager.getColor("Panel.background"));
    }

    public void setOverlay(boolean overlay) {
        this.isOverlay = overlay;
        setOpaque(!overlay);
        repaint();
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        repaint();
    }

    public synchronized void setImage(BufferedImage img) {
        this.image = img;
        if (calcThread != null && calcThread.isAlive()) {
            calcThread.interrupt();
        }

        if (image == null) {
            for (int i = 0; i < 256; i++) {
                histoRed[i] = 0;
                histoGreen[i] = 0;
                histoBlue[i] = 0;
            }
            maxAbsoluto = 0;
            repaint();
            return;
        }

        calcThread = new Thread(() -> {
            int[] localRed = new int[256];
            int[] localGreen = new int[256];
            int[] localBlue = new int[256];

            int ancho = img.getWidth();
            int alto = img.getHeight();

            for (int y = 0; y < alto; y++) {
                if (Thread.currentThread().isInterrupted()) return;
                for (int x = 0; x < ancho; x++) {
                    int pixel = img.getRGB(x, y);
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = (pixel & 0xFF);
                    localRed[r]++;
                    localGreen[g]++;
                    localBlue[b]++;
                }
            }

            int maxR = maximo(localRed);
            int maxG = maximo(localGreen);
            int maxB = maximo(localBlue);
            int localMax = Math.max(maxR, Math.max(maxG, maxB));

            if (!Thread.currentThread().isInterrupted()) {
                SwingUtilities.invokeLater(() -> {
                    this.histoRed = localRed;
                    this.histoGreen = localGreen;
                    this.histoBlue = localBlue;
                    this.maxAbsoluto = localMax;
                    repaint();
                });
            }
        });
        calcThread.start();
    }

    private int maximo(int[] h) {
        int max = 0;
        for (int v : h) if (v > max) max = v;
        return max;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null || maxAbsoluto == 0) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        // Dibujar fondo y rejilla
        if (isOverlay) {
            g2d.setColor(new Color(30, 30, 30, 180)); // Fondo semi-transparente oscuro
            g2d.fillRoundRect(0, 0, ancho, alto, 15, 15);
            g2d.setColor(new Color(255, 255, 255, 40)); // Líneas de rejilla sutiles
        } else {
            g2d.setColor(UIManager.getColor("Panel.background"));
            g2d.fillRect(0, 0, ancho, alto);
            g2d.setColor(UIManager.getColor("Component.borderColor"));
        }

        for (int i = 1; i < 4; i++) {
            int y = alto * i / 4;
            g2d.drawLine(0, y, ancho, y);
            g2d.drawLine(ancho * i / 4, 0, ancho * i / 4, alto);
        }

        float escalaX = (float) ancho / 255.0f;
        // Dejar un margen superior del 10%
        float escalaY = (alto * 0.9f) / maxAbsoluto;

        g2d.setStroke(new BasicStroke(1.5f));

        // Dibujar canales según el modo (con colores modernos)
        if (currentMode == Mode.RGB || currentMode == Mode.RED) {
            drawChannel(g2d, histoRed, new Color(255, 80, 80), escalaX, escalaY, alto);
        }
        if (currentMode == Mode.RGB || currentMode == Mode.GREEN) {
            drawChannel(g2d, histoGreen, new Color(80, 255, 80), escalaX, escalaY, alto);
        }
        if (currentMode == Mode.RGB || currentMode == Mode.BLUE) {
            drawChannel(g2d, histoBlue, new Color(80, 150, 255), escalaX, escalaY, alto);
        }
    }

    private void drawChannel(Graphics2D g2d, int[] histo, Color color, float escalaX, float escalaY, int altoHisto) {
        Polygon p = new Polygon();
        p.addPoint(0, altoHisto);
        for (int i = 0; i < histo.length; i++) {
            int x = (int) (escalaX * i);
            int y = altoHisto - (int) (escalaY * histo[i]);
            p.addPoint(x, y);
        }
        p.addPoint((int)(escalaX * 255), altoHisto);

        // Relleno semi-transparente para mejor estética
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);
        g2d.setColor(fillColor);
        g2d.fill(p);

        // Línea del borde más sólida
        g2d.setColor(color);
        for (int i = 1; i < histo.length; i++) {
            int x1 = (int) (escalaX * (i - 1));
            int y1 = altoHisto - (int) (escalaY * histo[i - 1]);
            int x2 = (int) (escalaX * i);
            int y2 = altoHisto - (int) (escalaY * histo[i]);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}
