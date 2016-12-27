/**
 *
 */
package visualization.java2dgraphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import visualization.Visualizer;

/**
 * @author Trayan Momkov
 * @date 2016-дек-27 19:16
 *
 */
public class VisualizationKeyListener implements KeyListener {

    Visualizer visualizer;

    public VisualizationKeyListener(Visualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {

            case KeyEvent.VK_ESCAPE:
                visualizer.closeWindow();
                break;

            case KeyEvent.VK_EQUALS: // Plus sign '+'
                if (e.isShiftDown()) {
                    visualizer.zoomIn();
                }
                break;

            case KeyEvent.VK_MINUS:
                if (e.isShiftDown()) {
                    visualizer.zoomOut();
                }
                break;

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                visualizer.translateLeft();
                break;

            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
                visualizer.translateUp();
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                visualizer.translateRight();
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
                visualizer.translateDown();
                break;

            default:
                break;
        }
    }
}
