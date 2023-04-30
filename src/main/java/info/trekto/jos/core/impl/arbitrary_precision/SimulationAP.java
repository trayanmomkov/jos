package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.CpuSimulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.impl.Data.countObjects;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_MILLISECOND;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.deepCopy;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.info;
import static info.trekto.jos.util.Utils.nanoToHumanReadable;
import static info.trekto.jos.util.Utils.showRemainingTime;

public class SimulationAP implements CpuSimulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationAP.class);
    
    public static final int PAUSE_SLEEP_MILLISECONDS = 100;
    public static final int SHOW_REMAINING_INTERVAL_SECONDS = 2;

    private MoveObjectsLogicAP moveObjectsLogic;
    private ProcessCollisionsLogicAP processCollisionsLogic;
    private final Number[] zeroArray;
    private DataAP data;

    private SimulationProperties properties;
    private long iterationCounter;

    public SimulationAP(SimulationProperties properties) {
        this.properties = properties;
        final int n = properties.getNumberOfObjects();
        int screenWidth = 0;
        int screenHeight = 0;
        if (properties.isBounceFromScreenBorders()) {
            screenWidth = C.getVisualizer().getVisualizationPanel().getWidth();
            screenHeight = C.getVisualizer().getVisualizationPanel().getHeight();
        }
        zeroArray = new Number[n];
        data = new DataAP(n);
        
        moveObjectsLogic = new MoveObjectsLogicAP(data, properties.getSecondsPerIteration(), screenWidth, screenHeight);
        processCollisionsLogic = new ProcessCollisionsLogicAP(data, properties.isMergeOnCollision(), properties.getCoefficientOfRestitution());
    }

    @Override
    public void doIteration(boolean saveCurrentIterationToFile, long iterationCounter) {
        data.copyToReadOnly(properties.isMergeOnCollision());
        moveObjectsLogic.run(); /* Execute in parallel */
//        moveObjectsLogic.runOnSingleThread();

        data.copyToReadOnly(properties.isMergeOnCollision());
        processCollisionsLogic.run(); /* Collisions - Execute in parallel */
//        processCollisionsLogic.runOnSingleThread();

        if (properties.isSaveToFile() && saveCurrentIterationToFile) {
            C.getReaderWriter().appendObjectsToFile(properties, iterationCounter, data.positionX, data.positionY, zeroArray, data.velocityX,
                                                    data.velocityY, zeroArray, data.mass, data.radius, data.id, data.color, data.deleted,
                                                    data.accelerationX, data.accelerationY, zeroArray);
        }
    }

    public void initArrays(List<SimulationObject> initialObjects) {
//        Arrays.fill(moveObjectsLogic.deleted, true);
        for (int i = 0; i < initialObjects.size(); i++) {
            SimulationObject o = initialObjects.get(i);
            data.positionX[i] = o.getX();
            data.positionY[i] = o.getY();
            data.velocityX[i] = o.getVelocity().getX();
            data.velocityY[i] = o.getVelocity().getY();
            data.accelerationX[i] = o.getAcceleration().getX();
            data.accelerationY[i] = o.getAcceleration().getY();
            data.mass[i] = o.getMass();
            data.radius[i] = o.getRadius();
            data.id[i] = o.getId();
            data.color[i] = o.getColor();
            data.deleted[i] = false;
        }
        
        deepCopy(data.mass, data.readOnlyMass);
        deepCopy(data.deleted, data.readOnlyDeleted);
        deepCopy(data.color, data.readOnlyColor);
        deepCopy(data.positionX, data.readOnlyPositionX);
        deepCopy(data.positionY, data.readOnlyPositionY);
        deepCopy(data.radius, data.readOnlyRadius);
        deepCopy(data.velocityX, data.readOnlyVelocityX);
        deepCopy(data.velocityY, data.readOnlyVelocityY);
    }

    @Override
    public void startSimulation() throws SimulationException {
        init(true);

        info(logger, "Start simulation...");
        C.setEndText("END.");
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long previousVisualizationTime = startTime;
        long endTime;

        C.setRunning(true);
        C.setHasToStop(false);
        try {
            for (long i = 0; properties.isInfiniteSimulation() || i < properties.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop()) {
                        doStop();
                        break;
                    }
                    while (C.isPaused()) {
                        Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                    }

                    iterationCounter = i + 1;
                    int numberOfObjects = countObjects(data);

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                        showRemainingTime(i, startTime, properties.getNumberOfIterations(), numberOfObjects);
                        previousTime = System.nanoTime();
                    }

                    boolean visualize = false;
                    if (properties.isRealTimeVisualization()) {
                        if (properties.getPlayingSpeed() < 0) {
                            /* Slow down */
                            Thread.sleep(-properties.getPlayingSpeed());
                            visualize = true;
                        } else if ((System.nanoTime() - previousVisualizationTime) / NANOSECONDS_IN_ONE_MILLISECOND >= properties.getPlayingSpeed()) {
                            visualize = true;
                        }
                    }

                    if (visualize) {
                        C.getVisualizer().visualize(iterationCounter, numberOfObjects, data.id, data.deleted,
                                                    Arrays.stream(data.positionX).mapToDouble(Number::doubleValue).toArray(),
                                                    Arrays.stream(data.positionY).mapToDouble(Number::doubleValue).toArray(),
                                                    Arrays.stream(data.radius).mapToDouble(Number::doubleValue).toArray(),
                                                    data.color);
                        previousVisualizationTime = System.nanoTime();
                    }

                    doIteration(i % properties.getSaveEveryNthIteration() == 0, iterationCounter);
                } catch (InterruptedException e) {
                    error(logger, "Concurrency failure. One of the threads interrupted in cycle " + i, e);
                    return;
                }
            }

            if (properties.isRealTimeVisualization()) {
                C.getVisualizer().end();
            }
            endTime = System.nanoTime();
        } finally {
            C.setRunning(false);
            if (properties.isSaveToFile()) {
                C.getReaderWriter().endFile();
            }
        }

        info(logger, "End of simulation. Time: " + nanoToHumanReadable(endTime - startTime));
    }

    private List<SimulationObject> convertToSimulationObjects() {
        List<SimulationObject> objects = new ArrayList<>();

        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                SimulationObject simo = new SimulationObjectImpl();
                simo.setId(data.id[i]);

                simo.setX(data.positionX[i]);
                simo.setY(data.positionY[i]);
                simo.setZ(ZERO);

                simo.setMass(data.mass[i]);

                simo.setVelocity(new TripleNumber(data.velocityX[i], data.velocityY[i], ZERO));
                simo.setAcceleration(new TripleNumber(data.accelerationX[i], data.accelerationY[i], ZERO));

                simo.setRadius(data.radius[i]);
                simo.setColor(data.color[i]);

                objects.add(simo);
            }
        }

        return objects;
    }

    public void init(boolean printInfo) throws SimulationException {
        if (printInfo) {
            info(logger, "Initialize simulation...");
        }
        initArrays(properties.getInitialObjects());
        
        if (duplicateIdExists(data.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(data.positionX, data.positionY, data.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        if (printInfo) {
            info(logger, "Done.\n");
            Utils.printConfiguration(this);
        }
    }

    private boolean duplicateIdExists(String[] id) {
        Set<String> ids = new HashSet<>();
        for (String objectId : id) {
            if (!ids.add(objectId)) {
                return true;
            }
        }
        return false;
    }

    public boolean collisionExists(Number[] positionX, Number[] positionY, Number[] radius) {
        for (int i = 0; i < positionX.length; i++) {
            for (int j = 0; j < positionX.length; j++) {
                if (i == j) {
                    continue;
                }
                // distance between centres
                Number distance = moveObjectsLogic.calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);

                if (distance.compareTo(radius[i].add(radius[j])) < 0) {
                    info(logger, String.format("Collision between object A(x:%f, y:%f, r:%f) and B(x:%f, y:%f, r:%f)",
                                               positionX[i], positionY[i], radius[i], positionX[j], positionY[j], radius[j]));
                    return true;
                }
            }
        }
        return false;
    }

    protected void doStop() {
        C.setHasToStop(false);
        if (properties.isSaveToFile()) {
            C.getReaderWriter().endFile();
        }
        if (C.getVisualizer() != null) {
            C.setEndText("Stopped!");
            C.getVisualizer().closeWindow();
        }
    }

    @Override
    public SimulationProperties getProperties() {
        return properties;
    }

    @Override
    public DataAP getData() {
        return data;
    }

    @Override
    public void playSimulation(String inputFile) {
        try {
            // Only reset reader pointer. Do not change properties! We want to have the latest changes from the GUI.
            C.getReaderWriter().readPropertiesForPlaying(inputFile);
        } catch (IOException e) {
            error(logger, "Cannot reset input file for playing.", e);
        }
        C.setVisualizer(C.createVisualizer(properties));
        long previousTime = System.nanoTime();
        long previousVisualizationTime = previousTime;
        C.setRunning(true);
        C.setEndText("END.");
        try {
            while (C.getReaderWriter().hasMoreIterations()) {
                if (C.hasToStop()) {
                    doStop();
                    break;
                }
                while (C.isPaused()) {
                    Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                }
                Iteration iteration = C.getReaderWriter().readNextIteration();
                if (iteration == null) {
                    break;
                }

                if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                    previousTime = System.nanoTime();
                    info(logger, "Cycle: " + iteration.getCycle() + ", number of objects: " + iteration.getNumberOfObjects());
                }

                if (properties.getPlayingSpeed() < 0) {
                    /* Slow down */
                    Thread.sleep(-properties.getPlayingSpeed());
                    C.getVisualizer().visualize(iteration);
                    previousVisualizationTime = System.nanoTime();
                } else if ((System.nanoTime() - previousVisualizationTime) / NANOSECONDS_IN_ONE_MILLISECOND >= properties.getPlayingSpeed()) {
                    C.getVisualizer().visualize(iteration);
                    previousVisualizationTime = System.nanoTime();
                }
            }
            info(logger, "End.");
            C.getVisualizer().end();
        } catch (IOException e) {
            error(logger, "Error while reading simulation object.", e);
        } catch (InterruptedException e) {
            error(logger, "Thread interrupted.", e);
        } finally {
            C.setRunning(false);
        }
    }

    @Override
    public void setProperties(SimulationProperties properties) {
        this.properties = properties;
    }

    public void setDataAndInitializeLogic(DataAP data) {
        this.data = data;
        moveObjectsLogic = new MoveObjectsLogicAP(data, properties.getSecondsPerIteration(), moveObjectsLogic.getScreenWidth(),
                                                  moveObjectsLogic.getScreenHeight());
        processCollisionsLogic = new ProcessCollisionsLogicAP(data, properties.isMergeOnCollision(), properties.getCoefficientOfRestitution());
    }

}
