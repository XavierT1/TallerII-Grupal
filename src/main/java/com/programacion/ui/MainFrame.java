package com.programacion.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.programacion.core.ImageFilter;
import com.programacion.filters.GrayscaleFilter;
import com.programacion.filters.NegativeFilter;
import com.programacion.filters.FrostedGlassFilter;
import com.programacion.filters.ColorChannelFilter;
import com.programacion.filters.CircularFadeFilter;
import com.programacion.filters.BlackAndWhiteFilter;
import com.programacion.filters.RetroEffectFilter;
import com.programacion.filters.RetroTwoFilter;
import com.programacion.filters.RadialGradientFilter;
import com.programacion.filters.StretchingFilter;
import com.programacion.filters.ConvolutionFilter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    // 1. Atributos (Lo que la ventana "tiene")
    private BufferedImage originalImage;
    private BufferedImage filteredImage;

    private JLabel labelOriginal = new JLabel("Sin imagen", SwingConstants.CENTER);
    private JLabel labelFiltered = new JLabel("Sin filtro", SwingConstants.CENTER);

    private boolean isDarkMode = true;

    // Referencias a botones para iconos
    private JButton btnCargar, btnGuardar, btnTema;

    public MainFrame() {
        // Configuración básica de la ventana
        setTitle("Editor de Imágenes Universitario");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Usamos BorderLayout: Arriba (Norte), Izquierda (Oeste) y Centro
        setLayout(new BorderLayout());

        // Llamamos a los bloques principales de la interfaz
        initToolbar();
        initMenu();
        initSidebar();
        initWorkspace();

        // Ponemos los iconos iniciales
        updateIcons();
    }

    // --- BLOQUE 0: BARRA DE HERRAMIENTAS (Con iconos) ---
    private void initToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        btnCargar = new JButton("Cargar");
        btnCargar.addActionListener(e -> accionCargar());

        btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> accionGuardar());

        btnTema = new JButton("Tema");
        btnTema.addActionListener(e -> accionCambiarTema());

        toolBar.add(btnCargar);
        toolBar.addSeparator();
        toolBar.add(btnGuardar);
        toolBar.add(Box.createHorizontalGlue()); // Empuja el tema a la derecha
        toolBar.add(btnTema);

        add(toolBar, BorderLayout.NORTH);
    }

    // --- LÓGICA DE ICONOS ---
    private void updateIcons() {
        btnCargar.setIcon(loadIcon("/assets/icons/add.png", 24));
        btnGuardar.setIcon(loadIcon("/assets/icons/save.png", 24));
        btnTema.setIcon(loadIcon("/assets/icons/theme.png", 24));
    }

    private ImageIcon loadIcon(String path, int size) {
        try {
            java.net.URL imgUrl = getClass().getResource(path);
            if (imgUrl == null)
                return null;
            BufferedImage img = ImageIO.read(imgUrl);
            if (!isDarkMode)
                img = invertImageColors(img);
            Image resized = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage invertImageColors(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = image.getRGB(x, y);
                Color c = new Color(p, true);
                res.setRGB(x, y,
                        new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha()).getRGB());
            }
        }
        return res;
    }

    // --- BLOQUE 1: MENÚ SUPERIOR ---
    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");

        JMenuItem itemCargar = new JMenuItem("Abrir Imagen");
        itemCargar.addActionListener(e -> accionCargar());

        JMenuItem itemGuardar = new JMenuItem("Guardar Como...");
        itemGuardar.addActionListener(e -> accionGuardar());

        JMenuItem itemTema = new JMenuItem("Cambiar Color (Tema)");
        itemTema.addActionListener(e -> accionCambiarTema());

        menuArchivo.add(itemCargar);
        menuArchivo.add(itemGuardar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemTema);

        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);
    }

    // --- BLOQUE 2: BARRA LATERAL (Aquí añades filtros fácilmente) ---
    private void initSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createTitledBorder("Filtros Disponibles"));
        sidebar.setPreferredSize(new Dimension(220, 0));

        // 1. Filtros normales
        List<ImageFilter> misFiltros = new ArrayList<>();
        misFiltros.add(new GrayscaleFilter(255));
        misFiltros.add(new NegativeFilter());
        misFiltros.add(new FrostedGlassFilter());
        misFiltros.add(new CircularFadeFilter());
        misFiltros.add(new BlackAndWhiteFilter());
        misFiltros.add(ConvolutionFilter.Enfoque());
        misFiltros.add(ConvolutionFilter.Desenfoque());
        misFiltros.add(ConvolutionFilter.DesenfoquePesado());
        misFiltros.add(ConvolutionFilter.Bordes());
        misFiltros.add(ConvolutionFilter.Aclarar());
        misFiltros.add(ConvolutionFilter.Oscurecer());

        for (ImageFilter f : misFiltros) {
            JButton btn = new JButton(f.getName());
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(200, 30));
            btn.addActionListener(e -> aplicarFiltro(f));
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
            sidebar.add(btn);
        }

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(new JSeparator());
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // 2. Botón Especial: Comparativa de Bits
        JButton btnComparar = new JButton("Comparativa de Bits");
        btnComparar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnComparar.setMaximumSize(new Dimension(200, 40));
        btnComparar.setFont(btnComparar.getFont().deriveFont(Font.BOLD));
        btnComparar.addActionListener(e -> accionComparativaBits());
        sidebar.add(btnComparar);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnCompararCanales = new JButton("Comparativa de Canales");
        btnCompararCanales.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompararCanales.setMaximumSize(new Dimension(200, 40));
        btnCompararCanales.setFont(btnCompararCanales.getFont().deriveFont(Font.BOLD));
        btnCompararCanales.addActionListener(e -> accionComparativaCanales());
        sidebar.add(btnCompararCanales);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnCompararRetro1 = new JButton("Comparativa Retro 1");
        btnCompararRetro1.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompararRetro1.setMaximumSize(new Dimension(200, 40));
        btnCompararRetro1.setFont(btnCompararRetro1.getFont().deriveFont(Font.BOLD));
        btnCompararRetro1.addActionListener(e -> accionComparativaRetro1());
        sidebar.add(btnCompararRetro1);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnCompararRetro2 = new JButton("Comparativa Retro 2");
        btnCompararRetro2.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompararRetro2.setMaximumSize(new Dimension(200, 40));
        btnCompararRetro2.setFont(btnCompararRetro2.getFont().deriveFont(Font.BOLD));
        btnCompararRetro2.addActionListener(e -> accionComparativaRetro2());
        sidebar.add(btnCompararRetro2);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnCompararRadiales = new JButton("Comparativa Radiales");
        btnCompararRadiales.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompararRadiales.setMaximumSize(new Dimension(200, 40));
        btnCompararRadiales.setFont(btnCompararRadiales.getFont().deriveFont(Font.BOLD));
        btnCompararRadiales.addActionListener(e -> accionComparativaRadiales());
        sidebar.add(btnCompararRadiales);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnCompararEstiramiento = new JButton("Comparativa Estiramiento");
        btnCompararEstiramiento.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompararEstiramiento.setMaximumSize(new Dimension(200, 40));
        btnCompararEstiramiento.setFont(btnCompararEstiramiento.getFont().deriveFont(Font.BOLD));
        btnCompararEstiramiento.addActionListener(e -> accionComparativaEstiramiento());
        sidebar.add(btnCompararEstiramiento);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnCompararConvolucion = new JButton("Comparativa Convoluciones");
        btnCompararConvolucion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompararConvolucion.setMaximumSize(new Dimension(200, 40));
        btnCompararConvolucion.setFont(btnCompararConvolucion.getFont().deriveFont(Font.BOLD));
        btnCompararConvolucion.addActionListener(e -> accionComparativaConvoluciones());
        sidebar.add(btnCompararConvolucion);

        // Botón para resetear (limpiar)
        JButton btnReset = new JButton("Limpiar Todo");
        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReset.setMaximumSize(new Dimension(200, 40));
        btnReset.setBackground(new Color(200, 50, 50));
        btnReset.setForeground(Color.WHITE);
        btnReset.addActionListener(e -> accionReset());

        JScrollPane scroll = new JScrollPane(sidebar);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scroll, BorderLayout.CENTER);
        container.add(btnReset, BorderLayout.SOUTH);

        add(container, BorderLayout.WEST);
    }

    // --- BLOQUE 3: ÁREA DE TRABAJO (Imágenes) ---
    private JScrollPane scrollFiltered; // Guardamos referencia para cambiar el contenido

    private void initWorkspace() {
        scrollFiltered = new JScrollPane(labelFiltered);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setTopComponent(new JScrollPane(labelOriginal));
        split.setBottomComponent(scrollFiltered);
        split.setDividerLocation(300);
        split.setResizeWeight(0.5);

        add(split, BorderLayout.CENTER);
    }

    // --- LÓGICA DE ACCIONES ---

    // --- NUEVO MÉTODO: ESCALAR IMAGEN PARA QUE QUEPA ---
    private ImageIcon prepararImagenParaLabel(BufferedImage img, boolean esComparativa) {
        if (img == null)
            return null;

        // Definimos un ancho objetivo
        // Si es comparativa, las hacemos miniaturas (250px)
        // Si es la principal, intentamos que ocupe el ancho del panel disponible
        int targetWidth = esComparativa ? 250 : 750;

        // Calculamos el alto manteniendo la proporción (aspect ratio)
        double ratio = (double) img.getHeight() / img.getWidth();
        int targetHeight = (int) (targetWidth * ratio);

        // Redimensionamos con suavizado para que no pierda tanta calidad visual
        Image escalada = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }

    private void accionCargar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageIO.read(chooser.getSelectedFile());
                filteredImage = originalImage;

                labelOriginal.setIcon(prepararImagenParaLabel(originalImage, false));
                labelOriginal.setText("");

                labelFiltered.setIcon(prepararImagenParaLabel(filteredImage, false));
                labelFiltered.setText("");

                scrollFiltered.setViewportView(labelFiltered);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage());
            }
        }
    }

    private void accionComparativaBits() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Carga una imagen primero.");
            return;
        }

        int[] niveles = { 2, 4, 8, 64, 128, 255 };

        // Usamos un panel con envoltura automática (FlowLayout)
        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelGrid.setPreferredSize(new Dimension(900, 1200)); // Tamaño sugerido para el scroll

        for (int n : niveles) {
            GrayscaleFilter f = new GrayscaleFilter(n);
            BufferedImage imgResult = f.apply(originalImage);

            JPanel item = new JPanel(new BorderLayout());
            // Mostramos versión pequeña en la comparativa
            JLabel imgLabel = new JLabel(prepararImagenParaLabel(imgResult, true));
            JLabel infoLabel = new JLabel("Nivel Gris N = " + n, SwingConstants.CENTER);
            infoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

            item.add(imgLabel, BorderLayout.CENTER);
            item.add(infoLabel, BorderLayout.SOUTH);
            item.setBorder(BorderFactory.createEtchedBorder());

            panelGrid.add(item);
        }

        scrollFiltered.setViewportView(panelGrid);
        revalidate();
        repaint();
    }

    private void accionComparativaCanales() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Carga una imagen primero.");
            return;
        }

        // Tipos: 1:Rojo, 2:Verde, 3:Azul, 4:Mitad, 5:Tercios
        int[] tipos = { 1, 2, 3, 4, 5 };

        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelGrid.setPreferredSize(new Dimension(900, 1200));

        for (int t : tipos) {
            ColorChannelFilter f = new ColorChannelFilter(t);
            BufferedImage imgResult = f.apply(originalImage);

            JPanel item = new JPanel(new BorderLayout());
            JLabel imgLabel = new JLabel(prepararImagenParaLabel(imgResult, true));
            JLabel infoLabel = new JLabel(f.getName(), SwingConstants.CENTER);
            infoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

            item.add(imgLabel, BorderLayout.CENTER);
            item.add(infoLabel, BorderLayout.SOUTH);
            item.setBorder(BorderFactory.createEtchedBorder());

            panelGrid.add(item);
        }

        scrollFiltered.setViewportView(panelGrid);
        revalidate();
        repaint();
    }

    private void accionComparativaRetro1() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Carga una imagen primero.");
            return;
        }

        int[] niveles = { 2, 4, 8, 64, 128, 255 };
        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelGrid.setPreferredSize(new Dimension(900, 1200));

        for (int n : niveles) {
            RetroEffectFilter f = new RetroEffectFilter(n);
            BufferedImage imgResult = f.apply(originalImage);

            JPanel item = new JPanel(new BorderLayout());
            JLabel imgLabel = new JLabel(prepararImagenParaLabel(imgResult, true));
            JLabel infoLabel = new JLabel("Retro 1 (RGB) N = " + n, SwingConstants.CENTER);
            infoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

            item.add(imgLabel, BorderLayout.CENTER);
            item.add(infoLabel, BorderLayout.SOUTH);
            item.setBorder(BorderFactory.createEtchedBorder());

            panelGrid.add(item);
        }

        scrollFiltered.setViewportView(panelGrid);
        revalidate();
        repaint();
    }

    private void accionComparativaRetro2() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Carga una imagen primero.");
            return;
        }

        int[] niveles = { 2, 4, 8, 64, 128, 255 };
        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelGrid.setPreferredSize(new Dimension(900, 1200));

        for (int n : niveles) {
            RetroTwoFilter f = new RetroTwoFilter(n, 1); // Modo 1: RG
            BufferedImage imgResult = f.apply(originalImage);

            JPanel item = new JPanel(new BorderLayout());
            JLabel imgLabel = new JLabel(prepararImagenParaLabel(imgResult, true));
            JLabel infoLabel = new JLabel("Retro 2 (RG) N = " + n, SwingConstants.CENTER);
            infoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

            item.add(imgLabel, BorderLayout.CENTER);
            item.add(infoLabel, BorderLayout.SOUTH);
            item.setBorder(BorderFactory.createEtchedBorder());

            panelGrid.add(item);
        }

        scrollFiltered.setViewportView(panelGrid);
        revalidate();
        repaint();
    }

    private void accionComparativaRadiales() {
        int[] tipos = { 1, 2, 3, 4, 5 };
        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelGrid.setPreferredSize(new Dimension(900, 1200));
        
        for (int t : tipos) {
            RadialGradientFilter f = new RadialGradientFilter(t);
            BufferedImage imgResult = f.apply(originalImage);
            
            JPanel item = new JPanel(new BorderLayout());
            JLabel imgLabel = new JLabel(prepararImagenParaLabel(imgResult, true));
            JLabel infoLabel = new JLabel(f.getName(), SwingConstants.CENTER);
            infoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            
            item.add(imgLabel, BorderLayout.CENTER);
            item.add(infoLabel, BorderLayout.SOUTH);
            item.setBorder(BorderFactory.createEtchedBorder());
            
            panelGrid.add(item);
        }

        scrollFiltered.setViewportView(panelGrid);
        revalidate();
        repaint();
    }

    private void accionComparativaEstiramiento() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Carga una imagen primero.");
            return;
        }

        // Mostraremos: 2 bits RGB, 2 bits HSV, 4 bits RGB, 4 bits HSV, 8 bits RGB, 8 bits HSV
        int[] bitsArr = { 2, 4, 8 };
        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelGrid.setPreferredSize(new Dimension(900, 1500));
        
        for (int b : bitsArr) {
            // Variante RGB
            StretchingFilter fRGB = new StretchingFilter(b, 1);
            BufferedImage imgRGB = fRGB.apply(originalImage);
            
            JPanel itemRGB = new JPanel(new BorderLayout());
            itemRGB.add(new JLabel(prepararImagenParaLabel(imgRGB, true)), BorderLayout.CENTER);
            itemRGB.add(new JLabel(fRGB.getName(), SwingConstants.CENTER), BorderLayout.SOUTH);
            itemRGB.setBorder(BorderFactory.createEtchedBorder());
            panelGrid.add(itemRGB);

            // Variante HSV
            StretchingFilter fHSV = new StretchingFilter(b, 2);
            BufferedImage imgHSV = fHSV.apply(originalImage);
            
            JPanel itemHSV = new JPanel(new BorderLayout());
            itemHSV.add(new JLabel(prepararImagenParaLabel(imgHSV, true)), BorderLayout.CENTER);
            itemHSV.add(new JLabel(fHSV.getName(), SwingConstants.CENTER), BorderLayout.SOUTH);
            itemHSV.setBorder(BorderFactory.createEtchedBorder());
            panelGrid.add(itemHSV);
        }

        scrollFiltered.setViewportView(panelGrid);
        revalidate();
        repaint();
    }

    private void accionComparativaConvoluciones() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Carga una imagen primero.");
            return;
        }

        ImageFilter[] convs = {
            ConvolutionFilter.Enfoque(),
            ConvolutionFilter.Desenfoque(),
            ConvolutionFilter.DesenfoquePesado(),
            ConvolutionFilter.Bordes(),
            ConvolutionFilter.Aclarar(),
            ConvolutionFilter.Oscurecer()
        };

        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelGrid.setPreferredSize(new Dimension(900, 1500));
        
        for (ImageFilter f : convs) {
            BufferedImage imgResult = f.apply(originalImage);
            
            JPanel item = new JPanel(new BorderLayout());
            item.add(new JLabel(prepararImagenParaLabel(imgResult, true)), BorderLayout.CENTER);
            item.add(new JLabel(f.getName(), SwingConstants.CENTER), BorderLayout.SOUTH);
            item.setBorder(BorderFactory.createEtchedBorder());
            panelGrid.add(item);
        }

        scrollFiltered.setViewportView(panelGrid);
        revalidate();
        repaint();
    }

    private void aplicarFiltro(ImageFilter filtro) {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Carga una imagen primero.");
            return;
        }
        filteredImage = filtro.apply(originalImage);
        labelFiltered.setIcon(prepararImagenParaLabel(filteredImage, false));

        // Nos aseguramos de mostrar el label individual en el scroll
        scrollFiltered.setViewportView(labelFiltered);
    }

    private void accionReset() {
        if (originalImage != null) {
            filteredImage = originalImage;
            labelFiltered.setIcon(prepararImagenParaLabel(originalImage, false));
            scrollFiltered.setViewportView(labelFiltered);
        }
    }

    private void accionGuardar() {
        if (filteredImage == null)
            return;

        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File output = chooser.getSelectedFile();
                if (!output.getName().toLowerCase().endsWith(".png")) {
                    output = new File(output.getAbsolutePath() + ".png");
                }
                ImageIO.write(filteredImage, "png", output);
                JOptionPane.showMessageDialog(this, "Imagen guardada!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar.");
            }
        }
    }

    private void accionCambiarTema() {
        try {
            isDarkMode = !isDarkMode;
            updateIcons(); // Actualiza los colores de los iconos

            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
