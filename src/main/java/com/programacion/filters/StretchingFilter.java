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

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int pixel = originalImage.getRGB(x, y);
                int a = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel >> 0) & 0xFF;

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
                    float[] hsv = Color.RGBtoHSB(r, g, b, null);
                    float h = hsv[0];
                    float s = hsv[1];
                    float v = hsv[2];

                    if (bits < 8) {
                        v = (float) ((int) (v * (niveles - 1))) / (niveles - 1);
                    }

                    int rgb = Color.HSBtoRGB(h, s, v);
                    pixelNuevo = (a << 24) | (rgb & 0x00FFFFFF);
                }

                result.setRGB(x, y, pixelNuevo);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        String espacio = (modo == 1) ? "RGB" : "HSV";
        return "Estiramiento " + espacio + " (" + bits + " bits)";
    }
}
