package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Range;
import info.trekto.jos.core.CpuSimulation;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.Data;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.DataAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.GpuChecker.checkExecutionMode;
import static info.trekto.jos.core.GpuChecker.createRange;
import static info.trekto.jos.core.impl.Data.countObjects;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_MILLISECOND;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.deepCopy;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.info;
import static info.trekto.jos.util.Utils.nanoToHumanReadable;
import static info.trekto.jos.util.Utils.showRemainingTime;

/**
 * This implementation uses Aparapi library and runs on GPU if possible.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationDouble implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationDouble.class);
    public static final int PAUSE_SLEEP_MILLISECONDS = 100;
    public static final int SHOW_REMAINING_INTERVAL_SECONDS = 2;

    private final MoveObjectsLogicDouble moveObjectsLogic;
    private final Range moveObjectsRange;
    private final ProcessCollisionsLogicDouble processCollisionsLogic;
    private final Range processCollisionsRange;
    private final double[] zeroArray;
    private final CpuSimulation cpuSimulation;
    private boolean executingOnCpu;
    private final DataDouble data;
    private final SimulationProperties properties;

    public SimulationDouble(SimulationProperties properties, CpuSimulation cpuSimulation) {
        this.properties = properties;
        final int n = properties.getNumberOfObjects();
        int screenWidth = 0;
        int screenHeight = 0;
        if (properties.isBounceFromScreenBorders()) {
            if (C.getVisualizer() == null) {
                C.setVisualizer(C.createVisualizer(properties));
            }
            screenWidth = C.getVisualizer().getVisualizationPanel().getWidth();
            screenHeight = C.getVisualizer().getVisualizationPanel().getHeight();
        }
        zeroArray = new double[n];
        data = new DataDouble(n);
        double coefficientOfRestitution = properties.getCoefficientOfRestitution().doubleValue();

        moveObjectsLogic = new MoveObjectsLogicDouble(data, properties.getSecondsPerIteration().doubleValue(),
                                                      properties.getMinDistance().doubleValue(), screenWidth, screenHeight);
        moveObjectsRange = createRange(n);
        moveObjectsLogic.setExecutionMode(GPU);

        processCollisionsLogic = new ProcessCollisionsLogicDouble(data, properties.isMergeOnCollision(), coefficientOfRestitution);
        processCollisionsRange = createRange(n);
        processCollisionsLogic.setExecutionMode(GPU);

        this.cpuSimulation = cpuSimulation;
    }

    @Override
    public void doIteration(boolean saveCurrentIterationToFile, long iterationCounter) {
        data.copyToReadOnly(properties.isMergeOnCollision());
//        moveObjectsLogic.runOnCpu();
        moveObjectsLogic.execute(moveObjectsRange); /* Execute in parallel on GPU if available */
        checkExecutionMode(iterationCounter, moveObjectsLogic);

        data.copyToReadOnly(properties.isMergeOnCollision());
//        processCollisionsLogic.runOnCpu();
        processCollisionsLogic.execute(processCollisionsRange); /* Collisions - Execute in parallel on GPU if available */
        checkExecutionMode(iterationCounter, processCollisionsLogic);

        if (properties.isSaveToFile() && saveCurrentIterationToFile) {
            C.getReaderWriter().appendObjectsToFile(properties, iterationCounter, data.positionX, data.positionY, zeroArray, data.velocityX,
                                                    data.velocityY, zeroArray, data.mass, data.radius, data.id, data.color, data.deleted,
                                                    data.accelerationX, data.accelerationY, zeroArray);
        }
    }

    @Override
    public double calculateTotalMass() {
        double mass = 0;
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                mass += data.mass[i];
            }
        }
        return mass;
    }

    @Override
    public double calculateTotalMomentum() {
        double momentum = 0;
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                momentum += data.mass[i] * (Math.abs(data.velocityX[i]) + Math.abs(data.velocityY[i]));
            }
        }
        return momentum;
    }

    public void initArrays(List<SimulationObject> initialObjects) {
        for (int i = 0; i < initialObjects.size(); i++) {
            SimulationObject o = initialObjects.get(i);
            data.positionX[i] = o.getX().doubleValue();
            data.positionY[i] = o.getY().doubleValue();
            data.velocityX[i] = o.getVelocity().getX().doubleValue();
            data.velocityY[i] = o.getVelocity().getY().doubleValue();
            data.accelerationX[i] = o.getAcceleration().getX().doubleValue();
            data.accelerationY[i] = o.getAcceleration().getY().doubleValue();
            data.mass[i] = o.getMass().doubleValue();
            data.radius[i] = o.getRadius().doubleValue();
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

                    long iterationCounter = i + 1;
                    int numberOfObjects = countObjects(data);

                    if (cpuSimulation != null && !executingOnCpu && numberOfObjects <= C.getCpuThreshold()) {
                        info(logger, "Switching to CPU - Initialize simulation...");

                        cpuSimulation.setDataAndInitializeLogic(convertToDataAP());
                        executingOnCpu = true;

                        info(logger, "Done.\n");
                        Utils.printConfiguration(this);
                    }

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
                        if (executingOnCpu) {
                            DataAP cpuData = cpuSimulation.getData();
                            C.getVisualizer().visualize(iterationCounter, numberOfObjects, cpuData.id, cpuData.deleted,
                                                        Arrays.stream(cpuData.positionX).mapToDouble(Number::doubleValue).toArray(),
                                                        Arrays.stream(cpuData.positionY).mapToDouble(Number::doubleValue).toArray(),
                                                        Arrays.stream(cpuData.radius).mapToDouble(Number::doubleValue).toArray(),
                                                        cpuData.color);
                        } else {
                            C.getVisualizer().visualize(iterationCounter, numberOfObjects, data.id, data.deleted, data.positionX, data.positionY,
                                                        data.radius, data.color);
                        }
                        previousVisualizationTime = System.nanoTime();
                    }

                    if (executingOnCpu) {
                        cpuSimulation.doIteration(i % properties.getSaveEveryNthIteration() == 0, iterationCounter);
                    } else {
                        doIteration(i % properties.getSaveEveryNthIteration() == 0, iterationCounter);
                    }
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

    public void init(boolean printInfo) throws SimulationException {
        if (printInfo) {
            info(logger, "Initialize simulation...");
        }

        initArrays(properties.getInitialObjects());

        if (duplicateIdExists(data.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        long start = System.nanoTime();
        if (collisionExists(data.positionX, data.positionY, data.radius)) {
            throw new SimulationException("Initial collision exists!");
        }
        info(logger, "Initial collision check time: " + (System.nanoTime() - start) / (double)NANOSECONDS_IN_ONE_SECOND);

        executingOnCpu = false;

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

    public boolean collisionExists(double[] positionX, double[] positionY, double[] radius) {
        ProcessCollisionsLogicDouble tempCollisionLogic = new ProcessCollisionsLogicDouble(data, true, 1);
        Range tempRange = createRange(data.n);

        data.copyToReadOnly(true);
        tempCollisionLogic.execute(tempRange);
        for (int i = 0; i < data.n; i++) {
            if (data.deleted[i]) {
                info(logger, String.format("Collision with object A(x:%f, y:%f, r:%f)", positionX[i], positionY[i], radius[i]));
                    return true;
                }
            }
        return false;
    }

    public CpuSimulation getCpuSimulation() {
        return cpuSimulation;
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
    public Data getData() {
        return data;
    }

    private DataAP convertToDataAP() {
        DataAP dataAp = new DataAP(data.n);
        deepCopy(data.id, dataAp.id);
        deepCopy(data.deleted, dataAp.deleted);
        deepCopy(data.color, dataAp.color);
        deepCopy(data.deleted, dataAp.readOnlyDeleted);
        deepCopy(data.color, dataAp.readOnlyColor);

        for (int i = 0; i < data.n; i++) {
            dataAp.positionX[i] = New.num(data.positionX[i]);
            dataAp.positionY[i] = New.num(data.positionY[i]);
            dataAp.radius[i] = New.num(data.radius[i]);
            dataAp.velocityX[i] = New.num(data.velocityX[i]);
            dataAp.velocityY[i] = New.num(data.velocityY[i]);
            dataAp.accelerationX[i] = New.num(data.accelerationX[i]);
            dataAp.accelerationY[i] = New.num(data.accelerationY[i]);
            dataAp.mass[i] = New.num(data.mass[i]);

            dataAp.mass[i] = New.num(data.mass[i]);
            dataAp.positionX[i] = New.num(data.positionX[i]);
            dataAp.positionY[i] = New.num(data.positionY[i]);
            dataAp.radius[i] = New.num(data.radius[i]);
            dataAp.velocityX[i] = New.num(data.velocityX[i]);
            dataAp.velocityY[i] = New.num(data.velocityY[i]);
        }

        return dataAp;
    }
}
