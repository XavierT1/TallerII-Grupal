package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class NegativeFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Recorremos pixel por pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = originalImage.getRGB(x, y);
                Color col = new Color(rgba, true);

                // Para el negativo, restamos el valor del color actual a 255
                int r = 255 - col.getRed();
                int g = 255 - col.getGreen();
                int b = 255 - col.getBlue();

                Color negativeColor = new Color(r, g, b, col.getAlpha());
                result.setRGB(x, y, negativeColor.getRGB());
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Negativo";
    }
}