package com.programacion.filters;

import com.programacion.core.ImageFilter;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class GrayscaleFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage originalImage) {
        if (originalImage == null) return null;

        // Creamos una nueva imagen para no alterar la original directamente
        BufferedImage result = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        // Java tiene una clase nativa optimizada para convertir espacios de color
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(originalImage, result);

        return result;
    }

    @Override
    public String getName() {
        return "Escala de Grises";
    }
}