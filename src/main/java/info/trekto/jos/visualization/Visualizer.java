package info.trekto.jos.visualization;

import info.trekto.jos.core.impl.Iteration;

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

    void visualize(Iteration iteration);
}
