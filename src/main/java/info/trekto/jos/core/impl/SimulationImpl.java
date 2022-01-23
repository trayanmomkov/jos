package info.trekto.jos.core.impl;

import com.aparapi.Range;
import info.trekto.jos.C;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
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
        }
        Arrays.fill(C.simulation.simulationLogicKernel.deleted, false);
    }

    public static void init(String inputFile) {
        C.io = new JsonReaderWriter();
        try {
            C.prop = C.io.readProperties(inputFile);
        } catch (FileNotFoundException e) {
            logger.error("Cannot read properties file.", e);
        }
    }

    public static void init(SimulationProperties prop) {
        C.io = new JsonReaderWriter();
        C.prop = prop;
        C.simulation = new SimulationImpl(C.prop.getNumberOfObjects(), C.prop.getSecondsPerIteration());
    }

    public long startSimulation() throws SimulationException {
        initArrays(C.prop.getInitialObjects());
        if (duplicateIdExists(simulationLogicKernel.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(simulationLogicKernel.positionX, simulationLogicKernel.positionY, simulationLogicKernel.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        logger.info("Done.\n");
        Utils.printConfiguration(C.prop);

        logger.info("\nStart simulation...");
        C.endText = "END.";
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long endTime;

        running = true;
        try {
            for (long i = 0; C.prop.isInfiniteSimulation() || i < C.prop.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop) {
                        C.hasToStop = false;
                        if (C.prop.isSaveToFile()) {
                            C.io.endFile();
                        }
                        C.endText = "Stopped!";
                        C.visualizer.closeWindow();
                        break;
                    }

                    iterationCounter = i + 1;

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                        showRemainingTime(i, startTime, C.prop.getNumberOfIterations(), countObjects());
                        previousTime = System.nanoTime();
                    }

                    if (C.prop.isRealTimeVisualization() && System.nanoTime() - previousTime >= C.prop.getPlayingSpeed()) {
                        C.visualizer.visualize();
                    }

                    doIteration();
                } catch (InterruptedException e) {
                    logger.error("Concurrency failure. One of the threads interrupted in cycle " + i, e);
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

        logger.info("End of simulation. Time: " + nanoToHumanReadable(endTime - startTime));
        return endTime - startTime;
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

        simulationLogicKernel.execute(simulationLogicRange);
        if (!GPU.equals(simulationLogicKernel.getExecutionMode()) || iterationCounter == 1) {
            logger.warn("Execution mode = " + simulationLogicKernel.getExecutionMode());
        }

        /* Collision and merging */
        collisionCheckKernel.prepare();
        collisionCheckKernel.execute(collisionCheckRange);
        if (!GPU.equals(collisionCheckKernel.getExecutionMode()) || iterationCounter == 1) {
            logger.warn("CollisionCheckKernel Execution mode = " + collisionCheckKernel.getExecutionMode());
        }
        if (collisionCheckKernel.collisionExists()) {
            simulationLogicKernel.processCollisions();
        }

        if (C.prop.isRealTimeVisualization() && C.prop.getPlayingSpeed() < 0) {
            Thread.sleep(-C.prop.getPlayingSpeed());
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
