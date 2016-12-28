/**
 *
 */
package visualization;

import java.util.Observer;

/**
 * @author Trayan Momkov
 * @date 2016-окт-17 23:11
 *
 */
public interface Visualizer extends Observer {
    void closeWindow();

    void zoomIn();

    void zoomOut();

    void translateLeft();

    void translateUp();

    void translateRight();

    void translateDown();
}
