package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class ColorChannelFilter implements ImageFilter {
    private int tipo; // 1:Rojo, 2:Verde, 3:Azul, 4:Mitad, 5:Tercios

    public ColorChannelFilter(int tipo) {
        this.tipo = tipo;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        int pixel, r, g, b;
        int ancho, alto;

        ancho = originalImage.getWidth();
        alto = originalImage.getHeight();

        // Usamos TYPE_INT_RGB para evitar problemas de transparencia
        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        int[] pixels = new int[ancho * alto];
        originalImage.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        for (int i = 0; i < alto; i++) {
            int rowOffset = i * ancho;
            for (int j = 0; j < ancho; j++) {
                int index = rowOffset + j;
                pixel = pixels[index];

                // Extraemos canales
                r = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                b = pixel & 0xFF;

                // Lógica de canales
                if (tipo == 1) { // Solo Rojo
                    g = 0; b = 0;
                } else if (tipo == 2) { // Solo Verde
                    r = 0; b = 0;
                } else if (tipo == 3) { // Solo Azul
                    r = 0; g = 0;
                } else if (tipo == 4) { // Mitad (Rojo | Azul)
                    if (j < ancho / 2) {
                        g = 0; b = 0;
                    } else {
                        r = 0; g = 0;
                    }
                } else if (tipo == 5) { // Tercios (Rojo | Verde | Azul)
                    if (j < ancho / 3) {
                        g = 0; b = 0;
                    } else if (j < 2 * ancho / 3) {
                        r = 0; b = 0;
                    } else {
                        r = 0; g = 0;
                    }
                }

                // Forzamos Alpha a 255 (Opaco)
                pixels[index] = (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        }

        result.setRGB(0, 0, ancho, alto, pixels, 0, ancho);
        return result;
    }

    @Override
    public String getName() {
        switch (tipo) {
            case 1: return "Canal Rojo";
            case 2: return "Canal Verde";
            case 3: return "Canal Azul";
            case 4: return "Mitad R|A";
            case 5: return "Tercios R|V|A";
            default: return "Canales";
        }
    }
}
