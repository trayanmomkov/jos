/**
 *
 */
package visualization.java2dgraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Shape;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author Trayan Momkov
 * @date 2016-окт-17 23:16
 *
 */
public class VisualizationPanel extends JPanel {

    private List<Shape> shapes;
    private int displayWidth;
    private int displayHeight;
    private Image image = null;
    private Graphics graphics = null;
    private Color backgroundColor;
    private double scale = 1;
    private final double scaleStep = 0.1;
    private double translateX = 0;
    private double translateY = 0;
    private final double translateStep = 1;

    public VisualizationPanel(Color backgroundColor) {
        super();
        this.backgroundColor = backgroundColor;
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        displayWidth = gd.getDisplayMode().getWidth();
        displayHeight = gd.getDisplayMode().getHeight();
        setSize(displayWidth, displayHeight);
        setBackground(backgroundColor);
    }

    @Override
    public void paint(Graphics g) {

        final Dimension dimension = getSize();
        if (image == null) {
            /** Double-buffer: clear the offscreen image. */
            System.out.println("Double-buffer: clear the offscreen image.");
            image = createImage(dimension.width, dimension.height);
            //            image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        }
        graphics = image.getGraphics();
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, dimension.width, dimension.height);

        /** Paint Offscreen */
        renderOffScreen(image.getGraphics());

        /** Scaling */
        ((Graphics2D) g).scale(scale, scale);

        /** Translating */
        ((Graphics2D) g).translate(translateX, translateY);

        g.drawImage(image, 0, 0, null);
    }

    public void renderOffScreen(final Graphics g) {
        if (shapes != null) {
            for (Object element : shapes) {
                Shape shape = (Shape) element;
                ((Graphics2D) g).fill(shape);
            }
        }
    }

    public void draw(List<Shape> shapes) {
        this.shapes = shapes;
        repaint();
    }


    public void zoomIn() {
        System.out.println("zoomIn");
        scale += scaleStep;
    }


    public void zoomOut() {
        System.out.println("zoomOut");
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
