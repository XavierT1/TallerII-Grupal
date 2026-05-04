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

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int pixel = originalImage.getRGB(x, y);

                int a = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel >> 0) & 0xFF;

                if (a == 0)
                    a = 255;

                a = (int) (a * factorT);

                int pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, pixelNuevo);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Transparencia";
    }
}