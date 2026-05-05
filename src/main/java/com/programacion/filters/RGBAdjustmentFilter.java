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

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y);
                Color c = new Color(p, true);
                int a = c.getAlpha();
                
                // Add offsets and clamp to 0-255
                int r = Math.min(255, Math.max(0, c.getRed() + rOffset));
                int g = Math.min(255, Math.max(0, c.getGreen() + gOffset));
                int b = Math.min(255, Math.max(0, c.getBlue() + bOffset));
                
                result.setRGB(x, y, new Color(r, g, b, a).getRGB());
            }
        }
        return result;
    }
}
