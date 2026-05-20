package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class HSVAdjustmentFilter implements ImageFilter {
    private float hueOffset; // -1.0 to 1.0 (corresponds to -360 to +360 degrees)
    private float satOffset; // -1.0 to 1.0
    private float valOffset; // -1.0 to 1.0

    public HSVAdjustmentFilter(float hueOffset, float satOffset, float valOffset) {
        this.hueOffset = hueOffset;
        this.satOffset = satOffset;
        this.valOffset = valOffset;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int ancho = originalImage.getWidth();
        int alto = originalImage.getHeight();
        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = new int[ancho * alto];
        originalImage.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int a = (pixel >> 24) & 0xFF;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            Color.RGBtoHSB(r, g, b, hsv);

            // Ajustar Hue (ciclar entre 0 y 1)
            float h = hsv[0] + hueOffset;
            while (h < 0.0f) h += 1.0f;
            while (h > 1.0f) h -= 1.0f;

            // Ajustar Saturation (limitar entre 0 y 1)
            float s = Math.max(0.0f, Math.min(1.0f, hsv[1] + satOffset));

            // Ajustar Value (limitar entre 0 y 1)
            float v = Math.max(0.0f, Math.min(1.0f, hsv[2] + valOffset));

            int rgb = Color.HSBtoRGB(h, s, v);
            pixels[i] = (a << 24) | (rgb & 0x00FFFFFF);
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        return "Ajuste HSV";
    }
}
