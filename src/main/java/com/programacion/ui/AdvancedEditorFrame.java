package com.programacion.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.programacion.core.ImageFilter;
import com.programacion.filters.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Stack;

public class AdvancedEditorFrame extends JFrame {
    private MainFrame parentFrame;
    private BufferedImage originalImage;
    private BufferedImage currentImage;
    private BufferedImage previewImage;
    private Stack<BufferedImage> history = new Stack<>();
    private Stack<String> filterNames = new Stack<>();

    private JLabel labelMain = new JLabel("Sin imagen", SwingConstants.CENTER);
    private JScrollPane scrollMain;
    private boolean isDarkMode;

    private JButton btnGuardar, btnDeshacer, btnReiniciar, btnVolver, btnTema;

    // Visual history
    private DefaultListModel<String> modelHistorial = new DefaultListModel<>();
    private JList<String> listHistorial;

    // Ventanas flotantes
    private JDialog ventanaTransparencia = null;
    private JDialog ventanaMascara = null;
    private JDialog ventanaRGB = null;

    private int ultimoValorTransparencia = 50;
    private int ultimoValorMascara = 4;
    private int valR = 0, valG = 0, valB = 0;

    public AdvancedEditorFrame(MainFrame parentFrame, BufferedImage initialImage, boolean isDarkMode) {
        this.parentFrame = parentFrame;
        this.originalImage = copyImage(initialImage);
        this.currentImage = copyImage(initialImage);
        this.isDarkMode = isDarkMode;

        setTitle("Editor Avanzado - Modo Apilado");
        setSize(1100, 750);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                volverAlPrincipal();
            }
        });

        initToolbar();
        initWorkspace();
        initRightSidebar();

        updateIcons();
        mostrarImagen(currentImage);
        actualizarHistorialVista();
    }

    private void initToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setMargin(new Insets(5, 10, 5, 10));

        btnVolver = new JButton("Volver al Editor Simple");
        btnVolver.addActionListener(e -> volverAlPrincipal());

        btnGuardar = new JButton("Guardar Resultado");
        btnGuardar.addActionListener(e -> accionGuardar());

        btnDeshacer = new JButton("Deshacer");
        btnDeshacer.addActionListener(e -> accionDeshacer());

        btnReiniciar = new JButton("Reiniciar Todo");
        btnReiniciar.addActionListener(e -> accionReiniciar());

        btnTema = new JButton("Tema");
        btnTema.addActionListener(e -> accionCambiarTema());

        toolBar.add(btnVolver);
        toolBar.addSeparator();
        toolBar.add(btnGuardar);
        toolBar.addSeparator();
        toolBar.add(btnDeshacer);
        toolBar.addSeparator();
        toolBar.add(btnReiniciar);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnTema);

        add(toolBar, BorderLayout.NORTH);
    }

    private void updateIcons() {
        btnGuardar.setIcon(loadIcon("/assets/icons/save.png", 20));
        btnTema.setIcon(loadIcon("/assets/icons/theme.png", 20));
        // Se pueden añadir más iconos si existen (undo, reset, back)
    }

    private ImageIcon loadIcon(String path, int size) {
        try {
            java.net.URL imgUrl = getClass().getResource(path);
            if (imgUrl == null) return null;
            BufferedImage img = ImageIO.read(imgUrl);
            if (!isDarkMode) img = invertImageColors(img);
            Image resized = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        } catch (Exception e) { return null; }
    }

    private BufferedImage invertImageColors(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = image.getRGB(x, y);
                Color c = new Color(p, true);
                res.setRGB(x, y, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha()).getRGB());
            }
        }
        return res;
    }

    private void initWorkspace() {
        scrollMain = new JScrollPane(labelMain);
        scrollMain.setBorder(BorderFactory.createEmptyBorder());
        
        // Mantener para ver Original (funcionalidad extra en el label)
        labelMain.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (originalImage != null) labelMain.setIcon(prepararImagenParaLabel(originalImage));
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentImage != null) labelMain.setIcon(prepararImagenParaLabel(currentImage));
            }
        });
        
        add(scrollMain, BorderLayout.CENTER);
    }

    private void initRightSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Filtros de Color
        sidebar.add(crearEncabezado("FILTROS DE COLOR", false));
        agregarBotonFiltro(sidebar, new GrayscaleFilter(255));
        agregarBotonFiltro(sidebar, new NegativeFilter());
        agregarBotonFiltro(sidebar, new BlackAndWhiteFilter());

        // Efectos Visuales
        sidebar.add(crearEncabezado("EFECTOS VISUALES", true));
        agregarBotonFiltro(sidebar, new FrostedGlassFilter());
        agregarBotonFiltro(sidebar, new CircularFadeFilter());

        // Convoluciones
        sidebar.add(crearEncabezado("ENFOQUE Y DESENFOQUE", true));
        ImageFilter[] convoluciones = {
            ConvolutionFilter.Enfoque(), ConvolutionFilter.Desenfoque(), ConvolutionFilter.DesenfoquePesado(),
            ConvolutionFilter.Bordes(), ConvolutionFilter.Aclarar(), ConvolutionFilter.Oscurecer()
        };
        JComboBox<String> comboConvolucion = new JComboBox<>();
        comboConvolucion.addItem("Seleccionar...");
        for (ImageFilter f : convoluciones) comboConvolucion.addItem(f.getName());
        comboConvolucion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        comboConvolucion.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboConvolucion.addActionListener(e -> {
            int idx = comboConvolucion.getSelectedIndex();
            if (idx > 0) {
                cerrarVentanasFlotantes();
                aplicarFiltro(convoluciones[idx - 1]);
                comboConvolucion.setSelectedIndex(0); // Reset para poder seleccionarlo de nuevo
            }
        });
        sidebar.add(comboConvolucion);

        // Herramientas Dinámicas
        sidebar.add(crearEncabezado("HERRAMIENTAS DINÁMICAS", true));
        
        JButton btnTrans = new JButton("Transparencia Ajustable");
        btnTrans.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnTrans.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTrans.addActionListener(e -> accionTransparenciaAjustable());
        sidebar.add(btnTrans);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton btnMasc = new JButton("Máscaras de Bits");
        btnMasc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnMasc.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMasc.addActionListener(e -> accionMascaraBitsAdjustable());
        sidebar.add(btnMasc);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton btnRGB = new JButton("Tonalidades RGB");
        btnRGB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnRGB.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRGB.setBackground(new Color(40, 120, 200));
        btnRGB.setForeground(Color.WHITE);
        btnRGB.addActionListener(e -> accionAjusteRGB());
        sidebar.add(btnRGB);

        // Historial de Efectos
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(crearEncabezado("HISTORIAL DE EFECTOS", true));
        
        listHistorial = new JList<>(modelHistorial);
        listHistorial.setEnabled(false); // Solo visual
        listHistorial.setBackground(UIManager.getColor("Panel.background"));
        JScrollPane scrollHistorial = new JScrollPane(listHistorial);
        scrollHistorial.setPreferredSize(new Dimension(250, 120));
        scrollHistorial.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollHistorial.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
        sidebar.add(scrollHistorial);

        sidebar.add(Box.createVerticalGlue());

        JScrollPane scrollSidebar = new JScrollPane(sidebar);
        scrollSidebar.setPreferredSize(new Dimension(280, 0));
        scrollSidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Component.borderColor")));
        add(scrollSidebar, BorderLayout.EAST);
    }

    private JPanel crearEncabezado(String titulo, boolean conMargenTop) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(new JSeparator(), BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        int top = conMargenTop ? 20 : 0;
        panel.setBorder(BorderFactory.createEmptyBorder(top, 0, 8, 0));
        return panel;
    }

    private void agregarBotonFiltro(JPanel contenedor, ImageFilter filtro) {
        JButton btn = new JButton(filtro.getName());
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.addActionListener(e -> {
            cerrarVentanasFlotantes();
            aplicarFiltro(filtro);
        });
        contenedor.add(btn);
        contenedor.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private ImageIcon prepararImagenParaLabel(BufferedImage img) {
        if (img == null) return null;
        int targetWidth = scrollMain.getWidth() - 20;
        if (targetWidth <= 0) targetWidth = 800;
        double ratio = (double) img.getHeight() / img.getWidth();
        int targetHeight = (int) (targetWidth * ratio);
        Image escalada = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }

    private void mostrarImagen(BufferedImage img) {
        if (img != null) {
            labelMain.setIcon(prepararImagenParaLabel(img));
            labelMain.setText(""); // Eliminar el texto "Sin imagen" cuando hay imagen
            scrollMain.setViewportView(labelMain);
        }
    }

    private void aplicarFiltro(ImageFilter filtro) {
        if (currentImage == null) return;
        
        // Guardamos el estado actual en el historial antes de aplicar el filtro
        history.push(copyImage(currentImage));
        filterNames.push(filtro.getName());
        
        // Aplicamos el filtro al currentImage (Apilamiento)
        currentImage = filtro.apply(currentImage);
        mostrarImagen(currentImage);
        btnDeshacer.setEnabled(true);
        actualizarHistorialVista();
    }

    private void aplicarPrevisualizacionDinamica(ImageFilter filtro) {
        if (currentImage == null) return;
        previewImage = filtro.apply(currentImage);
        mostrarImagen(previewImage);
    }

    private void confirmarHerramientaDinamica(String nombreFiltro) {
        if (previewImage != null) {
            history.push(copyImage(currentImage));
            filterNames.push(nombreFiltro);
            currentImage = copyImage(previewImage);
            previewImage = null;
            mostrarImagen(currentImage);
            actualizarHistorialVista();
        }
        cerrarVentanasFlotantes();
    }

    private void descartarHerramientaDinamica() {
        previewImage = null;
        mostrarImagen(currentImage);
        cerrarVentanasFlotantes();
    }

    private void accionDeshacer() {
        if (!history.isEmpty()) {
            cerrarVentanasFlotantes();
            currentImage = history.pop();
            filterNames.pop();
            mostrarImagen(currentImage);
            actualizarHistorialVista();
        } else {
            JOptionPane.showMessageDialog(this, "No hay más acciones para deshacer.");
        }
    }

    private void accionReiniciar() {
        cerrarVentanasFlotantes();
        history.clear();
        filterNames.clear();
        currentImage = copyImage(originalImage);
        mostrarImagen(currentImage);
        actualizarHistorialVista();
    }

    private void actualizarHistorialVista() {
        modelHistorial.clear();
        modelHistorial.addElement("Original");
        for (int i = 0; i < filterNames.size(); i++) {
            modelHistorial.addElement(" + " + filterNames.get(i));
        }
        // Hacer autoscroll hasta el último elemento añadido
        int lastIndex = listHistorial.getModel().getSize() - 1;
        if (lastIndex >= 0) {
            listHistorial.ensureIndexIsVisible(lastIndex);
        }
    }

    private void accionGuardar() {
        if (currentImage == null) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File output = chooser.getSelectedFile();
                if (!output.getName().toLowerCase().endsWith(".png")) {
                    output = new File(output.getAbsolutePath() + ".png");
                }
                ImageIO.write(currentImage, "png", output);
                JOptionPane.showMessageDialog(this, "Imagen guardada exitosamente!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar.");
            }
        }
    }

    private void accionCambiarTema() {
        try {
            isDarkMode = !isDarkMode;
            updateIcons();
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            SwingUtilities.updateComponentTreeUI(this);
            parentFrame.syncTheme(isDarkMode); // Synchronize with parent
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void volverAlPrincipal() {
        cerrarVentanasFlotantes();
        this.dispose();
        parentFrame.setVisible(true);
    }

    // --- Herramientas Dinámicas ---
    private void accionTransparenciaAjustable() {
        if (ventanaTransparencia != null && ventanaTransparencia.isVisible()) {
            ventanaTransparencia.toFront(); return;
        }

        ventanaTransparencia = new JDialog(this, "Ajustar Transparencia", false);
        ventanaTransparencia.setLayout(new BorderLayout());

        JSlider slider = new JSlider(0, 100, ultimoValorTransparencia);
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        slider.addChangeListener(e -> {
            ultimoValorTransparencia = slider.getValue();
            float factor = ultimoValorTransparencia / 100.0f;
            aplicarPrevisualizacionDinamica(new TransparenciaFilter(factor));
        });

        aplicarPrevisualizacionDinamica(new TransparenciaFilter(ultimoValorTransparencia / 100.0f));

        JPanel panelBotones = crearBotonesDinamicos("Transparencia Ajustable");
        ventanaTransparencia.add(slider, BorderLayout.CENTER);
        ventanaTransparencia.add(panelBotones, BorderLayout.SOUTH);
        ventanaTransparencia.pack();
        posicionarVentana(ventanaTransparencia, 70);
    }

    private void accionMascaraBitsAdjustable() {
        if (ventanaMascara != null && ventanaMascara.isVisible()) {
            ventanaMascara.toFront(); return;
        }

        ventanaMascara = new JDialog(this, "Máscaras de Bits", false);
        ventanaMascara.setLayout(new BorderLayout());

        JSlider slider = new JSlider(1, 8, ultimoValorMascara);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(BorderFactory.createTitledBorder("Bits por Canal (1-8)"));
        slider.setPreferredSize(new Dimension(250, 70));

        slider.addChangeListener(e -> {
            ultimoValorMascara = slider.getValue();
            aplicarPrevisualizacionDinamica(new BitMaskFilter(ultimoValorMascara, 0.8f));
        });

        aplicarPrevisualizacionDinamica(new BitMaskFilter(ultimoValorMascara, 0.8f));

        JPanel panelBotones = crearBotonesDinamicos("Máscaras de Bits");
        ventanaMascara.add(slider, BorderLayout.CENTER);
        ventanaMascara.add(panelBotones, BorderLayout.SOUTH);
        ventanaMascara.pack();
        posicionarVentana(ventanaMascara, 150);
    }

    private void accionAjusteRGB() {
        if (ventanaRGB != null && ventanaRGB.isVisible()) {
            ventanaRGB.toFront(); return;
        }

        ventanaRGB = new JDialog(this, "Ajuste RGB", false);
        ventanaRGB.setLayout(new BoxLayout(ventanaRGB.getContentPane(), BoxLayout.Y_AXIS));

        // Reset local values for new session
        valR = 0; valG = 0; valB = 0;

        JSlider sliderR = crearSliderRGB("Rojo", -255, 255);
        JSlider sliderG = crearSliderRGB("Verde", -255, 255);
        JSlider sliderB = crearSliderRGB("Azul", -255, 255);

        javax.swing.event.ChangeListener listener = e -> {
            valR = sliderR.getValue();
            valG = sliderG.getValue();
            valB = sliderB.getValue();
            aplicarPrevisualizacionDinamica(new RGBAdjustmentFilter(valR, valG, valB));
        };

        sliderR.addChangeListener(listener);
        sliderG.addChangeListener(listener);
        sliderB.addChangeListener(listener);

        ventanaRGB.add(sliderR);
        ventanaRGB.add(sliderG);
        ventanaRGB.add(sliderB);

        JPanel panelBotones = crearBotonesDinamicos("Ajuste Tonalidad RGB");
        ventanaRGB.add(panelBotones);

        ventanaRGB.pack();
        posicionarVentana(ventanaRGB, 230);
    }

    private JSlider crearSliderRGB(String titulo, int min, int max) {
        JSlider slider = new JSlider(min, max, 0);
        slider.setMajorTickSpacing(128);
        slider.setPaintTicks(true);
        slider.setBorder(BorderFactory.createTitledBorder(titulo));
        return slider;
    }

    private JPanel crearBotonesDinamicos(String nombreFiltro) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> descartarHerramientaDinamica());
        
        JButton btnAplicar = new JButton("Aplicar");
        btnAplicar.setBackground(new Color(40, 160, 80));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.addActionListener(e -> confirmarHerramientaDinamica(nombreFiltro));
        
        panel.add(btnCancelar);
        panel.add(btnAplicar);
        return panel;
    }

    private void posicionarVentana(JDialog dialog, int yOffset) {
        int xPos = this.getX() + this.getWidth() - dialog.getWidth() - 30;
        int yPos = this.getY() + yOffset;
        dialog.setLocation(xPos, yPos);
        dialog.setVisible(true);
    }

    private void cerrarVentanasFlotantes() {
        if (ventanaTransparencia != null) ventanaTransparencia.setVisible(false);
        if (ventanaMascara != null) ventanaMascara.setVisible(false);
        if (ventanaRGB != null) ventanaRGB.setVisible(false);
        // Si había una previsualización activa y el usuario cerró la ventana de golpe, se descarta.
        if (previewImage != null) {
            descartarHerramientaDinamica();
        }
    }

    // Helper for deep copy
    private BufferedImage copyImage(BufferedImage source) {
        if (source == null) return null;
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }
}
