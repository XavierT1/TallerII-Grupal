package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class NegativeFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        // Usamos los mismos nombres de variables para que sea fácil de memorizar
        int pixel, a, r, g, b;
        int ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = new int[ancho * alto];
        originalImage.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        // Bucle lineal
        for (int i = 0; i < pixels.length; i++) {
            pixel = pixels[i];

            // Extraer canales (Desplazamiento de bits)
            a = (pixel >> 24) & 0xFF;
            r = (pixel >> 16) & 0xFF;
            g = (pixel >> 8) & 0xFF;
            b = pixel & 0xFF;

            // Lógica del Negativo (Invertir colores)
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;

            // Reconstruir y guardar
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        return "Negativo";
    }
}