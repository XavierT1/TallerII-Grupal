package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class GrayscaleFilter implements ImageFilter {
    private int N; // Nivel de gris (2, 4, 8, 64, 128, 255)

    // Constructor por defecto
    public GrayscaleFilter() {
        this.N = 255;
    }

    // Constructor con nivel específico
    public GrayscaleFilter(int N) {
        this.N = N;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        // Variables idénticas a tu lógica de negocios
        int pixel, a, r, g, b, pixelNuevo;
        int gris, ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        // Bucle con i (alto) y j (ancho)
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {

                // Obtener pixel original
                pixel = originalImage.getRGB(j, i);

                // Extraer canales con desplazamiento de bits
                a = (pixel >> 24) & 0xFF;
                r = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                b = (pixel >> 0) & 0xFF;

                // Calcular el gris usando la fórmula de luminosidad
                gris = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);

                // Aplicar la reducción de niveles N
                gris = (gris * (N - 1)) / 255;
                gris = (gris * 255) / (N - 1);

                // Asignar el valor gris a los tres canales de color
                r = g = b = gris;

                // Reconstruir el pixelNuevo
                pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;

                // Guardar el resultado
                result.setRGB(j, i, pixelNuevo);
            }
        }

        return result;
    }

    @Override
    public String getName() {
        if (N == 255) return "Escala de Grises";
        return "Grises (N=" + N + ")";
    }
}