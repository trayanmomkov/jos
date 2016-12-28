/**
 *
 */
package info.trekto.jos.visualization.java2dgraphics;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import info.trekto.jos.visualization.Visualizer;

/**
 * @author Trayan Momkov
 * @date 2016-дек-27 19:12
 *
 */
public class VisualizationFrame extends JFrame {

    Visualizer visualizer;

    public VisualizationFrame(Visualizer visualizer, String frameTitle) {
        super(frameTitle);
        this.visualizer = visualizer;
    }

    @Override
    public void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            visualizer.closeWindow();
        }
    }
}
