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
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.Controller.PROGRAM_NAME;
import static info.trekto.jos.util.Utils.invertColor;
import static info.trekto.jos.util.Utils.secondsToHumanReadable;
import static java.awt.Color.BLUE;
import static java.awt.Color.RED;

/**
 * @author Trayan Momkov
 * 2016-Oct-17 23:13
 */
public class VisualizerImpl implements Visualizer {
    private static final Logger logger = LoggerFactory.getLogger(VisualizerImpl.class);
    public static final int TRAIL_SIZE = 1;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final Color DEFAULT_COLOR = BLUE;
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000E0");
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
            frame.setBackground(properties.getBackgroundColor());
            visualizationPanel = new VisualizationPanel(properties.getBackgroundColor());
            visualizationPanel.setScale(properties.getScale());
            frame.add(visualizationPanel);
            frame.addWindowListener(new WindowAdapter() {
                @Override
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
    public void visualize(List<SimulationObject> objects, long currentIterationNumber) {
        Iteration iteration = new Iteration(currentIterationNumber, objects.size(), objects);
        latestShapes = createShapes(iteration);
        visualizationPanel.draw(latestShapes);
    }

    @Override
    public void visualize(long currentIterationNumber, int numberOfObjects, String[] ids, boolean[] deleted,
                          double[] positionX, double[] positionY, double[] radiuses, int[] colors) {
        latestShapes = createShapes(currentIterationNumber, numberOfObjects, ids, deleted, positionX, positionY, radiuses, colors);
        visualizationPanel.draw(latestShapes);
    }

    private List<ShapeWithColorAndText> createShapes(Iteration iteration) {
        String[] ids = iteration.getObjects().stream().map(ImmutableSimulationObject::getId).toArray(String[]::new);
        double[] positionX = iteration.getObjects().stream().mapToDouble(e -> e.getX().doubleValue()).toArray();
        double[] positionY = iteration.getObjects().stream().mapToDouble(e -> e.getY().doubleValue()).toArray();
        double[] radiuses = iteration.getObjects().stream().mapToDouble(e -> e.getRadius().doubleValue()).toArray();
        int[] colors = iteration.getObjects().stream().mapToInt(ImmutableSimulationObject::getColor).toArray();

        return createShapes(iteration.getCycle(), iteration.getNumberOfObjects(), ids, new boolean[iteration.getNumberOfObjects()],
                            positionX, positionY, radiuses, colors);
    }

    private List<ShapeWithColorAndText> createShapes(long currentIterationNumber, int numberOfObjects, String[] ids, boolean[] deleted,
                                                     double[] positionX, double[] positionY, double[] radiuses, int[] colors) {
        List<ShapeWithColorAndText> shapes = new ArrayList<>();
        if (C.isShowTrail() && trails.isEmpty()) {
            for (String id : ids) {
                trails.put(id, new ArrayDeque<>());
            }
        }

        for (int i = 0; i < deleted.length; i++) {
            if (deleted[i]) {
                if (C.isShowTrail()) {
                    trails.put(ids[i], null);   // For garbage collection.
                }
                continue;
            }
            Ellipse2D ellipse = new Ellipse2D.Double();
            double radius = radiuses[i];
            double x = positionX[i] - radiuses[i];
            double y = positionY[i] - radiuses[i];
            double w = radius * 2;
            double h = w;
            ellipse.setFrame(convertCoordinatesForDisplayX(x), convertCoordinatesForDisplayY(y), w, h);

            Color color = new Color(colors[i]);

            shapes.add(new ShapeWithColorAndText(ellipse, color));
            if (C.isShowIds()) {
                ShapeWithColorAndText text = new ShapeWithColorAndText(ellipse, RED);
                text.setText(ids[i]);
                shapes.add(text);
            }

            if (C.isShowTrail()) {
                Queue<ShapeWithColorAndText> trail = trails.get(ids[i]);
                if (trail.size() >= 5) {
                    shapes.addAll(trail);
                }
                Ellipse2D trailEllipse = new Ellipse2D.Double();
                double trailEllipseRadius = TRAIL_SIZE / 2.0 / properties.getScale();
                double trailEllipseX = convertCoordinatesForDisplayX(x + radius - trailEllipseRadius / 2);
                double trailEllipseY = convertCoordinatesForDisplayY(y + radius - trailEllipseRadius / 2);
                trailEllipse.setFrame(trailEllipseX, trailEllipseY, trailEllipseRadius * TRAIL_SIZE, trailEllipseRadius * TRAIL_SIZE);
                ShapeWithColorAndText newTrailElement = new ShapeWithColorAndText(trailEllipse, color);
                if (trail.size() >= C.getTrailSize()) {
                    trail.poll();
                }
                trail.offer(newTrailElement);
            }
        }

        if (C.getShowTimeAndIteration()) {
            shapes.addAll(createInfo(currentIterationNumber, properties.getSecondsPerIteration(), numberOfObjects
                                     /*, C.getSimulation().calculateTotalMass(), C.getSimulation().calculateTotalMomentum()*/));
        }
        return shapes;
    }

    private Collection<? extends ShapeWithColorAndText> createInfo(long iteration, Number secondsPerIteration, int objectsCount
            /*, double totalMass, double totalMomentum*/) {
        int verPos = 20;
        final int verStep = 20;
        
        List<ShapeWithColorAndText> info = new ArrayList<>();
        info.add(newMeta(verPos, "Iteration: " + iteration));
        info.add(newMeta(verPos += verStep, "Time: " + secondsToHumanReadable(secondsPerIteration.multiply(New.num(iteration)).doubleValue())));
        info.add(newMeta(verPos += verStep, String.format("Scale: %1.2E", visualizationPanel.getScale())));
        info.add(newMeta(verPos += verStep, "Objects: " + objectsCount));

        /* For debugging purposes */
//        info.add(new ShapeWithColorAndText(new Rectangle2D.Double(0, 140, 100, 40), BLUE,
//                                           String.format("Mass: %s kg", DECIMAL_FORMAT.format(totalMass))));
//        info.add(new ShapeWithColorAndText(new Rectangle2D.Double(0, 180, 100, 40), BLUE,
//                                           String.format("Momentum: %s kg⋅m/s", DECIMAL_FORMAT.format(totalMomentum))));

        return info;
    }
    
    private ShapeWithColorAndText newMeta(double y, String text) {
        return new ShapeWithColorAndText(new Rectangle2D.Double(2, y, 100, 20), invertedBackground(), text, true);
    }

    public Color invertedBackground() {
        return visualizationPanel.getBackground().equals(DEFAULT_BACKGROUND_COLOR) ? DEFAULT_COLOR : invertColor(visualizationPanel.getBackground());
    }

    public void end() {
        Ellipse2D ellipse = new Ellipse2D.Double();
        ellipse.setFrame(convertCoordinatesForDisplayX(-100), convertCoordinatesForDisplayY(-10), 1, 1);
        ShapeWithColorAndText text = new ShapeWithColorAndText(ellipse, invertedBackground(), C.getEndText(), true);
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

    public Map<String, Queue<ShapeWithColorAndText>> getTrails() {
        return trails;
    }
}
