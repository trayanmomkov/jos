package info.trekto.jos.core.impl;

import com.aparapi.Range;
import info.trekto.jos.C;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private long iterationCounter;
    public SimulationLogicImpl kernel;
    private final Range range;

    public SimulationImpl(int numberOfObjects, double secondsPerIteration) {
        kernel = new SimulationLogicImpl(numberOfObjects, secondsPerIteration);
        range = Range.create(kernel.positionX.length);
        kernel.setExecutionMode(GPU);
    }

    public long startSimulation() throws SimulationException {
        if (duplicateIdExists(kernel.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(kernel.positionX, kernel.positionY, kernel.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        logger.info("Done.\n");
        Utils.printConfiguration(C.prop);

        logger.info("\nStart simulation...");
        C.endText = "END.";
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long endTime;

        try {
            for (long i = 0; C.prop.isInfiniteSimulation() || i < C.prop.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop) {
                        C.hasToStop = false;
                        C.io.endFile();
                        C.endText = "Stopped!";
                        break;
                    }

                    iterationCounter = i + 1;

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                        showRemainingTimeBasedOnLastNIterations(i, startTime, C.prop.getNumberOfIterations(), countObjects());
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
            if (C.prop.isSaveToFile()) {
                C.io.endFile();
            }
        }

        logger.info("End of simulation. Time: " + nanoToHumanReadable(endTime - startTime));
        return endTime - startTime;
    }

    private void doIteration() throws InterruptedException {
        deepCopy(kernel.positionX, kernel.readOnlyPositionX);
        deepCopy(kernel.positionY, kernel.readOnlyPositionY);
        deepCopy(kernel.speedX, kernel.readOnlySpeedX);
        deepCopy(kernel.speedY, kernel.readOnlySpeedY);
        deepCopy(kernel.mass, kernel.readOnlyMass);
        deepCopy(kernel.radius, kernel.readOnlyRadius);
        deepCopy(kernel.color, kernel.readOnlyColor);
        deepCopy(kernel.deleted, kernel.readOnlyDeleted);

        kernel.execute(range);
        if (!GPU.equals(kernel.getExecutionMode()) || iterationCounter == 1) {
            logger.warn("Execution mode = " + kernel.getExecutionMode());
        }

        /* Collision and merging */
        kernel.processCollisions();

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
        for (int j = 0; j < kernel.deleted.length; j++) {
            if (!kernel.deleted[j]) {
                numberOfObjects++;
            }
        }
        return numberOfObjects;
    }
}
