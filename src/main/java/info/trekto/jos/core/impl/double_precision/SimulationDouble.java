package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Range;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.util.Utils.*;

/**
 * This implementation uses Aparapi library and runs on GPU if possible.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationDouble extends SimulationAP implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationDouble.class);
    private static final String CPU_VERSION_LINK = "https://sourceforge.net/projects/jos-n-body/files/jos-cpu.jar/download";

    private final SimulationLogicDouble simulationLogic;
    private final Range simulationLogicRange;
    private final CollisionCheckDouble collisionCheckKernel;
    private final Range collisionCheckRange;
    private final double[] zeroArray;

    public SimulationDouble(SimulationProperties properties) {
        super(properties);
        final int n = properties.getNumberOfObjects();
        simulationLogic = new SimulationLogicDouble(n, properties.getSecondsPerIteration().doubleValue());
        zeroArray = new double[n];
        simulationLogicRange = Range.create(n);
        simulationLogic.setExecutionMode(GPU);

        collisionCheckKernel = new CollisionCheckDouble(
                n,
                simulationLogic.positionX,
                simulationLogic.positionY,
                simulationLogic.radius,
                simulationLogic.deleted);
        collisionCheckRange = Range.create(n);
        collisionCheckKernel.setExecutionMode(GPU);
    }

    private void doIteration(boolean saveCurrentIterationToFile) {
        deepCopy(simulationLogic.positionX, simulationLogic.readOnlyPositionX);
        deepCopy(simulationLogic.positionY, simulationLogic.readOnlyPositionY);
        deepCopy(simulationLogic.speedX, simulationLogic.readOnlySpeedX);
        deepCopy(simulationLogic.speedY, simulationLogic.readOnlySpeedY);
        deepCopy(simulationLogic.mass, simulationLogic.readOnlyMass);
        deepCopy(simulationLogic.radius, simulationLogic.readOnlyRadius);
        deepCopy(simulationLogic.color, simulationLogic.readOnlyColor);
        deepCopy(simulationLogic.deleted, simulationLogic.readOnlyDeleted);

        /* Execute in parallel on GPU if available */
        simulationLogic.execute(simulationLogicRange);
        if (iterationCounter == 1) {
            String message = "Simulation logic execution mode = " + simulationLogic.getExecutionMode();
            if (GPU.equals(simulationLogic.getExecutionMode())) {
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
            String message = "Collision detection execution mode = " + simulationLogic.getExecutionMode();
            if (GPU.equals(collisionCheckKernel.getExecutionMode())) {
                info(logger, message);
            } else {
                warn(logger, message);
            }
        }

        /* If collision/s exists execute sequentially on a single thread */
        if (collisionCheckKernel.collisionExists()) {
            simulationLogic.processCollisions();
        }

        if (properties.isSaveToFile() && saveCurrentIterationToFile) {
            C.getReaderWriter().appendObjectsToFile(properties, iterationCounter, simulationLogic.positionX, simulationLogic.positionY,
                                                    zeroArray, simulationLogic.speedX, simulationLogic.speedY, zeroArray, simulationLogic.mass,
                                                    simulationLogic.radius, simulationLogic.id, simulationLogic.color, simulationLogic.deleted);
        }
    }

    public void initArrays(List<SimulationObject> initialObjects) {
        Arrays.fill(simulationLogic.deleted, true);
        for (int i = 0; i < initialObjects.size(); i++) {
            SimulationObject o = initialObjects.get(i);
            simulationLogic.positionX[i] = o.getX().doubleValue();
            simulationLogic.positionY[i] = o.getY().doubleValue();
            simulationLogic.speedX[i] = o.getSpeed().getX().doubleValue();
            simulationLogic.speedY[i] = o.getSpeed().getY().doubleValue();
            simulationLogic.mass[i] = o.getMass().doubleValue();
            simulationLogic.radius[i] = o.getRadius().doubleValue();
            simulationLogic.id[i] = o.getId();
            simulationLogic.color[i] = o.getColor();
            simulationLogic.deleted[i] = false;
        }
    }

    @Override
    public void startSimulation() throws SimulationException {
        init();

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
                    int numberOfObjects = countObjects();

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
                        SimulationLogicDouble sl = simulationLogic;
                        C.getVisualizer().visualize(iterationCounter, numberOfObjects, sl.id, sl.deleted, sl.positionX, sl.positionY, sl.radius,
                                                    sl.color);
                        previousVisualizationTime = System.nanoTime();
                    }

                    doIteration(i % properties.getSaveEveryNthIteration() == 0);
                } catch (InterruptedException e) {
                    error(logger, "Concurrency failure. One of the threads interrupted in cycle " + i, e);
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

    private void init() throws SimulationException {
        initArrays(properties.getInitialObjects());
        if (duplicateIdExists(simulationLogic.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(simulationLogic.positionX, simulationLogic.positionY, simulationLogic.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        info(logger, "Done.\n");
        Utils.printConfiguration(properties);
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

    public int countObjects() {
        int numberOfObjects = 0;
        for (int j = 0; j < simulationLogic.deleted.length; j++) {
            if (!simulationLogic.deleted[j]) {
                numberOfObjects++;
            }
        }
        return numberOfObjects;
    }

    public void checkGpu() {
        String message = "Looks like your video card is not compatible with Aparapi.\n"
                + "Please download the CPU version: " + CPU_VERSION_LINK + "\n"
                + "Please send me email with this message and your video card model:\n"
                + "trayan.momkov аt gmail with subject: JOS - Error.\n";

        String htmlMessage = "<p>Looks like your video card is not compatible with Aparapi.</p>"
                + "<p>Please download the CPU version: <a href=\"" + CPU_VERSION_LINK + "\">" + CPU_VERSION_LINK + "</a></p>"
                + "<p>Please send me email with this message and your video card model:</p>"
                + "<p>trayan.momkov аt gmail with subject: JOS - Error.</p>";
        try {
            AparapiTestKernel testKernel = new AparapiTestKernel();
            Range testKernelRange = Range.create(5);
            testKernel.setExecutionMode(GPU);

            testKernel.execute(testKernelRange);
            if (!GPU.equals(testKernel.getExecutionMode())) {
                error(logger, message);
                C.showHtmlError(htmlMessage);
            }
        } catch (Exception ex) {
            error(logger, message, ex);
            C.showHtmlError(htmlMessage, ex);
        }
    }

    public boolean collisionExists(double[] positionX, double[] positionY, double[] radius) {
        for (int i = 0; i < positionX.length; i++) {
            for (int j = 0; j < positionX.length; j++) {
                if (i == j) {
                    continue;
                }
                // distance between centres
                double distance = SimulationLogicDouble.calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);

                if (distance < radius[i] + radius[j]) {
                    info(logger, String.format("Collision between object A(x:%f, y:%f, r:%f) and B(x:%f, y:%f, r:%f)",
                                               positionX[i], positionY[i], radius[i], positionX[j], positionY[j], radius[j]));
                    return true;
                }
            }
        }
        return false;
    }
}
