package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;

public class RadialGradientFilter implements ImageFilter {
    private int tipo; // 1: Izq-Der, 2: Der-Izq, 3: Arr-Aba, 4: Aba-Arr, 5: Centro

    public RadialGradientFilter(int tipo) {
        this.tipo = tipo;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        // Usamos las dimensiones de la imagen original como base
        int ancho = (originalImage != null) ? originalImage.getWidth() : 800;
        int alto = (originalImage != null) ? originalImage.getHeight() : 600;

        BufferedImage result = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        int cx = ancho / 2;
        int cy = alto / 2;
        double maxDist = Math.sqrt(cx * cx + cy * cy);

        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                int intensidad = 0;

                switch (tipo) {
                    case 1: // Izq -> Der
                        intensidad = (j * 255) / ancho;
                        break;
                    case 2: // Der -> Izq
                        intensidad = ((ancho - j) * 255) / ancho;
                        break;
                    case 3: // Arr -> Aba
                        intensidad = (i * 255) / alto;
                        break;
                    case 4: // Aba -> Arr
                        intensidad = ((alto - i) * 255) / alto;
                        break;
                    case 5: // Centro
                        double dist = Math.sqrt(Math.pow(j - cx, 2) + Math.pow(i - cy, 2));
                        intensidad = (int)((dist * 255) / maxDist);
                        break;
                }

                // Asegurar rango 0-255
                if (intensidad > 255) intensidad = 255;
                if (intensidad < 0) intensidad = 0;

                // Generar color Azul-Negro (según tu método colorAzulNegro)
                int r = 0;
                int g = 0;
                int b = intensidad;
                int pixel = (r << 16) | (g << 8) | b;

                result.setRGB(j, i, pixel);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        switch (tipo) {
            case 1: return "Radial Izq-Der";
            case 2: return "Radial Der-Izq";
            case 3: return "Radial Arr-Aba";
            case 4: return "Radial Aba-Arr";
            case 5: return "Radial Centro";
            default: return "Gradiente Radial";
        }
    }
}
