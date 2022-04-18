package info.trekto.jos.gui.java2dgraphics;

import info.trekto.jos.core.C;
import info.trekto.jos.gui.Visualizer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Trayan Momkov
 * 2022-Mar-22
 */
public class VisualizationMouseListener implements MouseListener {

    Visualizer visualizer;

    public VisualizationMouseListener(Visualizer visualizer) {
        this.visualizer = visualizer;
    }


    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        C.simulation.switchPause();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
