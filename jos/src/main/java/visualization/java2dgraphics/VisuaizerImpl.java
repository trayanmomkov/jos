/**
 *
 */
package visualization.java2dgraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.swing.JFrame;

import info.trekto.jos.model.SimulationObject;
import visualization.Visualizer;

/**
 * @author Trayan Momkov
 * @date 2016-окт-17 23:13
 *
 */
public class VisuaizerImpl implements Visualizer {

    private VisualizationPanel visualizationPanel;

    public VisuaizerImpl() {
        final JFrame frame = new JFrame("Simple Double Buffer") {
            @Override
            public void processWindowEvent(java.awt.event.WindowEvent e) {
                super.processWindowEvent(e);
                if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
                    System.exit(-1);
                }
            }
        };

        /** Get window dimension */
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int displayWidth = gd.getDisplayMode().getWidth();
        int displayHeight = gd.getDisplayMode().getHeight();

        frame.setSize(new Dimension(displayWidth, displayHeight));
        frame.setBackground(Color.WHITE);
        visualizationPanel = new VisualizationPanel(Color.WHITE);
        frame.add(visualizationPanel);
        frame.setVisible(true);
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        List<Shape> shapes = new ArrayList<>();
        for (SimulationObject simulationObject : (List<SimulationObject>) arg) {
            Ellipse2D ellipse = new Ellipse2D.Double();
            ellipse.setFrame(simulationObject.getX().doubleValue(), simulationObject.getY().doubleValue(),
                    simulationObject.getRadius().doubleValue() * 2,
                    simulationObject.getRadius().doubleValue() * 2);
            shapes.add(ellipse);
        }
        visualizationPanel.draw(shapes);
    }

}
