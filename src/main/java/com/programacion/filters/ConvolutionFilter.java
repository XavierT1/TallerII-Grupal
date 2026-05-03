package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ConvolutionFilter implements ImageFilter {
    private String nombre;
    private float[] matriz;

    public ConvolutionFilter(String nombre, float[] matriz) {
        this.nombre = nombre;
        this.matriz = matriz;
    }

    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        // Calculamos la dimensión de la matriz (asumiendo que es cuadrada)
        int size = (int) Math.sqrt(matriz.length);
        
        // Creamos el Kernel de Java
        Kernel kernel = new Kernel(size, size, matriz);
        
        // Configuramos la operación de convolución
        // EDGE_NO_OP significa que no procesa los bordes (los deja como están)
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        // Aplicamos el filtro
        // Nota: ConvolveOp a veces tiene problemas con TYPE_INT_ARGB si hay transparencias,
        // por lo que nos aseguramos de que el destino sea compatible.
        BufferedImage result = op.filter(originalImage, null);
        
        return result;
    }

    @Override
    public String getName() {
        return nombre;
    }

    // --- Definición de Matrices (Kernels) ---
    
    public static ConvolutionFilter Enfoque() {
        return new ConvolutionFilter("Enfoque (Sharpen)", new float[]{
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        });
    }

    public static ConvolutionFilter Desenfoque() {
        float v = 1f / 9f;
        return new ConvolutionFilter("Desenfoque (Blur)", new float[]{
            v, v, v,
            v, v, v,
            v, v, v
        });
    }

    public static ConvolutionFilter Bordes() {
        return new ConvolutionFilter("Detector de Bordes", new float[]{
            -0.5f, -0.5f, -0.5f,
            -0.5f, 4f, -0.5f,
            -0.5f, -0.5f, -0.5f
        });
    }

    public static ConvolutionFilter DesenfoquePesado() {
        float v = 1f / 81f;
        float[] matriz = new float[81];
        for (int i = 0; i < 81; i++) matriz[i] = v;
        return new ConvolutionFilter("Desenfoque 9x9", matriz);
    }

    public static ConvolutionFilter Aclarar() {
        return new ConvolutionFilter("Aclarar", new float[]{
            0.1f, 0.1f, 0.1f,
            0.1f, 1.0f, 0.1f,
            0.1f, 0.1f, 0.1f
        });
    }

    public static ConvolutionFilter Oscurecer() {
        return new ConvolutionFilter("Oscurecer", new float[]{
            0.01f, 0.01f, 0.01f,
            0.01f, 0.5f, 0.01f,
            0.01f, 0.01f, 0.01f
        });
    }
}
