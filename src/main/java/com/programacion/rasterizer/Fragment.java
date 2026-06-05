package com.programacion.rasterizer;

/**
 * Representa un fragmento (DTO) generado durante el proceso de rasterización.
 * Contiene coordenadas de pantalla, profundidad y color ARGB.
 */
public class Fragment {
    public int x;
    public int y;
    public double z;
    public int color;
    public boolean discarded = false;

    public Fragment(int x, int y, double z, int color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }
}
