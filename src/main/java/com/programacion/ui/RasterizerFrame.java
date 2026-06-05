package com.programacion.ui;

import com.programacion.rasterizer.Fragment;
import com.programacion.rasterizer.FragmentConsumer;
import com.programacion.rasterizer.SoftwareRasterizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Interfaz interactiva para el motor de rasterización por software en 3D.
 * Permite visualizar y rotar figuras geométricas en tiempo real y probar la lógica del Z-Buffer.
 */
public class RasterizerFrame extends JPanel {
    private final MainFrame parentFrame;
    private final SoftwareRasterizer rasterizer;
    private BufferedImage renderImage;
    private final RasterizerCanvas canvasPanel;
    private Timer animationTimer;

    // Estados de rotación y cámara
    private double rotX = 0.5, rotY = 0.5, rotZ = 0.0;
    private double speedX = 0.01, speedY = 0.015, speedZ = 0.0;
    private double cameraDist = 4.0;
    private double fovScale = 0.8;
    private boolean isPlaying = true;

    // Opciones de renderizado
    private enum ShapeType { CUBE, PYRAMID, SPHERE, COMBINED }
    private enum RenderMode { SOLID_SHADED, WIREFRAME, POINTS, Z_BUFFER_MAP }
    private enum ShadingMode { GOURAUD, FLAT, NO_LIGHTING }
    private enum LineAlgo { BRESENHAM, DDA }

    private ShapeType activeShape = ShapeType.CUBE;
    private RenderMode activeRenderMode = RenderMode.SOLID_SHADED;
    private ShadingMode activeShadingMode = ShadingMode.GOURAUD;
    private LineAlgo activeLineAlgo = LineAlgo.BRESENHAM;
    private int bgColor = 0xFF121214; // Gris oscuro
    private int meshLineColor = 0xFF6366F1; // Índigo

    // Estructuras 3D
    private Vertex3D[] cubeVertices;
    private Face3D[] cubeFaces;
    private Vertex3D[] pyramidVertices;
    private Face3D[] pyramidFaces;
    private Vertex3D[] sphereVertices;
    private Face3D[] sphereFaces;

    // Componentes Swing
    private JComboBox<String> comboFiguras, comboModo, comboSombreado, comboLineaAlgo, comboBgColor, comboLineColor;
    private JSlider sliderSpeedX, sliderSpeedY, sliderSpeedZ, sliderZoom, sliderFov;
    private JCheckBox checkZBuffer;
    private JButton btnPlayPause;

    public RasterizerFrame(MainFrame parentFrame, boolean isDarkMode) {
        this.parentFrame = parentFrame;
        this.rasterizer = new SoftwareRasterizer();

        setLayout(new BorderLayout());

        // Inicializar figuras geométricas
        initShapes();

        // Inicializar componentes visuales
        initToolbar();

        canvasPanel = new RasterizerCanvas();
        add(canvasPanel, BorderLayout.CENTER);

        initSidebar();

        // Control de redimensionado automático del buffer de píxeles
        canvasPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recreateBuffer();
            }
        });

        // Configurar timer de animación (aprox. 60 FPS)
        animationTimer = new Timer(16, e -> {
            if (isPlaying) {
                rotX += speedX;
                rotY += speedY;
                rotZ += speedZ;
            }
            renderFrame();
        });
        animationTimer.start();
    }

    private void initShapes() {
        // --- CUBO ---
        cubeVertices = new Vertex3D[]{
                new Vertex3D(-0.8, -0.8, -0.8, 0xFFFF3B30), // Rojo
                new Vertex3D( 0.8, -0.8, -0.8, 0xFF34C759), // Verde
                new Vertex3D( 0.8,  0.8, -0.8, 0xFF007AFF), // Azul
                new Vertex3D(-0.8,  0.8, -0.8, 0xFFFFCC00), // Amarillo
                new Vertex3D(-0.8, -0.8,  0.8, 0xFFAF52DE), // Violeta
                new Vertex3D( 0.8, -0.8,  0.8, 0xFF5AC8FA), // Cyan
                new Vertex3D( 0.8,  0.8,  0.8, 0xFFFFFFFF), // Blanco
                new Vertex3D(-0.8,  0.8,  0.8, 0xFF8E8E93)  // Gris
        };

        // Caras en sentido horario/antihorario divididas en triángulos
        cubeFaces = new Face3D[]{
                // Frontal (z = -0.8)
                new Face3D(new int[]{0, 2, 1}, 0xFFFF3B30),
                new Face3D(new int[]{0, 3, 2}, 0xFFFF3B30),
                // Trasera (z = 0.8)
                new Face3D(new int[]{5, 6, 4}, 0xFF34C759),
                new Face3D(new int[]{6, 7, 4}, 0xFF34C759),
                // Superior (y = 0.8)
                new Face3D(new int[]{3, 6, 2}, 0xFF007AFF),
                new Face3D(new int[]{3, 7, 6}, 0xFF007AFF),
                // Inferior (y = -0.8)
                new Face3D(new int[]{4, 1, 0}, 0xFFFFCC00),
                new Face3D(new int[]{4, 5, 1}, 0xFFFFCC00),
                // Derecha (x = 0.8)
                new Face3D(new int[]{1, 6, 5}, 0xFFAF52DE),
                new Face3D(new int[]{1, 2, 6}, 0xFFAF52DE),
                // Izquierda (x = -0.8)
                new Face3D(new int[]{4, 3, 0}, 0xFF5AC8FA),
                new Face3D(new int[]{4, 7, 3}, 0xFF5AC8FA)
        };

        // --- PIRÁMIDE ---
        pyramidVertices = new Vertex3D[]{
                new Vertex3D( 0.0,  0.9,  0.0, 0xFFFFFFFF), // 0: Cúspide (Blanco)
                new Vertex3D(-0.9, -0.7, -0.9, 0xFFFF3B30), // 1: Base Front-Izquierda (Rojo)
                new Vertex3D( 0.9, -0.7, -0.9, 0xFF34C759), // 2: Base Front-Derecha (Verde)
                new Vertex3D( 0.9, -0.7,  0.9, 0xFF007AFF), // 3: Base Tras-Derecha (Azul)
                new Vertex3D(-0.9, -0.7,  0.9, 0xFFFFCC00)  // 4: Base Tras-Izquierda (Amarillo)
        };

        pyramidFaces = new Face3D[]{
                // Caras laterales
                new Face3D(new int[]{0, 2, 1}, 0xFFFF2D55),
                new Face3D(new int[]{0, 3, 2}, 0xFF5AC8FA),
                new Face3D(new int[]{0, 4, 3}, 0xFFFFCC00),
                new Face3D(new int[]{0, 1, 4}, 0xFFAF52DE),
                // Base (2 triángulos)
                new Face3D(new int[]{1, 2, 3}, 0xFF8E8E93),
                new Face3D(new int[]{1, 3, 4}, 0xFF8E8E93)
        };

        // --- ESFERA (UV Sphere) ---
        generateSphere(16, 16);
    }

    private void generateSphere(int rings, int sectors) {
        List<Vertex3D> verts = new ArrayList<>();
        List<Face3D> faces = new ArrayList<>();
        double radius = 0.95;

        for (int r = 0; r <= rings; r++) {
            double phi = Math.PI * (double) r / rings;
            double sinPhi = Math.sin(phi);
            double cosPhi = Math.cos(phi);

            for (int s = 0; s <= sectors; s++) {
                double theta = 2.0 * Math.PI * (double) s / sectors;
                double sinTheta = Math.sin(theta);
                double cosTheta = Math.cos(theta);

                double x = radius * cosTheta * sinPhi;
                double y = radius * cosPhi;
                double z = radius * sinTheta * sinPhi;

                // Color según la posición del vértice para efectos atractivos
                int red = (int) ((cosTheta + 1.0) * 127);
                int green = (int) ((cosPhi + 1.0) * 127);
                int blue = (int) ((sinTheta + 1.0) * 127);
                int color = 0xFF000000 | (red << 16) | (green << 8) | blue;

                verts.add(new Vertex3D(x, y, z, color));
            }
        }

        for (int r = 0; r < rings; r++) {
            for (int s = 0; s < sectors; s++) {
                int first = r * (sectors + 1) + s;
                int second = first + sectors + 1;

                // Crear dos triángulos por celda
                faces.add(new Face3D(new int[]{first, second, first + 1}, 0xFF5AC8FA));
                faces.add(new Face3D(new int[]{second, second + 1, first + 1}, 0xFF5AC8FA));
            }
        }

        sphereVertices = verts.toArray(new Vertex3D[0]);
        sphereFaces = faces.toArray(new Face3D[0]);
    }

    private void initToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setMargin(new Insets(6, 12, 6, 12));

        JLabel lblLogo = new JLabel("✨ LuminaFX | Renderizado 3D");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLogo.setForeground(new Color(99, 102, 241));
        toolBar.add(lblLogo);
        toolBar.add(Box.createRigidArea(new Dimension(15, 0)));

        JButton btnVolver = new JButton("Volver");
        btnVolver.putClientProperty("JButton.buttonType", "toolBarButton");
        btnVolver.setIcon(new ModernIcon("volver", 16));
        btnVolver.addActionListener(e -> {
            animationTimer.stop();
            parentFrame.mostrarSimpleMode();
        });

        toolBar.add(btnVolver);
        toolBar.add(Box.createHorizontalGlue());

        add(toolBar, BorderLayout.NORTH);
    }

    private void initSidebar() {
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Component.borderColor")));
        sidebarWrapper.setPreferredSize(new Dimension(300, 0));

        ScrollablePanel sidebarContent = new ScrollablePanel();
        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
        sidebarContent.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // --- SECCIÓN 1: CONFIGURACIÓN DE FIGURA ---
        sidebarContent.add(crearEncabezado("CONFIGURACIÓN DE FIGURA", false));

        comboFiguras = new JComboBox<>(new String[]{"Cubo", "Pirámide", "Esfera", "Todos Juntos"});
        comboFiguras.addActionListener(e -> {
            int idx = comboFiguras.getSelectedIndex();
            activeShape = ShapeType.values()[idx];
        });
        sidebarContent.add(crearFilaControl("Figura 3D:", comboFiguras));

        comboModo = new JComboBox<>(new String[]{"Sólido Sombreado", "Alambre (Wireframe)", "Puntos (Vertices)", "Buffer Z (Profundidad)"});
        comboModo.addActionListener(e -> {
            int idx = comboModo.getSelectedIndex();
            activeRenderMode = RenderMode.values()[idx];
            // Bloquear selector de sombreado en modo alambre o puntos
            comboSombreado.setEnabled(activeRenderMode == RenderMode.SOLID_SHADED);
        });
        sidebarContent.add(crearFilaControl("Modo Render:", comboModo));

        comboSombreado = new JComboBox<>(new String[]{"Gouraud (Vertex)", "Flat (Por Cara)", "Sin Iluminación"});
        comboSombreado.addActionListener(e -> {
            int idx = comboSombreado.getSelectedIndex();
            activeShadingMode = ShadingMode.values()[idx];
        });
        sidebarContent.add(crearFilaControl("Iluminación:", comboSombreado));

        // --- SECCIÓN 2: CONFIGURACIÓN DEL MOTOR ---
        sidebarContent.add(crearEncabezado("CONFIGURACIÓN DEL MOTOR", true));

        checkZBuffer = new JCheckBox("Habilitar Prueba Z-Buffer", true);
        checkZBuffer.addActionListener(e -> rasterizer.setZBufferEnabled(checkZBuffer.isSelected()));
        checkZBuffer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarContent.add(checkZBuffer);
        sidebarContent.add(Box.createRigidArea(new Dimension(0, 8)));

        comboLineaAlgo = new JComboBox<>(new String[]{"Bresenham", "DDA"});
        comboLineaAlgo.addActionListener(e -> {
            int idx = comboLineaAlgo.getSelectedIndex();
            activeLineAlgo = LineAlgo.values()[idx];
        });
        sidebarContent.add(crearFilaControl("Algoritmo Líneas:", comboLineaAlgo));

        // --- SECCIÓN 3: ROTACIÓN Y CÁMARA ---
        sidebarContent.add(crearEncabezado("CÁMARA Y PERSPECTIVA", true));

        sliderZoom = new JSlider(15, 80, 40); // representa distancia cameraDist (1.5 a 8.0)
        sliderZoom.addChangeListener(e -> cameraDist = sliderZoom.getValue() / 10.0);
        sidebarContent.add(crearSliderControl("Dist. Cámara:", sliderZoom));

        sliderFov = new JSlider(30, 150, 80); // representa escala FOV
        sliderFov.addChangeListener(e -> fovScale = sliderFov.getValue() / 100.0);
        sidebarContent.add(crearSliderControl("Escala FOV (Zoom):", sliderFov));

        sidebarContent.add(crearEncabezado("ANIMACIÓN Y ROTACIÓN", true));

        sliderSpeedX = new JSlider(-100, 100, 10);
        sliderSpeedX.addChangeListener(e -> speedX = sliderSpeedX.getValue() / 1000.0);
        sidebarContent.add(crearSliderControl("Velocidad X:", sliderSpeedX));

        sliderSpeedY = new JSlider(-100, 100, 15);
        sliderSpeedY.addChangeListener(e -> speedY = sliderSpeedY.getValue() / 1000.0);
        sidebarContent.add(crearSliderControl("Velocidad Y:", sliderSpeedY));

        sliderSpeedZ = new JSlider(-100, 100, 0);
        sliderSpeedZ.addChangeListener(e -> speedZ = sliderSpeedZ.getValue() / 1000.0);
        sidebarContent.add(crearSliderControl("Velocidad Z:", sliderSpeedZ));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 8)));

        btnPlayPause = new JButton("Pausar Animación");
        btnPlayPause.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPlayPause.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnPlayPause.putClientProperty("JButton.buttonType", "roundRect");
        btnPlayPause.addActionListener(e -> {
            isPlaying = !isPlaying;
            btnPlayPause.setText(isPlaying ? "Pausar Animación" : "Iniciar Animación");
        });
        sidebarContent.add(btnPlayPause);

        // --- SECCIÓN 4: COLORES ---
        sidebarContent.add(crearEncabezado("APARIENCIA", true));

        comboBgColor = new JComboBox<>(new String[]{"Gris Oscuro", "Negro", "Azul Profundo", "Gris Claro"});
        comboBgColor.addActionListener(e -> {
            switch (comboBgColor.getSelectedIndex()) {
                case 1 -> bgColor = 0xFF000000;
                case 2 -> bgColor = 0xFF0B0F19;
                case 3 -> bgColor = 0xFFD2D2D7;
                default -> bgColor = 0xFF121214;
            }
        });
        sidebarContent.add(crearFilaControl("Fondo:", comboBgColor));

        comboLineColor = new JComboBox<>(new String[]{"Índigo", "Blanco", "Verde Esmeralda", "Rojo Coral"});
        comboLineColor.addActionListener(e -> {
            switch (comboLineColor.getSelectedIndex()) {
                case 1 -> meshLineColor = 0xFFFFFFFF;
                case 2 -> meshLineColor = 0xFF34C759;
                case 3 -> meshLineColor = 0xFFFF3B30;
                default -> meshLineColor = 0xFF6366F1;
            }
        });
        sidebarContent.add(crearFilaControl("Líneas/Puntos:", comboLineColor));

        sidebarContent.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(sidebarContent);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarWrapper.add(scroll, BorderLayout.CENTER);

        add(sidebarWrapper, BorderLayout.EAST);
    }

    private JPanel crearEncabezado(String titulo, boolean conMargenTop) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 10f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(new JSeparator(), BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        int top = conMargenTop ? 18 : 0;
        panel.setBorder(BorderFactory.createEmptyBorder(top, 0, 8, 0));
        return panel;
    }

    private JPanel crearFilaControl(String labelText, JComponent comp) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(110, 25));
        label.setMinimumSize(new Dimension(110, 25));
        panel.add(label, BorderLayout.WEST);
        panel.add(comp, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearSliderControl(String labelText, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(11f));
        panel.add(label, BorderLayout.NORTH);

        slider.setPreferredSize(new Dimension(150, 20));
        panel.add(slider, BorderLayout.CENTER);

        return panel;
    }

    private synchronized void recreateBuffer() {
        int w = Math.max(10, canvasPanel.getWidth());
        int h = Math.max(10, canvasPanel.getHeight());

        renderImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        rasterizer.bindTarget(renderImage);
    }

    private synchronized void renderFrame() {
        if (renderImage == null) return;

        int w = renderImage.getWidth();
        int h = renderImage.getHeight();

        // 1. Limpiar buffers
        rasterizer.clearColorBuffer(bgColor);
        rasterizer.clearZBuffer();

        // 2. Elegir figuras a dibujar
        List<DrawTask> drawTasks = new ArrayList<>();
        switch (activeShape) {
            case CUBE -> addShapeTasks(cubeVertices, cubeFaces, drawTasks, 0, 0, 0);
            case PYRAMID -> addShapeTasks(pyramidVertices, pyramidFaces, drawTasks, 0, 0, 0);
            case SPHERE -> addShapeTasks(sphereVertices, sphereFaces, drawTasks, 0, 0, 0);
            case COMBINED -> {
                // Dibujar las tres figuras espaciadas
                addShapeTasks(cubeVertices, cubeFaces, drawTasks, -1.8, 0, 0);
                addShapeTasks(pyramidVertices, pyramidFaces, drawTasks, 0, 0, 0);
                addShapeTasks(sphereVertices, sphereFaces, drawTasks, 1.8, 0, 0);
            }
        }

        // 3. Renderizar cada tarea usando el rasterizador
        for (DrawTask task : drawTasks) {
            int x1 = task.screenX[0], y1 = task.screenY[0];
            int x2 = task.screenX[1], y2 = task.screenY[1];
            int x3 = task.screenX[2], y3 = task.screenY[2];
            double z1 = task.projZ[0], z2 = task.projZ[1], z3 = task.projZ[2];

            if (activeRenderMode == RenderMode.SOLID_SHADED) {
                rasterizer.drawTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, task.c1, task.c2, task.c3);
            } else if (activeRenderMode == RenderMode.WIREFRAME) {
                if (activeLineAlgo == LineAlgo.BRESENHAM) {
                    rasterizer.drawLineBresenham(x1, y1, z1, x2, y2, z2, meshLineColor);
                    rasterizer.drawLineBresenham(x2, y2, z2, x3, y3, z3, meshLineColor);
                    rasterizer.drawLineBresenham(x3, y3, z3, x1, y1, z1, meshLineColor);
                } else {
                    rasterizer.drawLineDDA(x1, y1, z1, x2, y2, z2, meshLineColor);
                    rasterizer.drawLineDDA(x2, y2, z2, x3, y3, z3, meshLineColor);
                    rasterizer.drawLineDDA(x3, y3, z3, x1, y1, z1, meshLineColor);
                }
            } else if (activeRenderMode == RenderMode.POINTS) {
                rasterizer.drawPoint(x1, y1, z1, meshLineColor);
                rasterizer.drawPoint(x2, y2, z2, meshLineColor);
                rasterizer.drawPoint(x3, y3, z3, meshLineColor);
            } else if (activeRenderMode == RenderMode.Z_BUFFER_MAP) {
                // Primero rellenamos las figuras para actualizar el Z-buffer
                rasterizer.drawTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, task.c1, task.c2, task.c3);
            }
        }

        // 4. Si el modo de renderizado es mapa Z, dibujamos el Z-Buffer como escala de grises
        if (activeRenderMode == RenderMode.Z_BUFFER_MAP) {
            rasterizer.drawZBufferToColorBuffer();
        }

        canvasPanel.repaint();
    }

    private void addShapeTasks(Vertex3D[] vertices, Face3D[] faces, List<DrawTask> drawTasks, double tx, double ty, double tz) {
        int w = renderImage.getWidth();
        int h = renderImage.getHeight();

        // Precalcular valores trigonométricos
        double cosX = Math.cos(rotX), sinX = Math.sin(rotX);
        double cosY = Math.cos(rotY), sinY = Math.sin(rotY);
        double cosZ = Math.cos(rotZ), sinZ = Math.sin(rotZ);

        // 1. Transformación (Rotación y Traslación en el espacio del objeto)
        Vertex3D[] transformed = new Vertex3D[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            Vertex3D v = vertices[i];

            // Rotar alrededor de X
            double y1 = v.y * cosX - v.z * sinX;
            double z1 = v.y * sinX + v.z * cosX;
            double x1 = v.x;

            // Rotar alrededor de Y
            double x2 = x1 * cosY + z1 * sinY;
            double z2 = -x1 * sinY + z1 * cosY;
            double y2 = y1;

            // Rotar alrededor de Z
            double x3 = x2 * cosZ - y2 * sinZ;
            double y3 = x2 * sinZ + y2 * cosZ;
            double z3 = z2;

            // Trasladar en el espacio
            transformed[i] = new Vertex3D(x3 + tx, y3 + ty, z3 + tz, v.color);
        }

        // 2. Proyección en perspectiva de vértices a coordenadas de pantalla 2D
        int[] screenX = new int[vertices.length];
        int[] screenY = new int[vertices.length];
        double[] projZ = new double[vertices.length];

        double fov = Math.min(w, h) * 0.85 * fovScale;

        for (int i = 0; i < vertices.length; i++) {
            Vertex3D r = transformed[i];
            double depth = r.z + cameraDist;

            if (depth <= 0.1) depth = 0.1; // Clipping mínimo

            screenX[i] = (int) (r.x * fov / depth + w / 2.0);
            screenY[i] = (int) (-r.y * fov / depth + h / 2.0); // Invertir Y en pantalla
            projZ[i] = depth;
        }

        // Direcciones de luz e iluminación
        double lx = 0.3;
        double ly = 0.4;
        double lz = -1.0; // Desde atrás de la cámara hacia la escena
        double lLen = Math.sqrt(lx * lx + ly * ly + lz * lz);
        lx /= lLen; ly /= lLen; lz /= lLen;

        // 3. Crear tareas de dibujado por cada triángulo
        for (Face3D face : faces) {
            int idx1 = face.vIndices[0];
            int idx2 = face.vIndices[1];
            int idx3 = face.vIndices[2];

            // Vértices proyectados
            int x1 = screenX[idx1], y1 = screenY[idx1];
            int x2 = screenX[idx2], y2 = screenY[idx2];
            int x3 = screenX[idx3], y3 = screenY[idx3];

            // Validar si los tres están fuera de pantalla para descarte rápido
            if ((x1 < 0 && x2 < 0 && x3 < 0) || (x1 >= w && x2 >= w && x3 >= w) ||
                (y1 < 0 && y2 < 0 && y3 < 0) || (y1 >= h && y2 >= h && y3 >= h)) {
                continue;
            }

            // Calcular normal del triángulo para Flat Shading y Backface Culling
            Vertex3D r1 = transformed[idx1];
            Vertex3D r2 = transformed[idx2];
            Vertex3D r3 = transformed[idx3];

            double ax = r2.x - r1.x;
            double ay = r2.y - r1.y;
            double az = r2.z - r1.z;

            double bx = r3.x - r1.x;
            double by = r3.y - r1.y;
            double bz = r3.z - r1.z;

            double nx = ay * bz - az * by;
            double ny = az * bx - ax * bz;
            double nz = ax * by - ay * bx;

            double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 0) {
                nx /= len; ny /= len; nz /= len;
            }

            // Culling de caras traseras (Backface Culling) en modo sólido para optimizar
            if (activeRenderMode == RenderMode.SOLID_SHADED || activeRenderMode == RenderMode.Z_BUFFER_MAP) {
                // Vector visual desde el origen del triángulo hacia la cámara (0,0,-cameraDist)
                double vx = r1.x;
                double vy = r1.y;
                double vz = r1.z + cameraDist;
                double vLen = Math.sqrt(vx*vx + vy*vy + vz*vz);
                if (vLen > 0) { vx /= vLen; vy /= vLen; vz /= vLen; }

                double cullDot = nx * vx + ny * vy + nz * vz;
                // Si la cara está mirando en dirección contraria a la cámara (dot < 0), la descartamos
                if (cullDot < -0.05) {
                    continue;
                }
            }

            // Calcular iluminación
            int c1, c2, c3;

            if (activeShadingMode == ShadingMode.FLAT) {
                // Sombreado plano (Toda la cara comparte el mismo color de luz)
                double dot = nx * lx + ny * ly + nz * lz;
                double intensity = Math.max(0.12, dot); // 0.12 ambiental
                c1 = applyIntensity(face.color, intensity);
                c2 = c1;
                c3 = c1;
            } else if (activeShadingMode == ShadingMode.GOURAUD) {
                // Sombreado Gouraud (Interpolación de luz por vértice)
                // Como las figuras están centradas en sus orígenes locales (o desplazadas),
                // podemos aproximar el vector normal del vértice desde su centro de figura.
                c1 = calculateGouraudColor(transformed[idx1], vertices[idx1].color, lx, ly, lz, tx, ty, tz);
                c2 = calculateGouraudColor(transformed[idx2], vertices[idx2].color, lx, ly, lz, tx, ty, tz);
                c3 = calculateGouraudColor(transformed[idx3], vertices[idx3].color, lx, ly, lz, tx, ty, tz);
            } else {
                // Sin iluminación
                c1 = vertices[idx1].color;
                c2 = vertices[idx2].color;
                c3 = vertices[idx3].color;
            }

            DrawTask task = new DrawTask(
                    new int[]{x1, x2, x3},
                    new int[]{y1, y2, y3},
                    new double[]{projZ[idx1], projZ[idx2], projZ[idx3]},
                    c1, c2, c3
            );
            drawTasks.add(task);
        }
    }

    private int calculateGouraudColor(Vertex3D r, int baseColor, double lx, double ly, double lz, double tx, double ty, double tz) {
        // Obtener la posición del vértice relativa al centro del objeto (tx, ty, tz)
        // para estimar la normal del vértice
        double nx = r.x - tx;
        double ny = r.y - ty;
        double nz = r.z - tz;

        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0) {
            nx /= len; ny /= len; nz /= len;
        }

        double dot = nx * lx + ny * ly + nz * lz;
        double intensity = Math.max(0.15, dot);

        return applyIntensity(baseColor, intensity);
    }

    private int applyIntensity(int color, double intensity) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * intensity);
        int g = (int) (((color >> 8) & 0xFF) * intensity);
        int b = (int) ((color & 0xFF) * intensity);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // --- CLASES AUXILIARES INTERNAS DE MODELADO ---

    private static class Vertex3D {
        double x, y, z;
        int color;

        Vertex3D(double x, double y, double z, int color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = color;
        }
    }

    private static class Face3D {
        int[] vIndices;
        int color;

        Face3D(int[] vIndices, int color) {
            this.vIndices = vIndices;
            this.color = color;
        }
    }

    private static class DrawTask {
        int[] screenX;
        int[] screenY;
        double[] projZ;
        int c1, c2, c3;

        DrawTask(int[] screenX, int[] screenY, double[] projZ, int c1, int c2, int c3) {
            this.screenX = screenX;
            this.screenY = screenY;
            this.projZ = projZ;
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
        }
    }

    // Canvas de dibujado personalizado
    private class RasterizerCanvas extends JPanel {
        public RasterizerCanvas() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (renderImage != null) {
                // Centrar la imagen en el lienzo
                int dx = (getWidth() - renderImage.getWidth()) / 2;
                int dy = (getHeight() - renderImage.getHeight()) / 2;
                g.drawImage(renderImage, dx, dy, null);
            }
        }
    }

    // Panel con soporte para Scrollable para ajustar su ancho al JScrollPane
    private static class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // Fuerza a que el ancho coincida con el JScrollPane, evitando cortes laterales
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
