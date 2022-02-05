package info.trekto.jos.core.impl;

import com.aparapi.Range;
import info.trekto.jos.C;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.util.Utils;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.formulas.ScientificConstants.NANOSECONDS_IN_ONE_MILLISECOND;
import static info.trekto.jos.formulas.ScientificConstants.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.*;

/**
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationImpl {
    private static final Logger logger = LoggerFactory.getLogger(SimulationImpl.class);
    public static double RATIO_FOUR_THREE = 4 / 3.0;
    public static double BILLION = 1000000000;
    public static final int SHOW_REMAINING_INTERVAL_SECONDS = 2;
    public boolean running = false;

    private long iterationCounter;
    public SimulationLogicImpl simulationLogicKernel;
    private final Range simulationLogicRange;
    public CollisionCheck collisionCheckKernel;
    private final Range collisionCheckRange;

    public SimulationImpl(int numberOfObjects, double secondsPerIteration) {
        simulationLogicKernel = new SimulationLogicImpl(numberOfObjects, secondsPerIteration);
        simulationLogicRange = Range.create(numberOfObjects);
        simulationLogicKernel.setExecutionMode(GPU);

        collisionCheckKernel = new CollisionCheck(
                simulationLogicKernel, numberOfObjects,
                simulationLogicKernel.positionX,
                simulationLogicKernel.positionY,
                simulationLogicKernel.radius,
                simulationLogicKernel.deleted);
        collisionCheckRange = Range.create(numberOfObjects);
        collisionCheckKernel.setExecutionMode(GPU);
    }

    public static void initArrays(List<SimulationObject> initialObjects) {
        Arrays.fill(C.simulation.simulationLogicKernel.deleted, true);
        for (int i = 0; i < initialObjects.size(); i++) {
            SimulationObject o = initialObjects.get(i);
            C.simulation.simulationLogicKernel.positionX[i] = o.getX();
            C.simulation.simulationLogicKernel.positionY[i] = o.getY();
            C.simulation.simulationLogicKernel.speedX[i] = o.getSpeedX();
            C.simulation.simulationLogicKernel.speedY[i] = o.getSpeedY();
            C.simulation.simulationLogicKernel.mass[i] = o.getMass();
            C.simulation.simulationLogicKernel.radius[i] = o.getRadius();
            C.simulation.simulationLogicKernel.id[i] = o.getId();
            C.simulation.simulationLogicKernel.color[i] = o.getColor();
            C.simulation.simulationLogicKernel.deleted[i] = false;
        }
    }

    public static void init(String inputFile) {
        C.io = new JsonReaderWriter();
        try {
            C.prop = C.io.readProperties(inputFile);
        } catch (FileNotFoundException e) {
            error(logger, "Cannot read properties file.", e);
        }
    }

    public static void init(SimulationProperties prop) {
        C.io = new JsonReaderWriter();
        C.prop = prop;
        C.simulation = new SimulationImpl(C.prop.getNumberOfObjects(), C.prop.getSecondsPerIteration());
    }

    public static void initForPlaying(String inputFile) throws IOException {
        C.io = new JsonReaderWriter();
        try {
            C.prop = C.io.readPropertiesForPlaying(inputFile);
            C.prop.setRealTimeVisualization(true);
        } catch (FileNotFoundException e) {
            error(logger, "Cannot read properties file.", e);
        }
    }

    public void playSimulation(String inputFile) {
        try {
            // Only reset reader pointer. Do not change properties! We want to have the latest changes from the GUI.
            C.io.readPropertiesForPlaying(inputFile);
            C.simulation = this;
        } catch (IOException e) {
            error(logger, "Cannot reset input file for playing.", e);
        }
        C.visualizer = new VisualizerImpl();
        long previousTime = System.nanoTime();
        long previousVisualizationTime = previousTime;
        running = true;
        C.endText = "END.";
        try {
            while (C.io.hasMoreIterations()) {
                if (C.hasToStop) {
                    doStop();
                    break;
                }
                Iteration iteration = C.io.readNextIteration();
                if (iteration == null) {
                    break;
                }

                if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                    previousTime = System.nanoTime();
                    info(logger, "Cycle: " + iteration.getCycle() + ", number of objects: " + iteration.getNumberOfObjects());
                }

                if (C.prop.getPlayingSpeed() < 0) {
                    /* Slow down */
                    Thread.sleep(-C.prop.getPlayingSpeed());
                    C.visualizer.visualize(iteration);
                    previousVisualizationTime = System.nanoTime();
                } else if ((System.nanoTime() - previousVisualizationTime) / NANOSECONDS_IN_ONE_MILLISECOND >= C.prop.getPlayingSpeed()) {
                    C.visualizer.visualize(iteration);
                    previousVisualizationTime = System.nanoTime();
                }
            }
            info(logger, "End.");
            C.visualizer.end();
        } catch (IOException e) {
            error(logger, "Error while reading simulation object.", e);
        } catch (InterruptedException e) {
            error(logger, "Thread interrupted.", e);
        } finally {
            running = false;
        }
    }

    private void doStop() {
        C.hasToStop = false;
        if (C.prop.isSaveToFile()) {
            C.io.endFile();
        }
        if (C.visualizer != null) {
            C.endText = "Stopped!";
            C.visualizer.closeWindow();
        }
    }

    public void startSimulation() throws SimulationException {
        initArrays(C.prop.getInitialObjects());
        if (duplicateIdExists(simulationLogicKernel.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(simulationLogicKernel.positionX, simulationLogicKernel.positionY, simulationLogicKernel.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        info(logger, "Done.\n");
        Utils.printConfiguration(C.prop);

        info(logger, "Start simulation...");
        C.endText = "END.";
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long previousVisualizationTime = startTime;
        long endTime;

        running = true;
        try {
            for (long i = 0; C.prop.isInfiniteSimulation() || i < C.prop.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop) {
                        doStop();
                        break;
                    }

                    iterationCounter = i + 1;

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                        showRemainingTime(i, startTime, C.prop.getNumberOfIterations(), countObjects());
                        previousTime = System.nanoTime();
                    }

                    if (C.prop.isRealTimeVisualization()) {
                        if (C.prop.getPlayingSpeed() < 0) {
                            /* Slow down */
                            Thread.sleep(-C.prop.getPlayingSpeed());
                            C.visualizer.visualize();
                            previousVisualizationTime = System.nanoTime();
                        } else if ((System.nanoTime() - previousVisualizationTime) / NANOSECONDS_IN_ONE_MILLISECOND >= C.prop.getPlayingSpeed()) {
                            C.visualizer.visualize();
                            previousVisualizationTime = System.nanoTime();
                        }
                    }

                    doIteration();
                } catch (InterruptedException e) {
                    error(logger, "Concurrency failure. One of the threads interrupted in cycle " + i, e);
                }
            }

            if (C.prop.isRealTimeVisualization()) {
                C.visualizer.end();
            }
            endTime = System.nanoTime();
        } finally {
            running = false;
            if (C.prop.isSaveToFile()) {
                C.io.endFile();
            }
        }

        info(logger, "End of simulation. Time: " + nanoToHumanReadable(endTime - startTime));
    }

    private void doIteration() throws InterruptedException {
        deepCopy(simulationLogicKernel.positionX, simulationLogicKernel.readOnlyPositionX);
        deepCopy(simulationLogicKernel.positionY, simulationLogicKernel.readOnlyPositionY);
        deepCopy(simulationLogicKernel.speedX, simulationLogicKernel.readOnlySpeedX);
        deepCopy(simulationLogicKernel.speedY, simulationLogicKernel.readOnlySpeedY);
        deepCopy(simulationLogicKernel.mass, simulationLogicKernel.readOnlyMass);
        deepCopy(simulationLogicKernel.radius, simulationLogicKernel.readOnlyRadius);
        deepCopy(simulationLogicKernel.color, simulationLogicKernel.readOnlyColor);
        deepCopy(simulationLogicKernel.deleted, simulationLogicKernel.readOnlyDeleted);

        /* Execute in parallel on GPU if available */
        simulationLogicKernel.execute(simulationLogicRange);
        if (iterationCounter == 1) {
            String message = "Simulation logic execution mode = " + simulationLogicKernel.getExecutionMode();
            if (GPU.equals(simulationLogicKernel.getExecutionMode())) {
                info(logger, message);
            } else {
                warn(logger, message);
            }
        }

        /* Collision and merging */
        collisionCheckKernel.prepare();

        /* Execute in parallel on GPU if available */
        collisionCheckKernel.execute(collisionCheckRange);
        if (iterationCounter == 1) {
            String message = "Collision detection execution mode = " + simulationLogicKernel.getExecutionMode();
            if (GPU.equals(simulationLogicKernel.getExecutionMode())) {
                info(logger, message);
            } else {
                warn(logger, message);
            }
        }

        /* If collision/s exists execute sequentially on a single thread */
        if (collisionCheckKernel.collisionExists()) {
            simulationLogicKernel.processCollisions();
        }

        if (C.prop.isSaveToFile()) {
            C.io.appendObjectsToFile();
        }
    }

    public long getCurrentIterationNumber() {
        return iterationCounter;
    }

    private int countObjects() {
        int numberOfObjects = 0;
        for (int j = 0; j < simulationLogicKernel.deleted.length; j++) {
            if (!simulationLogicKernel.deleted[j]) {
                numberOfObjects++;
            }
        }
        return numberOfObjects;
    }
}
