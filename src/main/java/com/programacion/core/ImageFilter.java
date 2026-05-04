package com.programacion.core;

import java.awt.image.BufferedImage;

public interface ImageFilter {
    /**
     * Aplica un filtro a la imagen original y devuelve una nueva imagen.
     */
    BufferedImage apply(BufferedImage originalImage);

    /**
     * Devuelve el nombre del filtro (útil para la UI).
     */
    String getName();
}