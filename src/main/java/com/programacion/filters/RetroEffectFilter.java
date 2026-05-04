package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class RetroEffectFilter implements ImageFilter {
    private int N; // Nivel de colores (2, 4, 8, 64, 128, 255)

    public RetroEffectFilter(int N) {
        this.N = N;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int pixel, a, r, g, b, pixelNuevo;
        int ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                pixel = originalImage.getRGB(j, i);

                a = (pixel >> 24) & 0xFF;
                r = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                b = (pixel >> 0) & 0xFF;

                // Fórmula académica original (Retro 1)
                if (N > 1) {
                    r = (r * (N - 1)) / 255;
                    r = (r * 255) / (N - 1);

                    g = (g * (N - 1)) / 255;
                    g = (g * 255) / (N - 1);

                    b = (b * (N - 1)) / 255;
                    b = (b * 255) / (N - 1);
                }

                pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(j, i, pixelNuevo);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        if (N == 255) return "Efecto Retro";
        return "Retro (N=" + N + ")";
    }
}
