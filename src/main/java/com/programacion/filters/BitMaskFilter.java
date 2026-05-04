package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class BitMaskFilter implements ImageFilter {

    private int bits; // Cantidad de bits a conservar (2, 4, 6, etc.)
    private float factorT; // Tu factor de transparencia

    public BitMaskFilter(int bits, float factorT) {
        this.bits = bits;
        this.factorT = factorT;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null)
            return null;

        int ancho = originalImage.getWidth();
        int alto = originalImage.getHeight();
        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        // Cálculos de la máscara según los bits elegidos
        int desplazamiento = 8 - bits;
        int mascara = (1 << bits) - 1; // Genera 0b11 para 2 bits, 0xF para 4 bits, etc.
        int maximoValor = mascara;

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int pixel = originalImage.getRGB(x, y);

                int a = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel >> 0) & 0xFF;

                // 1. Aplicamos tu lógica de transparencia
                a = (int) (Math.min(255, a * factorT));

                // 2. Lógica de Máscaras y Recorte (Bitwise)
                r = (r >> desplazamiento) & mascara;
                g = (g >> desplazamiento) & mascara;
                b = (b >> desplazamiento) & mascara;

                // 3. Lógica de Estiramiento (Para recuperar el brillo original)
                r = (r * 255) / maximoValor;
                g = (g * 255) / maximoValor;
                b = (b * 255) / maximoValor;

                int pixelNuevo = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, pixelNuevo);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Máscara de Bits (" + bits + " bits)";
    }
}