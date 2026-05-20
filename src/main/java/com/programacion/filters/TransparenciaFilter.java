package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class TransparenciaFilter implements ImageFilter {

    private float factorT;

    public TransparenciaFilter(float factorT) {
        this.factorT = factorT;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null)
            return null;

        int ancho = originalImage.getWidth();
        int alto = originalImage.getHeight();
        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = new int[ancho * alto];
        originalImage.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            int a = (pixel >> 24) & 0xFF;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            if (a == 0)
                a = 255;

            a = (int) (a * factorT);

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        return "Transparencia";
    }
}