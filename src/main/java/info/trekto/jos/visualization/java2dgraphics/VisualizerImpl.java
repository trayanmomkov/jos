package info.trekto.jos.visualization.java2dgraphics;

import info.trekto.jos.C;
import info.trekto.jos.gui.MainForm;
import info.trekto.jos.model.ImmutableSimulationObject;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.visualization.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;

import static info.trekto.jos.util.Utils.info;
import static java.awt.Color.BLUE;
import static java.awt.Color.RED;

/**
 * @author Trayan Momkov
 * 2016-Oct-17 23:13
 */
public class VisualizerImpl implements Visualizer {
    private static final Logger logger = LoggerFactory.getLogger(VisualizerImpl.class);
    public static final int TRAIL_SIZE = 2;
    private VisualizationPanel visualizationPanel;
    private JFrame frame = null;
    List<ShapeWithColorAndText> latestShapes;
    Map<String, Queue<ShapeWithColorAndText>> trails;

    public VisualizerImpl() {
        trails = new HashMap<>();
        if (C.prop.isRealTimeVisualization()) {
            frame = new VisualizationFrame(this, "Simulation");
            if (MainForm.icon != null) {
                frame.setIconImage(MainForm.icon);
            }
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
        info(logger, "Release graphic resources.");
        frame.dispose();
        C.mainForm.onVisualizationWindowClosed();
    }

    double convertCoordinatesForDisplayX(double x) {
        return x + visualizationPanel.getWidth() / 2.0;
    }

    double convertCoordinatesForDisplayY(double y) {
        return y + visualizationPanel.getHeight() / 2.0;
    }

    public void visualize() {
        latestShapes = createShapes();
        visualizationPanel.draw(latestShapes);
    }

    @Override
    public void visualize(List<SimulationObject> objects) {
        latestShapes = createShapes(objects);
        visualizationPanel.draw(latestShapes);
    }

    private List<ShapeWithColorAndText> createShapes() {
        List<ShapeWithColorAndText> shapes = new ArrayList<>();
        if (C.mainForm.isShowTrail() && trails.isEmpty()) {
            for (int j = 0; j < C.simulation.simulationLogicKernel.id.length; j++) {
                trails.put(C.simulation.simulationLogicKernel.id[j], new ArrayDeque<>());
            }
        }
        for (int i = 0; i < C.simulation.simulationLogicKernel.id.length; i++) {
            if (C.simulation.simulationLogicKernel.deleted[i]) {
                if (C.mainForm.isShowTrail()) {
                    trails.put(C.simulation.simulationLogicKernel.id[i], null);
                }
                continue;
            }
            Ellipse2D ellipse = new Ellipse2D.Double();
            double x = C.simulation.simulationLogicKernel.positionX[i] - C.simulation.simulationLogicKernel.radius[i];
            double y = C.simulation.simulationLogicKernel.positionY[i] - C.simulation.simulationLogicKernel.radius[i];
            double radius = C.simulation.simulationLogicKernel.radius[i];
            double w = radius * 2;
            double h = w;
            ellipse.setFrame(convertCoordinatesForDisplayX(x), convertCoordinatesForDisplayY(y), w, h);

            Color color = new Color(C.simulation.simulationLogicKernel.color[i]);

            shapes.add(new ShapeWithColorAndText(ellipse, color));
            if (C.mainForm.isShowIds()) {
                ShapeWithColorAndText text = new ShapeWithColorAndText(ellipse, RED);
                text.setText(C.simulation.simulationLogicKernel.id[i]);
                shapes.add(text);
            }

            if (C.mainForm.isShowTrail()) {
                Queue<ShapeWithColorAndText> trail = trails.get(C.simulation.simulationLogicKernel.id[i]);
                if (trail.size() >= 5) {
                    shapes.addAll(trail);
                }
                Ellipse2D trailEllipse = new Ellipse2D.Double();
                double trailEllipseRadius = TRAIL_SIZE / 2.0;
                double trailEllipseX = convertCoordinatesForDisplayX(x + radius - trailEllipseRadius / 2);
                double trailEllipseY = convertCoordinatesForDisplayY(y + radius - trailEllipseRadius / 2);
                trailEllipse.setFrame(trailEllipseX, trailEllipseY, trailEllipseRadius * TRAIL_SIZE, trailEllipseRadius * TRAIL_SIZE);
                ShapeWithColorAndText newTrailElement = new ShapeWithColorAndText(trailEllipse, color);
                if (trail.size() >= C.mainForm.getTrailSize()) {
                    trail.poll();
                }
                trail.offer(newTrailElement);
            }
        }
        return shapes;
    }

    private List<ShapeWithColorAndText> createShapes(List<SimulationObject> objects) {
        List<ShapeWithColorAndText> shapes = new ArrayList<>();
        if (C.mainForm.isShowTrail()) {
            if (trails.isEmpty()) {
                for (SimulationObject object : objects) {
                    trails.put(object.getId(), new ArrayDeque<>());
                }
            } else {
                Set<String> ids = objects.stream().map(ImmutableSimulationObject::getId).collect(Collectors.toSet());
                trails.entrySet().removeIf(e -> !ids.contains(e.getKey()));
            }
        }
        for (SimulationObject object : objects) {
            Ellipse2D ellipse = new Ellipse2D.Double();
            double radius = object.getRadius();
            double x = object.getX() - radius;
            double y = object.getY() - radius;
            double w = radius * 2;
            double h = w;
            ellipse.setFrame(convertCoordinatesForDisplayX(x), convertCoordinatesForDisplayY(y), w, h);

            Color color = new Color(object.getColor());

            shapes.add(new ShapeWithColorAndText(ellipse, color));
            if (C.mainForm.isShowIds()) {
                ShapeWithColorAndText text = new ShapeWithColorAndText(ellipse, RED);
                text.setText(object.getId());
                shapes.add(text);
            }

            if (C.mainForm.isShowTrail()) {
                Queue<ShapeWithColorAndText> trail = trails.get(object.getId());
                if (trail.size() >= 5) {
                    shapes.addAll(trail);
                }
                Ellipse2D trailEllipse = new Ellipse2D.Double();
                double trailEllipseRadius = TRAIL_SIZE / 2.0;
                double trailEllipseX = convertCoordinatesForDisplayX(x + radius - trailEllipseRadius / 2);
                double trailEllipseY = convertCoordinatesForDisplayY(y + radius - trailEllipseRadius / 2);
                trailEllipse.setFrame(trailEllipseX, trailEllipseY, trailEllipseRadius * TRAIL_SIZE, trailEllipseRadius * TRAIL_SIZE);
                ShapeWithColorAndText newTrailElement = new ShapeWithColorAndText(trailEllipse, color);
                if (trail.size() >= C.mainForm.getTrailSize()) {
                    trail.poll();
                }
                trail.offer(newTrailElement);
            }
        }
        return shapes;
    }

    public void end() {
        Ellipse2D ellipse = new Ellipse2D.Double();
        ellipse.setFrame(convertCoordinatesForDisplayX(-100), convertCoordinatesForDisplayY(-10), 1, 1);
        ShapeWithColorAndText text = new ShapeWithColorAndText(ellipse, BLUE);
        text.setText(C.endText);
        if (latestShapes == null) {
            latestShapes = new ArrayList<>();
        }
        latestShapes.add(text);
        visualizationPanel.draw(latestShapes);
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
    public VisualizationPanel getVisualizationPanel() {
        return visualizationPanel;
    }
}
