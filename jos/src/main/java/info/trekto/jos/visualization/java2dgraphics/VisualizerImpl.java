/**
 *
 */
package info.trekto.jos.visualization.java2dgraphics;

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
import info.trekto.jos.visualization.Visualizer;

/**
 * @author Trayan Momkov
 * @date 2016-окт-17 23:13
 *
 */
public class VisualizerImpl implements Visualizer {

    private VisualizationPanel visualizationPanel;
    private final JFrame frame;

    @Override
    public void closeWindow() {
        System.out.println("Release graphic resources.");
        frame.dispose();
    }

    public VisualizerImpl() {
        frame = new VisualizationFrame(this, "Simple Double Buffer");
        frame.addKeyListener(new VisualizationKeyListener(this));

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
        visualizationPanel.draw(createShapes((List<SimulationObject>) arg));
    }

    private List<Shape> createShapes(List<SimulationObject> simulationObjects) {
        List<Shape> shapes = new ArrayList<>();
        for (SimulationObject simulationObject : simulationObjects) {
            Ellipse2D ellipse = new Ellipse2D.Double();
            ellipse.setFrame(simulationObject.getX().doubleValue(), simulationObject.getY().doubleValue(),
                    simulationObject.getRadius().doubleValue() * 2,
                    simulationObject.getRadius().doubleValue() * 2);

            shapes.add(ellipse);
        }
        return shapes;
    }

    @Override
    public void zoomIn() {
        visualizationPanel.zoomIn();
    }

    @Override
    public void zoomOut() {
        visualizationPanel.zoomOut();
    }

    @Override
    public void translateLeft() {
        visualizationPanel.translateLeft();
    }

    @Override
    public void translateUp() {
        visualizationPanel.translateUp();
    }

    @Override
    public void translateRight() {
        visualizationPanel.translateRight();
    }

    @Override
    public void translateDown() {
        visualizationPanel.translateDown();
    }

}
