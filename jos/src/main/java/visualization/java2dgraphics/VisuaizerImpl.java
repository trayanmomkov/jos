/**
 *
 */
package visualization.java2dgraphics;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import info.trekto.jos.model.SimulationObject;
import visualization.Visualizer;

/**
 * @author Trayan Momkov
 * @date 2016-окт-17 23:13
 *
 */
public class VisuaizerImpl implements Visualizer {

    private VisualizationFrame visualizationFrame;

    public VisuaizerImpl() {
        visualizationFrame = new VisualizationFrame();
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
        visualizationFrame.draw(shapes);
    }

}
