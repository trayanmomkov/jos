package info.trekto.jos.visualization;

import info.trekto.jos.model.SimulationObject;

import java.util.List;
import java.util.concurrent.Flow;

/**
 * @author Trayan Momkov
 * 2016-Oct-17 23:11
 *
 */
public interface Visualizer extends Flow.Subscriber<List<SimulationObject>> {
    void closeWindow();

    void zoomIn();

    void zoomOut();

    void translateLeft();

    void translateUp();

    void translateRight();

    void translateDown();
}
