package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class FrostedGlassFilter implements ImageFilter {

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null)
            return null;

        int ancho = originalImage.getWidth();
        int alto = originalImage.getHeight();

        // buffer para la imagen resultante
        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = new int[ancho * alto];
        originalImage.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            // Extracción de canales
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            // Cálculo de brillo
            int brillo = (r + g + b) / 3;

            // Cálculo del canal Alpha
            int a = (int) (50 + (brillo * (205.0 / 255.0)));

            // Reconstrucción del píxel
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        return "Vidrio Esmerilado";
    }
}