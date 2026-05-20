package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class BlackAndWhiteFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int pixel, a, r, g, b, pixelNuevo;
        int gris, ancho, alto;

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

            // 1. Calcular el valor de gris (Brillo)
            gris = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);

            // 2. Umbral (Threshold): Si es claro -> Blanco, si es oscuro -> Negro
            if (gris > 127) {
                r = g = b = 255;
            } else {
                r = g = b = 0;
            }

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        return "Blanco y Negro";
    }
}
