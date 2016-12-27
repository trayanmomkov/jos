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
    private Image offScreenImage = null;
    private Graphics offScreenGraphics = null;
    private Color backgroundColor;

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
        if (offScreenImage == null) {
            /** Double-buffer: clear the offscreen image. */
            offScreenImage = createImage(dimension.width, dimension.height);
        }
        offScreenGraphics = offScreenImage.getGraphics();
        offScreenGraphics.setColor(backgroundColor);
        offScreenGraphics.fillRect(0, 0, dimension.width, dimension.height);

        /** Paint Offscreen */
        renderOffScreen(offScreenImage.getGraphics());
        g.drawImage(offScreenImage, 0, 0, null);
    }

    public void renderOffScreen(final Graphics g) {
        if (shapes != null) {
            for (Object element : shapes) {
                Shape shape = (Shape) element;
                ((Graphics2D) g).draw(shape);
            }
        }
    }

    public void draw(List<Shape> shapes) {
        this.shapes = shapes;
        repaint();
    }
}
