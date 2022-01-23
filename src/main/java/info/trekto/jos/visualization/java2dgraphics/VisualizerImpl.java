package info.trekto.jos.visualization.java2dgraphics;

import info.trekto.jos.C;
import info.trekto.jos.visualization.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.BLUE;

/**
 * @author Trayan Momkov
 * 2016-окт-17 23:13
 */
public class VisualizerImpl implements Visualizer {
    private static final Logger logger = LoggerFactory.getLogger(VisualizerImpl.class);
    private VisualizationPanel visualizationPanel;
    private JFrame frame = null;
    List<ShapeWithColor> lastShapes;

    public VisualizerImpl() {
        if (C.prop.isRealTimeVisualization()) {
            frame = new VisualizationFrame(this, "Simple Double Buffer");
            frame.addKeyListener(new VisualizationKeyListener(this));

            /* Get window dimension */
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int displayWidth = gd.getDisplayMode().getWidth();
            int displayHeight = gd.getDisplayMode().getHeight();

            frame.setSize(new Dimension(displayWidth, displayHeight));
            frame.setBackground(Color.WHITE);
            visualizationPanel = new VisualizationPanel(Color.WHITE);
            frame.add(visualizationPanel);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    C.hasToStop = true;
                }
            });
            frame.setVisible(true);
        }
    }

    @Override
    public void closeWindow() {
        logger.info("Release graphic resources.");
        frame.dispose();
    }

    double convertCoordinatesForDisplayX(double x) {
        return x + visualizationPanel.getWidth() / 2.0;
    }

    double convertCoordinatesForDisplayY(double y) {
        return y + visualizationPanel.getHeight() / 2.0;
    }

    public void visualize() {
        lastShapes = createShapes();
        visualizationPanel.draw(lastShapes);
    }

    private List<ShapeWithColor> createShapes() {
        List<ShapeWithColor> shapes = new ArrayList<>();
        for (int i = 0; i < C.simulation.simulationLogicKernel.positionX.length; i++) {
            if (C.simulation.simulationLogicKernel.deleted[i]) {
                continue;
            }
            Ellipse2D ellipse = new Ellipse2D.Double();
            ellipse.setFrame(convertCoordinatesForDisplayX(C.simulation.simulationLogicKernel.positionX[i] - C.simulation.simulationLogicKernel.radius[i]),
                             convertCoordinatesForDisplayY(C.simulation.simulationLogicKernel.positionY[i] - C.simulation.simulationLogicKernel.radius[i]),
                             C.simulation.simulationLogicKernel.radius[i] * 2,
                             C.simulation.simulationLogicKernel.radius[i] * 2);

            Color color = new Color(C.simulation.simulationLogicKernel.color[i]);
            shapes.add(new ShapeWithColor(ellipse, color));
        }
        return shapes;
    }

    public void end() {
        Ellipse2D ellipse = new Ellipse2D.Double();
        ellipse.setFrame(convertCoordinatesForDisplayX(-100), convertCoordinatesForDisplayY(-10), 1, 1);
        ShapeWithColor text = new ShapeWithColor(ellipse, BLUE);
        text.setText(C.endText);
        if (lastShapes == null) {
            lastShapes = new ArrayList<>();
        }
        lastShapes.add(text);
        visualizationPanel.draw(lastShapes);
    }

    @Override
    public void zoomIn() {
        visualizationPanel.zoomIn();
    }

    @Override
    public void zoomOut() {
        visualizationPanel.zoomOut();
    }

    @Override
    public void translateLeft() {
        visualizationPanel.translateLeft();
    }

    @Override
    public void translateUp() {
        visualizationPanel.translateUp();
    }

    @Override
    public void translateRight() {
        visualizationPanel.translateRight();
    }

    @Override
    public void translateDown() {
        visualizationPanel.translateDown();
    }

    public VisualizationPanel getVisualizationPanel() {
        return visualizationPanel;
    }
}
