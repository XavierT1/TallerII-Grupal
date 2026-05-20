package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class ColorMatrixFilter implements ImageFilter {
    private String name;
    private float[] matrix; // 20 elements (4x5 matrix representation)

    public ColorMatrixFilter(String name, float[] matrix) {
        this.name = name;
        this.matrix = matrix;
    }

    public float[] getMatrix() {
        return matrix;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

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

            // Matrix multiplication
            float newR = matrix[0] * r + matrix[1] * g + matrix[2] * b + matrix[3] * a + matrix[4];
            float newG = matrix[5] * r + matrix[6] * g + matrix[7] * b + matrix[8] * a + matrix[9];
            float newB = matrix[10] * r + matrix[11] * g + matrix[12] * b + matrix[13] * a + matrix[14];
            float newA = matrix[15] * r + matrix[16] * g + matrix[17] * b + matrix[18] * a + matrix[19];

            // Clamp values between 0 and 255
            int finalR = Math.max(0, Math.min(255, (int) newR));
            int finalG = Math.max(0, Math.min(255, (int) newG));
            int finalB = Math.max(0, Math.min(255, (int) newB));
            int finalA = Math.max(0, Math.min(255, (int) newA));

            pixels[i] = (finalA << 24) | (finalR << 16) | (finalG << 8) | finalB;
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    // Preset matrices
    public static float[] getNeutral() {
        return new float[]{
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        };
    }

    public static float[] getSepia() {
        return new float[]{
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        };
    }

    public static float[] getVintage() {
        return new float[]{
            0.9f, 0.1f, 0.1f, 0f, 0f,
            0.3f, 0.8f, 0.1f, 0f, 0f,
            0.1f, 0.1f, 0.9f, 0f, 0f,
            0f,   0f,   0f,   1f, 0f
        };
    }

    public static float[] getPolaroid() {
        return new float[]{
            1.438f, -0.062f, -0.062f, 0f, -16f,
            -0.122f, 1.378f, -0.122f, 0f, -16f,
            -0.016f, -0.016f, 1.483f, 0f, -16f,
            0f,      0f,      0f,     1f, 0f
        };
    }

    public static float[] getGrayscale() {
        return new float[]{
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        };
    }

    public static float[] getInvert() {
        return new float[]{
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        };
    }

    public static float[] getWarm() {
        return new float[]{
            1.2f, 0f, 0f, 0f, 0f,
            0f, 1.0f, 0f, 0f, 0f,
            0f, 0f, 0.8f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        };
    }

    public static float[] getCool() {
        return new float[]{
            0.8f, 0f, 0f, 0f, 0f,
            0f, 1.0f, 0f, 0f, 0f,
            0f, 0f, 1.2f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        };
    }
}
