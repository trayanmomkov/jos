package info.trekto.jos.visualization;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.visualization.java2dgraphics.VisualizationPanel;

import java.util.List;

/**
 * @author Trayan Momkov
 * 2016-Oct-17 23:11
 *
 */
public interface Visualizer {
    void closeWindow();

    void zoomIn();

    void zoomOut();

    void translateLeft();

    void translateUp();

    void translateRight();

    void translateDown();
    
    void visualize();

    void end();

    void visualize(List<SimulationObject> objects);

    VisualizationPanel getVisualizationPanel();
}
