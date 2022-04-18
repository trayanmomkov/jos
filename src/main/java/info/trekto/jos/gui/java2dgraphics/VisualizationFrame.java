package info.trekto.jos.gui.java2dgraphics;

import info.trekto.jos.gui.Visualizer;

import javax.swing.*;
import java.awt.event.WindowEvent;

/**
 * @author Trayan Momkov
 * 2016-Dec-27 19:12
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
