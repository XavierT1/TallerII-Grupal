# Plantillas de Issues para GitHub - LuminaFX

Este documento contiene las especificaciones, requisitos y guías de acoplamiento para los nuevos capítulos del proyecto (6 al 10 y README), junto con las instrucciones para que cada colaborador vincule y cierre de forma automática sus Issues a través de sus Pull Requests.

---

## 📌 Issue 1 (Capítulo 6)
* **Asignado a**: `@Dennis290699`
* **Título**: `feat(cap-6): Implementar Fundamentos de Rasterización y Z-Buffer`
* **Etiquetas**: `enhancement`, `capitulo-6`

### Cuerpo de la Issue:
```markdown
## 📖 Descripción General
Este issue consiste en implementar los fundamentos del renderizado en 2D y 3D dentro del proyecto mediante la creación de un motor de rasterización por software (`SoftwareRasterizer`). En este capítulo se cubrirán el trazado de primitivas básicas, el control directo de arreglos de píxeles en memoria y la resolución de visibilidad de superficies utilizando un buffer de profundidad (Z-Buffer).

## 📋 Requisitos a cumplir (Capítulo 6)
- [ ] **Rasterización**:
  - Implementar funciones para dibujar primitivas básicas: Puntos, Líneas (usando el algoritmo de Bresenham o DDA) y Triángulos (usando coordenadas baricéntricas o algoritmo de scanline).
- [ ] **Z-buffers (Depth)**:
  - Crear e inicializar un buffer de profundidad (`Z`-buffer) con el tamaño de la pantalla/imagen.
  - Asegurar que al pintar píxeles se compare su valor `Z` actual contra el almacenado, permitiendo pintar solo si el nuevo fragmento está más cerca de la cámara.
- [ ] **Bitmaps, píxeles**:
  - Manipulación directa de arreglos unidimensionales de píxeles en memoria (`int[]` o `BufferedImage` tipo `TYPE_INT_ARGB`).
  - Eficiencia en el acceso y lectura directa al buffer.

## 🔗 Acoplamiento con otros Capítulos
- **Entrada/Salida**: Tu rasterizador debe generar y emitir fragmentos (`Fragment`). Un fragmento es un objeto de transferencia de datos (DTO) que contiene `(x, y, z, color)`.
- **Integración**: Los fragmentos generados aquí serán la entrada obligatoria para el **Pipeline de Fragmentos** (Capítulo 8) asignado a @XavierT1.

## 🚀 Instrucciones para subir tu PR y vincularla a esta Issue
Para que esta Issue se cierre de manera automática cuando el administrador acepte y fusione tu Pull Request, debes seguir estos pasos:
1. Crea tu rama de trabajo con un nombre descriptivo, por ejemplo: `feature/capitulo-6`.
2. Una vez que subas tus cambios a GitHub, abre la Pull Request hacia la rama principal (`main` o `master`).
3. En la **descripción (cuerpo) de la PR**, añade la siguiente línea exacta:
   ```text
   Closes #<ID_DE_ESTE_ISSUE>
   ```
   *(Nota: Reemplaza `<ID_DE_ESTE_ISSUE>` por el número que GitHub le asigne a esta Issue en el panel superior, por ejemplo: `Closes #1`)*.
```

---

## 📌 Issue 2 (Capítulo 7)
* **Asignado a**: `@Dennis290699`
* **Título**: `feat(cap-7): Implementar Mapeado de Texturas e Interpolación Correjida por Perspectiva (W-Buffering)`
* **Etiquetas**: `enhancement`, `capitulo-7`

### Cuerpo de la Issue:
```markdown
## 📖 Descripción General
Este issue expande las capacidades del motor de rasterización implementando el mapeado de texturas sobre los triángulos y resolviendo la interpolación de color y profundidad con corrección de perspectiva. Se utilizará W-buffering como alternativa al Z-buffer estándar para prevenir problemas de precisión numérica en proyecciones de perspectiva.

## 📋 Requisitos a cumplir (Capítulo 7)
- [ ] **Texturas, color**:
  - Carga y lectura de texturas (`BufferedImage`) para mapearlas sobre los triángulos.
  - Asignación de coordenadas de textura `(u, v)` en los vértices e interpolación bilineal de texturas.
- [ ] **Interpolación en profundidad (Depth interpolation)**:
  - Interpolación lineal del valor `Z` de profundidad a lo largo de la superficie de los triángulos.
- [ ] **W-buffering**:
  - Implementar el buffer de perspectiva (`W`-buffer) interpolando `1/W` para lograr un mapeo de texturas libre de distorsiones de perspectiva en primitivas inclinadas.

## 🔗 Acoplamiento con otros Capítulos
- **Entrada/Salida**: Tu rasterizador con texturas ahora emitirá fragmentos (`Fragment`) que incluirán coordenadas de textura interpoladas e información de perspectiva.
- **Integración**: Estos fragmentos con textura se enviarán directamente al pipeline de pruebas de fragmentos de @XavierT1.

## 🚀 Instrucciones para subir tu PR y vincularla a esta Issue
Para que esta Issue se cierre de manera automática cuando el administrador acepte y fusione tu Pull Request, debes seguir estos pasos:
1. Crea tu rama de trabajo con un nombre descriptivo, por ejemplo: `feature/capitulo-7`.
2. Una vez que subas tus cambios a GitHub, abre la Pull Request hacia la rama principal.
3. En la **descripción (cuerpo) de la PR**, añade la siguiente línea exacta:
   ```text
   Closes #<ID_DE_ESTE_ISSUE>
   ```
   *(Nota: Reemplaza `<ID_DE_ESTE_ISSUE>` por el número que GitHub le asigne a esta Issue, por ejemplo: `Closes #2`)*.
```

---

## 📌 Issue 3 (Capítulo 8)
* **Asignado a**: `@XavierT1`
* **Título**: `feat(cap-8): Implementar Pipeline de Fragmentos y Operaciones de Prueba (Parte 1)`
* **Etiquetas**: `enhancement`, `capitulo-8`

### Cuerpo de la Issue:
```markdown
## 📖 Descripción General
Este issue consiste en crear el pipeline de operaciones que se aplican a cada píxel individual (fragmento) antes de escribirse en el Framebuffer final. Se implementará la arquitectura básica del pipeline, el test de recorte (Scissor), el test de opacidad (Alpha) y el test de profundidad (Depth), junto con una aproximación de Multisample (MSAA) para evitar el aliasing.

## 📋 Requisitos a cumplir (Capítulo 8)
- [ ] **Fragmentos: concepto y pipeline**:
  - Diseñar la clase `FragmentPipeline` que reciba fragmentos procedentes del rasterizador de @Dennis290699.
  - Implementar la lógica secuencial de pruebas que decide si un fragmento se dibuja o se descarta.
- [ ] **Operaciones con fragmentos**:
  - Implementar **Scissor Test**: comprobar si las coordenadas `(x, y)` del fragmento caen dentro de un rectángulo de recorte definido.
- [ ] **Multisample**:
  - Implementar un sistema básico de suavizado por muestreo múltiple (MSAA) calculando la cobertura del fragmento sobre subpíxeles.
- [ ] **Alpha test**:
  - Comparar el valor Alpha del fragmento contra una referencia elegida (ej. mayor a un umbral) y descartar el fragmento si no lo supera.

## 🔗 Acoplamiento con otros Capítulos
- **Entrada**: Tu pipeline recibe objetos `Fragment` producidos por el rasterizador de @Dennis290699 (Capítulos 6 y 7).
- **Consumo de Buffer**: El Depth Test debe consultar y actualizar el Z-Buffer creado por Dennis.
- **Continuación**: Los fragmentos sobrevivientes irán al Stencil Test y Blending (Capítulo 9).

## 🚀 Instrucciones para subir tu PR y vincularla a esta Issue
Para que esta Issue se cierre de manera automática cuando el administrador acepte y fusione tu Pull Request, debes seguir estos pasos:
1. Crea tu rama de trabajo con un nombre descriptivo, por ejemplo: `feature/capitulo-8`.
2. Una vez que subas tus cambios a GitHub, abre la Pull Request hacia la rama principal.
3. En la **descripción (cuerpo) de la PR**, añade la siguiente línea exacta:
   ```text
   Closes #<ID_DE_ESTE_ISSUE>
   ```
   *(Nota: Reemplaza `<ID_DE_ESTE_ISSUE>` por el número que GitHub le asigne a esta Issue, por ejemplo: `Closes #3`)*.
```

---

## 📌 Issue 4 (Capítulo 9)
* **Asignado a**: `@XavierT1`
* **Título**: `feat(cap-9): Implementar Stencil Test, Blending y Operaciones Lógicas (Logic Ops)`
* **Etiquetas**: `enhancement`, `capitulo-9`

### Cuerpo de la Issue:
```markdown
## 📖 Descripción General
Este issue cubre la segunda parte del pipeline de fragmentos. Se implementará el test de plantilla (Stencil), que permite enmascarar o delimitar áreas de dibujo avanzadas, la mezcla de color (Blending) para transparencia y las operaciones lógicas booleanas directas sobre el framebuffer (Logic Ops).

## 📋 Requisitos a cumplir (Capítulo 9)
- [ ] **Stencil test**:
  - Crear un buffer de stencil (plantilla).
  - Implementar la prueba del Stencil comparando el valor de la máscara stencil contra la referencia y definir las acciones del stencil (mantener, incrementar, decrementar, etc.).
- [ ] **Blending**:
  - Implementar fórmulas matemáticas para mezclar el color entrante (source) con el color que ya está en el framebuffer (destination).
  - Soportar modos: Transparencia (Source Alpha / One Minus Source Alpha), Aditivo y Multiplicativo.
- [ ] **Logic Op**:
  - Implementar operaciones de bits lógicas (como XOR, AND, OR) sobre el framebuffer para generar efectos de dibujo alternativos (inversión de colores, etc.).

## 🔗 Acoplamiento con otros Capítulos
- **Salida**: El resultado final de tu `FragmentPipeline` es el Framebuffer final (la imagen resultante).
- **Integración**: Este buffer final procesado servirá como entrada para el **Buffer de Acumulación** (Capítulo 10) de @KevinPozo para efectos temporales.

## 🚀 Instrucciones para subir tu PR y vincularla a esta Issue
Para que esta Issue se cierre de manera automática cuando el administrador acepte y fusione tu Pull Request, debes seguir estos pasos:
1. Crea tu rama de trabajo con un nombre descriptivo, por ejemplo: `feature/capitulo-9`.
2. Una vez que subas tus cambios a GitHub, abre la Pull Request hacia la rama principal.
3. En la **descripción (cuerpo) de la PR**, añade la siguiente línea exacta:
   ```text
   Closes #<ID_DE_ESTE_ISSUE>
   ```
   *(Nota: Reemplaza `<ID_DE_ESTE_ISSUE>` por el número que GitHub le asigne a esta Issue, por ejemplo: `Closes #4`)*.
```

---

## 📌 Issue 5 (Capítulo 10 y README)
* **Asignado a**: `@KevinPozo`
* **Título**: `feat(cap-10-readme): Implementar Accumulation Buffer y Documentación General`
* **Etiquetas**: `documentation`, `enhancement`, `capitulo-10`

### Cuerpo de la Issue:
```markdown
## 📖 Descripción General
Este issue consiste en implementar el Buffer de Acumulación para combinar múltiples fotogramas y aplicar efectos de post-procesamiento temporal avanzados como Motion Blur (desenfoque de movimiento), Depth of Field (profundidad de campo) y Full-Scene Antialiasing (FSAA). Adicionalmente, se redactará la documentación técnica unificada del proyecto completo en un archivo `README.md` en la raíz.

## 📋 Requisitos a cumplir (Capítulo 10)
- [ ] **Buffer de acumulación: concepto y uso**:
  - Implementar la clase `AccumulationBuffer` que permita almacenar temporalmente acumulaciones de color con precisión de punto flotante para no perder precisión en las mezclas de múltiples cuadros.
- [ ] **Aplicaciones prácticas**:
  - **Motion Blur**: Generar proyecciones de una animación o cambio de posición acumulando fotogramas secuenciales.
  - **Depth of Field**: Renderizar la escena con ligeros desplazamientos del plano focal y acumularlas para simular lentes reales.
  - **Antialiasing (FSAA)**: Muestreo de subpíxeles mediante acumulación de frames con vibraciones (jitter) en el viewport.

## 📋 Requisitos del README del Proyecto Completo
Redactar el `README.md` en la raíz del repositorio siguiendo los estándares académicos y profesionales:
- [ ] **Título y descripción general**: Nombre y propósito del proyecto (LuminaFX).
- [ ] **Integrantes del equipo y roles**: Listar los integrantes y sus asignaciones de capítulos.
- [ ] **Índice de capítulos con enlaces**: Navegación rápida con anclas markdown.
- [ ] **Instrucciones de instalación/ejecución**: Comandos gradle o pasos para ejecutar.
- [ ] **Tecnologías utilizadas**: Java Swing, Gradle, FlatLaf, etc.
- [ ] **Cómo contribuir**: Guía detallada explicando el uso de ramas, Pull Requests y la vinculación de issues usando palabras clave de GitHub (como `Closes #issue`).
- [ ] **Licencia**.

## 🔗 Acoplamiento con otros Capítulos
- **Entrada**: Tu `AccumulationBuffer` consumirá las sucesivas imágenes resultantes generadas tras la rasterización de Dennis y el filtrado por fragmentos de Freddy (@XavierT1).

## 🚀 Instrucciones para subir tu PR y vincularla a esta Issue
Para que esta Issue se cierre de manera automática cuando el administrador acepte y fusione tu Pull Request, debes seguir estos pasos:
1. Crea tu rama de trabajo con un nombre descriptivo, por ejemplo: `feature/cap-10-readme`.
2. Una vez que subas tus cambios a GitHub, abre la Pull Request hacia la rama principal.
3. En la **descripción (cuerpo) de la PR**, añade la siguiente línea exacta:
   ```text
   Closes #<ID_DE_ESTE_ISSUE>
   ```
   *(Nota: Reemplaza `<ID_DE_ESTE_ISSUE>` por el número que GitHub le asigne a esta Issue, por ejemplo: `Closes #5`)*.
```
