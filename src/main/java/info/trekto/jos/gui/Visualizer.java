package info.trekto.jos.gui;

import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.gui.java2dgraphics.ShapeWithColorAndText;
import info.trekto.jos.gui.java2dgraphics.VisualizationPanel;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author Trayan Momkov
 * 2016-Oct-17 23:11
 */
public interface Visualizer {
    void closeWindow();

    void zoomIn();

    void zoomOut();

    void translateLeft();

    void translateUp();

    void translateRight();

    void translateDown();

    void visualize(long currentIterationNumber, int numberOfObjects, String[] ids, boolean[] deleted,
                   double[] positionX, double[] positionY, double[] radiuses, int[] colors);

    void end();

    void visualize(List<SimulationObject> objects, long currentIterationNumber);

    void visualize(Iteration iteration);

    VisualizationPanel getVisualizationPanel();
    
    Map<String, Queue<ShapeWithColorAndText>> getTrails();
}
