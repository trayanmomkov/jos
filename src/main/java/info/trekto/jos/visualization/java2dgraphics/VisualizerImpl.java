package info.trekto.jos.visualization.java2dgraphics;

import info.trekto.jos.C;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.visualization.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

/**
 * @author Trayan Momkov
 * 2016-окт-17 23:13
 *
 */
public class VisualizerImpl implements Visualizer {
    private static final Logger logger = LoggerFactory.getLogger(VisualizerImpl.class);
    private VisualizationPanel visualizationPanel;
    private JFrame frame = null;

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
            frame.setVisible(true);
        }
    }

    @Override
    public void closeWindow() {
        System.out.println("Release graphic resources.");
        frame.dispose();
    }
    
    double convertCoordinatesForDisplayX(double x) {
        return x + visualizationPanel.getWidth() / 2.0;
    }
    
    double convertCoordinatesForDisplayY(double y) {
        return y + visualizationPanel.getHeight() / 2.0;
    }

    @Override
    public void onNext(List<SimulationObject> simulationObject) {
        visualizationPanel.draw(createShapes(simulationObject));
    }

    private List<ShapeWithColor> createShapes(List<SimulationObject> simulationObjects) {
        List<ShapeWithColor> shapes = new ArrayList<>();
        for (SimulationObject simulationObject : simulationObjects) {
            Ellipse2D ellipse = new Ellipse2D.Double();
            ellipse.setFrame(convertCoordinatesForDisplayX(simulationObject.getX().subtract(simulationObject.getRadius()).doubleValue()),
                             convertCoordinatesForDisplayY(simulationObject.getY().subtract(simulationObject.getRadius()).doubleValue()),
                             simulationObject.getRadius().doubleValue() * 2,
                             simulationObject.getRadius().doubleValue() * 2);

            Color color = new Color(simulationObject.getColor().getR(), simulationObject.getColor().getG(), simulationObject.getColor().getB());
            shapes.add(new ShapeWithColor(ellipse, color));
        }
        return shapes;
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

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("onError called.", throwable);
    }

    @Override
    public void onComplete() {

    }

    public VisualizationPanel getVisualizationPanel() {
        return visualizationPanel;
    }
}
