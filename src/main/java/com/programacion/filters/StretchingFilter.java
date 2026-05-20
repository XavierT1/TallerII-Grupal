package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StretchingFilter implements ImageFilter {
    private int bits;
    private int modo; // 1: RGB, 2: HSV

    public StretchingFilter(int bits, int modo) {
        this.bits = bits;
        this.modo = modo;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int ancho = originalImage.getWidth();
        int alto = originalImage.getHeight();
        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        int niveles = (int) Math.pow(2, bits);
        int mascara = niveles - 1;

        int[] pixels = new int[ancho * alto];
        originalImage.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        // Pre-allocate hsv array to minimize garbage collection overhead
        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int a = (pixel >> 24) & 0xFF;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            int pixelNuevo;

            if (modo == 1) {
                // --- Estiramiento RGB ---
                if (bits < 8) {
                    r = (r >> (8 - bits)) & mascara;
                    g = (g >> (8 - bits)) & mascara;
                    b = (b >> (8 - bits)) & mascara;

                    r = (r * 255) / mascara;
                    g = (g * 255) / mascara;
                    b = (b * 255) / mascara;
                }
                pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;
            } else {
                // --- Estiramiento HSV (Brillo) ---
                Color.RGBtoHSB(r, g, b, hsv);
                float h = hsv[0];
                float s = hsv[1];
                float v = hsv[2];

                if (bits < 8) {
                    v = (float) ((int) (v * (niveles - 1))) / (niveles - 1);
                }

                int rgb = Color.HSBtoRGB(h, s, v);
                pixelNuevo = (a << 24) | (rgb & 0x00FFFFFF);
            }

            pixels[i] = pixelNuevo;
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        String espacio = (modo == 1) ? "RGB" : "HSV";
        return "Estiramiento " + espacio + " (" + bits + " bits)";
    }
}
