package info.trekto.jos.gui.java2dgraphics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.util.Utils.info;

/**
 * @author Trayan Momkov
 * 2016-Oct-17 23:16
 */
public class VisualizationPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(VisualizationPanel.class);

    private final double scaleStep = 0.1;
    private final double translateStep = 1;
    private List<ShapeWithColorAndText> shapes;
    private Image image = null;
    private final Color backgroundColor;
    private double scale = 1;
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
    public void paint(Graphics g) {

        final Dimension dimension = getSize();
        if (image == null) {
            /* Double-buffer: clear the offscreen image. */
            image = createImage(dimension.width, dimension.height);
            //            image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        }
        Graphics graphics = image.getGraphics();
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, dimension.width, dimension.height);

        /* Paint Offscreen */
        renderOffScreen(image.getGraphics());

        /* Scaling */
        ((Graphics2D) g).scale(scale, scale);

        /* Translating */
        ((Graphics2D) g).translate(translateX, translateY);

        g.drawImage(image, 0, 0, null);
    }

    public void renderOffScreen(final Graphics g) {
        if (shapes != null) {
            for (ShapeWithColorAndText shape : shapes) {
                g.setColor(shape.getColor());
                if (shape.getText() != null) {
                    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, C.getFontSize()));
                    ((Graphics2D) g).drawString(shape.getText(),
                                                Math.round(shape.getShape().getBounds2D().getX()),
                                                Math.round(shape.getShape().getBounds2D().getY()));
                } else {
                    ((Graphics2D) g).fill(shape.getShape());
                }
            }
        }
    }

    public void draw(List<ShapeWithColorAndText> shapes) {
        this.shapes = shapes;
        repaint();
    }

    public void zoomIn() {
        info(logger, "zoomIn");
        scale += scaleStep;
    }

    public void zoomOut() {
        info(logger, "zoomOut");
        scale -= scaleStep;
    }

    public void translateLeft() {
        translateX += translateStep;
    }

    public void translateUp() {
        translateY += translateStep;
    }

    public void translateRight() {
        translateX -= translateStep;
    }

    public void translateDown() {
        translateY -= translateStep;
    }

}
