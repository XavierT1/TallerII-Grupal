package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class RetroTwoFilter implements ImageFilter {
    private int N;
    private int modo; // 1: RG, 2: RB, 3: GB

    public RetroTwoFilter(int N, int modo) {
        this.N = N;
        this.modo = modo;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int pixel, a, r, g, b;
        int ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = new int[ancho * alto];
        originalImage.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        for (int i = 0; i < pixels.length; i++) {
            pixel = pixels[i];

            a = (pixel >> 24) & 0xFF;
            r = (pixel >> 16) & 0xFF;
            g = (pixel >> 8) & 0xFF;
            b = pixel & 0xFF;

            // Fórmula académica original (Retro 2)
            if (N > 1) {
                if (modo == 1) { // RG afectado
                    r = (r * (N - 1)) / 255;
                    r = (r * 255) / (N - 1);
                    g = (g * (N - 1)) / 255;
                    g = (g * 255) / (N - 1);
                } else if (modo == 2) { // RB afectado
                    r = (r * (N - 1)) / 255;
                    r = (r * 255) / (N - 1);
                    b = (b * (N - 1)) / 255;
                    b = (b * 255) / (N - 1);
                } else if (modo == 3) { // GB afectado
                    g = (g * (N - 1)) / 255;
                    g = (g * 255) / (N - 1);
                    b = (b * (N - 1)) / 255;
                    b = (b * 255) / (N - 1);
                }
            }

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        String canales = (modo == 1) ? "RG" : (modo == 2) ? "RB" : "GB";
        return "Retro 2 (" + canales + ")";
    }
}
