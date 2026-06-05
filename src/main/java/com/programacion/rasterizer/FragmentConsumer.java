package com.programacion.rasterizer;

/**
 * Interfaz funcional que actúa como receptor para procesar fragmentos generados por el rasterizador.
 * Es el punto de acoplamiento obligatorio para el pipeline del Capítulo 8.
 */
@FunctionalInterface
public interface FragmentConsumer {
    /**
     * Consume e intercepta un fragmento del rasterizador.
     * Puede modificar los campos del fragmento (como color o profundidad) o marcarlo como descartado.
     *
     * @param fragment El fragmento generado por el rasterizador.
     */
    void consume(Fragment fragment);
}
