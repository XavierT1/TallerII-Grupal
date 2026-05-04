package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class CircularFadeFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int pixel, a, r, g, b, pixelNuevo;
        int ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        // Coordenadas del centro
        int cx = ancho / 2;
        int cy = alto / 2;

        // Distancia máxima (del centro a una esquina)
        double maxDist = Math.sqrt(cx * cx + cy * cy);

        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                pixel = originalImage.getRGB(j, i);

                r = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                b = (pixel >> 0) & 0xFF;

                // Calcular distancia al centro (Teorema de Pitágoras)
                double dist = Math.sqrt(Math.pow(j - cx, 2) + Math.pow(i - cy, 2));

                // Lógica: a mayor distancia, menor Alpha (más transparente)
                a = (int)(255 - (dist * 255 / maxDist));
                
                // Aseguramos que 'a' no se salga del rango 0-255
                if (a < 0) a = 0;

                // Reconstruir pixel
                pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;

                result.setRGB(j, i, pixelNuevo);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Desvanecimiento Circular";
    }
}
