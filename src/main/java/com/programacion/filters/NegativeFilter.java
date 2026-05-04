package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class NegativeFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        // Usamos los mismos nombres de variables para que sea fácil de memorizar
        int pixel, a, r, g, b, pixelNuevo;
        int ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        // Bucle con i (alto) y j (ancho)
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                
                // 1. Obtener pixel original
                pixel = originalImage.getRGB(j, i);

                // 2. Extraer canales (Desplazamiento de bits)
                a = (pixel >> 24) & 0xFF;
                r = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                b = (pixel >> 0) & 0xFF;

                // 3. Lógica del Negativo (Invertir colores)
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;

                // 4. Reconstruir el pixelNuevo
                pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;

                // 5. Guardar el resultado
                result.setRGB(j, i, pixelNuevo);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Negativo";
    }
}