package info.trekto.jos.gui.java2dgraphics;

import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.gui.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.Controller.PROGRAM_NAME;
import static info.trekto.jos.core.numbers.New.TWO;
import static info.trekto.jos.util.Utils.secondsToHumanReadable;
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
    SimulationProperties properties;

    public VisualizerImpl(SimulationProperties properties) {
        this.properties = properties;
        trails = new HashMap<>();
        if (properties.isRealTimeVisualization()) {
            frame = new VisualizationFrame(this, PROGRAM_NAME);
            if (C.getIcon() != null) {
                frame.setIconImage(C.getIcon());
            }
            frame.addKeyListener(new VisualizationKeyListener(this));
            frame.addMouseListener(new VisualizationMouseListener(this));

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
                    C.setHasToStop(true);
                }
            });
            frame.setVisible(true);
        }
    }

    @Override
    public void closeWindow() {
        frame.dispose();
        C.onVisualizationWindowClosed();
    }

    double convertCoordinatesForDisplayX(double x) {
        return x + visualizationPanel.getWidth() / 2.0;
    }

    double convertCoordinatesForDisplayY(double y) {
        return y + visualizationPanel.getHeight() / 2.0;
    }

    @Override
    public void visualize(Iteration iteration) {
        latestShapes = createShapes(iteration);
        visualizationPanel.draw(latestShapes);
    }

    @Override
    public void visualize(List<SimulationObject> objects) {
        Iteration iteration = new Iteration(C.getSimulation().getCurrentIterationNumber(), C.getSimulation().getObjects().size(), objects);
        latestShapes = createShapes(iteration);
        visualizationPanel.draw(latestShapes);
    }

    private List<ShapeWithColorAndText> createShapes(Iteration iteration) {
        List<SimulationObject> objects = iteration.getObjects();
        List<ShapeWithColorAndText> shapes = new ArrayList<>();
        if (C.isShowTrail()) {
            if (C.isShowTrail() && trails.isEmpty()) {
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
            Number radius = object.getRadius();
            double x = object.getX().subtract(radius).doubleValue();
            double y = object.getY().subtract(radius).doubleValue();
            double w = radius.multiply(TWO).doubleValue();
            double h = w;
            ellipse.setFrame(convertCoordinatesForDisplayX(x), convertCoordinatesForDisplayY(y), w, h);

            Color color = new Color(object.getColor());

            shapes.add(new ShapeWithColorAndText(ellipse, color));
            if (C.isShowIds()) {
                ShapeWithColorAndText text = new ShapeWithColorAndText(ellipse, RED);
                text.setText(object.getId());
                shapes.add(text);
            }

            if (C.isShowTrail()) {
                Queue<ShapeWithColorAndText> trail = trails.get(object.getId());
                if (trail.size() >= 5) {
                    shapes.addAll(trail);
                }
                Ellipse2D trailEllipse = new Ellipse2D.Double();
                double trailEllipseRadius = TRAIL_SIZE / 2.0;
                double trailEllipseX = convertCoordinatesForDisplayX(x + radius.doubleValue() - trailEllipseRadius / 2);
                double trailEllipseY = convertCoordinatesForDisplayY(y + radius.doubleValue() - trailEllipseRadius / 2);
                trailEllipse.setFrame(trailEllipseX, trailEllipseY, trailEllipseRadius * TRAIL_SIZE, trailEllipseRadius * TRAIL_SIZE);
                ShapeWithColorAndText newTrailElement = new ShapeWithColorAndText(trailEllipse, color);
                if (trail.size() >= C.getTrailSize()) {
                    trail.poll();
                }
                trail.offer(newTrailElement);
            }
        }

        if (C.getShowTimeAndIteration()) {
            shapes.addAll(createInfo(iteration.getCycle(), properties.getSecondsPerIteration(), iteration.getNumberOfObjects()));
        }
        return shapes;
    }

    private Collection<? extends ShapeWithColorAndText> createInfo(long iteration, Number secondsPerIteration, int objectsCount) {
        ShapeWithColorAndText info1 = new ShapeWithColorAndText(
                new Rectangle2D.Double(0, 20, 100, 40), BLUE, "Iteration: " + iteration);

        ShapeWithColorAndText info2 = new ShapeWithColorAndText(
                new Rectangle2D.Double(0, 60, 100, 40), BLUE, "Time: "
                + secondsToHumanReadable(secondsPerIteration.multiply(New.num(iteration)).doubleValue()));

        ShapeWithColorAndText info3 = new ShapeWithColorAndText(
                new Rectangle2D.Double(0, 100, 100, 40), BLUE, "Objects: " + objectsCount);

        return Arrays.asList(info1, info2, info3);
    }

    public void end() {
        Ellipse2D ellipse = new Ellipse2D.Double();
        ellipse.setFrame(convertCoordinatesForDisplayX(-100), convertCoordinatesForDisplayY(-10), 1, 1);
        ShapeWithColorAndText text = new ShapeWithColorAndText(ellipse, BLUE);
        text.setText(C.getEndText());
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
