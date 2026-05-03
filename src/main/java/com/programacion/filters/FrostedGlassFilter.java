package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class FrostedGlassFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        // Variables idénticas a tu lógica
        int pixel, a, r, g, b, pixelNuevo;
        int gris, ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                // 1. Obtener pixel
                pixel = originalImage.getRGB(j, i);

                // 2. Extraer canales
                r = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                b = (pixel >> 0) & 0xFF;

                // 3. Calcular gris para determinar la transparencia (Brillo)
                gris = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);

                // 4. Lógica de Vidrio: A mayor brillo, mayor opacidad
                a = 50 + (gris * (255 - 50) / 255);

                // 5. Reconstruir con el nuevo canal Alpha
                pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;

                result.setRGB(j, i, pixelNuevo);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Vidrio Esmerilado";
    }
}
