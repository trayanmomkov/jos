package info.trekto.jos.gui.java2dgraphics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.Controller.DEFAULT_SCALE;
import static info.trekto.jos.util.Utils.info;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;

/**
 * @author Trayan Momkov
 * 2016-Oct-17 23:16
 */
public class VisualizationPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(VisualizationPanel.class);

    private static final double SCALE_STEP = 1.25;
    private static final double TRANSLATE_STEP = 50;
    public static final int DEFAULT_FONT_SIZE = 18;
    public static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, DEFAULT_FONT_SIZE);
    private List<ShapeWithColorAndText> shapes;
    private final Color backgroundColor;
    private double scale = DEFAULT_SCALE;
    private double translateX = 0;
    private double translateY = 0;

    public VisualizationPanel(Color backgroundColor) {
        super();
        this.backgroundColor = backgroundColor;
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int displayWidth = gd.getDisplayMode().getWidth();
        int displayHeight = gd.getDisplayMode().getHeight();
        setSize(displayWidth, displayHeight);
        setBackground(backgroundColor);
    }

    @Override
    public void paint(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        /* Antialiasing is the dominant playback cost with thousands of objects, so it is a toggle
         * (the "Antialiasing" checkbox, read each paint). Off (default) = harder edges but far faster. */
        boolean antialiasing = C.isAntialiasing();
        g.setRenderingHint(KEY_ANTIALIASING, antialiasing ? VALUE_ANTIALIAS_ON : VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(KEY_RENDERING, antialiasing ? VALUE_RENDER_QUALITY : VALUE_RENDER_SPEED);

        drawShapes(g);
    }

    /* Draws on top of the transform which g already has, so the same code serves the screen and the PNG export. */
    private void drawShapes(Graphics2D g) {
        if (shapes == null) {
            return;
        }

        drawMetadata(g, shapes);   // filters metadata inline - no per-paint stream/collect of 8000 shapes

        AffineTransform saved = g.getTransform();
        g.translate(getWidth() * (1 - scale) / 2.0 + translateX, getHeight() * (1 - scale) / 2.0 + translateY);
        g.scale(scale, scale);
        drawObjects(g, shapes);    // filters non-metadata inline
        g.setTransform(saved);
    }

    /* Draws the current view, with antialiasing, in an image of the given size. The whole view is fitted
     * inside the image and centred, so nothing is cut. */
    public BufferedImage renderToImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
        if (getWidth() > 0 && getHeight() > 0) {
            double k = Math.min((double) width / getWidth(), (double) height / getHeight());  // fit the whole view in - never clip
            g.translate((width - getWidth() * k) / 2.0, (height - getHeight() * k) / 2.0);    // centre it both ways
            g.scale(k, k);
            drawShapes(g);                                    // same drawing code as the screen, now scaled
        }
        g.dispose();
        return image;
    }

    private void drawObjects(final Graphics2D g, List<ShapeWithColorAndText> shapesList) {
        for (ShapeWithColorAndText shape : shapesList) {
            if (shape.isMetaData()) {
                continue;
            }
            g.setColor(shape.getColor());
            if (shape.getText() != null) {
                drawString(g, shape, (int) Math.round(C.getFontSize() / scale));
            } else {
                g.fill(shape.getShape());
            }
        }
    }

    private void drawMetadata(final Graphics2D g, List<ShapeWithColorAndText> shapesList) {
        for (ShapeWithColorAndText shape : shapesList) {
            if (!shape.isMetaData()) {
                continue;
            }
            g.setColor(shape.getColor());
            drawString(g, shape, C.getFontSize());
        }
    }

    private void drawString(Graphics2D g, ShapeWithColorAndText shape, int fontSize) {
        g.setFont(fontSize == DEFAULT_FONT_SIZE ? DEFAULT_FONT : new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        g.drawString(shape.getText(),
                     Math.round(shape.getShape().getBounds2D().getX()),
                     Math.round(shape.getShape().getBounds2D().getY()));
    }

    public void draw(List<ShapeWithColorAndText> shapes) {
        this.shapes = shapes;
        repaint();
    }

    public void zoomIn() {
        info(logger, "zoomIn");
        scale *= SCALE_STEP;
        C.updateScaleLabel(scale);
    }

    public void zoomOut() {
        info(logger, "zoomOut");
        scale /= SCALE_STEP;
        C.updateScaleLabel(scale);
    }

    public void translateLeft() {
        translateX += TRANSLATE_STEP;
    }

    public void translateUp() {
        translateY += TRANSLATE_STEP;
    }

    public void translateRight() {
        translateX -= TRANSLATE_STEP;
    }

    public void translateDown() {
        translateY -= TRANSLATE_STEP;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}
