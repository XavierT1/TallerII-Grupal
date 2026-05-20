package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class RGBAdjustmentFilter implements ImageFilter {
    private int rOffset;
    private int gOffset;
    private int bOffset;

    public RGBAdjustmentFilter(int rOffset, int gOffset, int bOffset) {
        this.rOffset = rOffset;
        this.gOffset = gOffset;
        this.bOffset = bOffset;
    }

    @Override
    public String getName() {
        return "Ajuste RGB";
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int a = (p >> 24) & 0xFF;
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = p & 0xFF;

            // Add offsets and clamp to 0-255
            r = Math.min(255, Math.max(0, r + rOffset));
            g = Math.min(255, Math.max(0, g + gOffset));
            b = Math.min(255, Math.max(0, b + bOffset));

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        result.setRGB(0, 0, width, height, pixels, 0, width);
        return result;
    }
}
