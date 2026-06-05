package com.programacion.rasterizer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * Motor nuclear de rasterización por software (SoftwareRasterizer).
 * Controla directamente arreglos unidimensionales de píxeles y realiza pruebas de visibilidad
 * mediante un buffer de profundidad (Z-Buffer).
 */
public class SoftwareRasterizer {
    private int width;
    private int height;
    private int[] pixels;
    private double[] zBuffer;
    private FragmentConsumer consumer;
    private boolean zBufferEnabled = true;

    /**
     * Crea un rasterizador por software vacío. Debe inicializarse vinculando una imagen
     * con bindTarget().
     */
    public SoftwareRasterizer() {
        this.width = 0;
        this.height = 0;
    }

    /**
     * Crea un rasterizador independiente con dimensiones fijas.
     *
     * @param width Ancho del buffer.
     * @param height Alto del buffer.
     */
    public SoftwareRasterizer(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        this.zBuffer = new double[width * height];
        clearZBuffer();
    }

    /**
     * Vincula directamente el buffer de píxeles de una BufferedImage de tipo TYPE_INT_ARGB
     * para manipulación ultra-eficiente en memoria.
     *
     * @param image Imagen objetivo de tipo TYPE_INT_ARGB.
     */
    public void bindTarget(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("La imagen objetivo no puede ser nula.");
        }
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalArgumentException("La imagen debe ser de tipo TYPE_INT_ARGB para acceso directo.");
        }
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        if (this.zBuffer == null || this.zBuffer.length != width * height) {
            this.zBuffer = new double[width * height];
        }
    }

    /**
     * Limpia el buffer de color (píxeles) con un color ARGB específico.
     */
    public void clearColorBuffer(int argbColor) {
        if (pixels != null) {
            Arrays.fill(pixels, argbColor);
        }
    }

    /**
     * Inicializa o limpia el buffer de profundidad (Z-Buffer) a su valor máximo.
     */
    public void clearZBuffer() {
        if (zBuffer != null) {
            Arrays.fill(zBuffer, Double.MAX_VALUE);
        }
    }

    /**
     * Configura el consumidor de fragmentos para acoplamiento con otros pipelines.
     */
    public void setFragmentConsumer(FragmentConsumer consumer) {
        this.consumer = consumer;
    }

    public FragmentConsumer getFragmentConsumer() {
        return this.consumer;
    }

    /**
     * Activa o desactiva la prueba de profundidad (Z-Buffer).
     */
    public void setZBufferEnabled(boolean enabled) {
        this.zBufferEnabled = enabled;
    }

    public boolean isZBufferEnabled() {
        return zBufferEnabled;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

    public double[] getZBuffer() {
        return zBuffer;
    }

    /**
     * Compara y escribe un fragmento en el framebuffer realizando la prueba de profundidad.
     */
    private void writeFragment(int x, int y, double z, int color) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;

        Fragment fragment = new Fragment(x, y, z, color);

        // Si hay un pipeline externo configurado (Capítulo 8), emitimos el fragmento.
        if (consumer != null) {
            consumer.consume(fragment);
        }

        // Si el fragmento es descartado por el pipeline, detenemos el proceso de escritura.
        if (fragment.discarded) {
            return;
        }

        // Validar si las coordenadas siguen vigentes tras el procesamiento externo
        int fx = fragment.x;
        int fy = fragment.y;
        if (fx < 0 || fx >= width || fy < 0 || fy >= height) return;

        int index = fy * width + fx;

        // Prueba de profundidad (Z-Buffer)
        if (!zBufferEnabled || fragment.z < zBuffer[index]) {
            if (zBufferEnabled) {
                zBuffer[index] = fragment.z;
            }
            pixels[index] = fragment.color;
        }
    }

    /**
     * Dibuja un punto (Píxel individual) en las coordenadas y profundidad indicadas.
     */
    public void drawPoint(int x, int y, double z, int color) {
        writeFragment(x, y, z, color);
    }

    /**
     * Dibuja una línea en 3D utilizando el algoritmo de Bresenham, interpolando la profundidad Z linealmente.
     */
    public void drawLineBresenham(int x1, int y1, double z1, int x2, int y2, double z2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;
        int steps = Math.max(dx, dy);
        int step = 0;

        while (true) {
            double t = steps == 0 ? 1.0 : (double) step / steps;
            double z = z1 + t * (z2 - z1);
            writeFragment(x, y, z, color);

            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
            step++;
        }
    }

    /**
     * Dibuja una línea en 3D utilizando el algoritmo DDA (Digital Differential Analyzer),
     * interpolando la profundidad Z linealmente.
     */
    public void drawLineDDA(int x1, int y1, double z1, int x2, int y2, double z2, int color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double steps = Math.max(Math.abs(dx), Math.abs(dy));

        if (steps == 0) {
            writeFragment(x1, y1, z1, color);
            return;
        }

        double xInc = dx / steps;
        double yInc = dy / steps;
        double zInc = dz / steps;

        double x = x1;
        double y = y1;
        double z = z1;

        for (int i = 0; i <= steps; i++) {
            writeFragment((int) Math.round(x), (int) Math.round(y), z, color);
            x += xInc;
            y += yInc;
            z += zInc;
        }
    }

    /**
     * Dibuja un triángulo sólido con un color uniforme.
     */
    public void drawTriangle(int x1, int y1, double z1,
                             int x2, int y2, double z2,
                             int x3, int y3, double z3,
                             int color) {
        drawTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, color, color, color);
    }

    /**
     * Rasteriza un triángulo en 3D utilizando coordenadas baricéntricas.
     * Soporta interpolación de profundidad Z e interpolación de color en los vértices (Gouraud shading).
     */
    public void drawTriangle(int x1, int y1, double z1,
                             int x2, int y2, double z2,
                             int x3, int y3, double z3,
                             int c1, int c2, int c3) {
        // Encontrar la caja delimitadora (Bounding Box) del triángulo
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));

        // Recortar contra las dimensiones de pantalla (Clipping bidimensional)
        minX = Math.max(0, minX);
        maxX = Math.min(width - 1, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min(height - 1, maxY);

        if (minX > maxX || minY > maxY) return;

        // Denominador para coordenadas baricéntricas
        double denom = (double) ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
        if (Math.abs(denom) < 1e-9) {
            // El triángulo es colineal o degenerado, dibujamos sus aristas como líneas
            drawLineBresenham(x1, y1, z1, x2, y2, z2, c1);
            drawLineBresenham(x2, y2, z2, x3, y3, z3, c2);
            drawLineBresenham(x3, y3, z3, x1, y1, z1, c3);
            return;
        }

        double invDenom = 1.0 / denom;

        // Iterar en la caja delimitadora
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // Calcular pesos baricéntricos w1, w2, w3
                double w1 = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) * invDenom;
                double w2 = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) * invDenom;
                double w3 = 1.0 - w1 - w2;

                // Si está dentro (permitiendo un margen infinitesimal para precisión numérica)
                if (w1 >= -1e-9 && w2 >= -1e-9 && w3 >= -1e-9) {
                    // Interpolación lineal de Z
                    double z = w1 * z1 + w2 * z2 + w3 * z3;

                    // Interpolación lineal de componentes de color (ARGB)
                    int a = (int) (w1 * ((c1 >> 24) & 0xFF) + w2 * ((c2 >> 24) & 0xFF) + w3 * ((c3 >> 24) & 0xFF));
                    int r = (int) (w1 * ((c1 >> 16) & 0xFF) + w2 * ((c2 >> 16) & 0xFF) + w3 * ((c3 >> 16) & 0xFF));
                    int g = (int) (w1 * ((c1 >> 8) & 0xFF) + w2 * ((c2 >> 8) & 0xFF) + w3 * ((c3 >> 8) & 0xFF));
                    int b = (int) (w1 * (c1 & 0xFF) + w2 * (c2 & 0xFF) + w3 * (c3 & 0xFF));

                    // Clampear componentes
                    a = Math.max(0, Math.min(255, a));
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));

                    int interpolatedColor = (a << 24) | (r << 16) | (g << 8) | b;
                    writeFragment(x, y, z, interpolatedColor);
                }
            }
        }
    }

    /**
     * Dibuja los valores del Z-buffer representados como escala de grises en el buffer de píxeles principal.
     * Útil para inspección visual y demostraciones didácticas.
     */
    public void drawZBufferToColorBuffer() {
        if (pixels == null || zBuffer == null) return;

        double minZ = Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        // Encontrar los límites de profundidad vigentes
        for (double z : zBuffer) {
            if (z != Double.MAX_VALUE) {
                if (z < minZ) minZ = z;
                if (z > maxZ) maxZ = z;
            }
        }

        // Si el buffer de profundidad está completamente libre de contenido
        if (minZ == Double.MAX_VALUE) {
            Arrays.fill(pixels, 0xFF000000); // Rellenar con negro completo
            return;
        }

        double range = maxZ - minZ;
        if (range == 0) range = 1.0;

        for (int i = 0; i < pixels.length; i++) {
            double z = zBuffer[i];
            if (z == Double.MAX_VALUE) {
                pixels[i] = 0xFF000000; // Píxeles sin profundidad pintados de negro
            } else {
                // Mapear Z de manera inversa para que los objetos cercanos (menores Z)
                // se muestren con blanco/brillante (cerca de 255)
                double norm = 1.0 - ((z - minZ) / range);
                int val = (int) (norm * 255.0);
                val = Math.max(0, Math.min(255, val));
                pixels[i] = 0xFF000000 | (val << 16) | (val << 8) | val;
            }
        }
    }
}
