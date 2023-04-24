package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Range;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.GpuChecker.checkExecutionMode;
import static info.trekto.jos.core.GpuChecker.createRange;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_MILLISECOND;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.deepCopy;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.info;
import static info.trekto.jos.util.Utils.nanoToHumanReadable;
import static info.trekto.jos.util.Utils.showRemainingTime;
import static java.util.stream.IntStream.range;

/**
 * This implementation uses Aparapi library and runs on GPU if possible.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationFloat extends SimulationAP implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationFloat.class);

    private final MoveObjectsLogicFloat moveObjectsLogic;
    private final Range moveObjectsRange;
    private final ProcessCollisionsLogicFloat processCollisionsLogic;
    private final Range processCollisionsRange;
    private final float[] zeroArray;
    private final SimulationAP cpuSimulation;
    private boolean executingOnCpu;
    private final GpuDataFloat data;

    public SimulationFloat(SimulationProperties properties, SimulationAP cpuSimulation) {
        super(properties);
        final int n = properties.getNumberOfObjects();
        int screenWidth = 0;
        int screenHeight = 0;
        if (properties.isBounceFromScreenBorders()) {
            screenWidth = C.getVisualizer().getVisualizationPanel().getWidth();
            screenHeight = C.getVisualizer().getVisualizationPanel().getHeight();
        }
        zeroArray = new float[n];
        data = new GpuDataFloat(n);
        float coefficientOfRestitution = properties.getCoefficientOfRestitution().floatValue();
        
        moveObjectsLogic = new MoveObjectsLogicFloat(data, properties.getSecondsPerIteration().floatValue(), screenWidth, screenHeight);
        moveObjectsRange = createRange(n);
        moveObjectsLogic.setExecutionMode(GPU);

        processCollisionsLogic = new ProcessCollisionsLogicFloat(data, properties.isMergeOnCollision(), coefficientOfRestitution);
        processCollisionsRange = createRange(n);
        processCollisionsLogic.setExecutionMode(GPU);
        
        this.cpuSimulation = cpuSimulation;
    }

    @Override
    public void doIteration(boolean saveCurrentIterationToFile, long iterationCounter) {
        data.copyToReadOnly(properties.isMergeOnCollision());
        moveObjectsLogic.execute(moveObjectsRange); /* Execute in parallel on GPU if available */
//        moveObjectsLogic.runOnCpu();
        checkExecutionMode(iterationCounter, moveObjectsLogic);

        data.copyToReadOnly(properties.isMergeOnCollision());
        processCollisionsLogic.execute(processCollisionsRange); /* Collisions - Execute in parallel on GPU if available */
//        processCollisionsLogic.runOnCpu();
        checkExecutionMode(iterationCounter, processCollisionsLogic);

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
            data.positionX[i] = o.getX().floatValue();
            data.positionY[i] = o.getY().floatValue();
            data.velocityX[i] = o.getVelocity().getX().floatValue();
            data.velocityY[i] = o.getVelocity().getY().floatValue();
            data.accelerationX[i] = o.getAcceleration().getX().floatValue();
            data.accelerationY[i] = o.getAcceleration().getY().floatValue();
            data.mass[i] = o.getMass().floatValue();
            data.radius[i] = o.getRadius().floatValue();
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
                    int numberOfObjects = executingOnCpu ? cpuSimulation.getObjects().size() : countObjects();

                    if (cpuSimulation != null && !executingOnCpu && numberOfObjects <= C.getCpuThreshold()) {
                        cpuSimulation.initSwitchingFromGpu(convertToSimulationObjects());
                        executingOnCpu = true;
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
                            C.getVisualizer().visualize(cpuSimulation.getObjects(), iterationCounter);
                        } else {
                            C.getVisualizer().visualize(iterationCounter, numberOfObjects, data.id, data.deleted,
                                                        range(0, data.positionX.length).mapToDouble(j -> data.positionX[j]).toArray(),
                                                        range(0, data.positionY.length).mapToDouble(j -> data.positionY[j]).toArray(),
                                                        range(0, data.radius.length).mapToDouble(j -> data.radius[j]).toArray(),
                                                        data.color);
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

    private List<SimulationObject> convertToSimulationObjects() {
        List<SimulationObject> objects = new ArrayList<>();

        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                SimulationObject simo = new SimulationObjectImpl();
                simo.setId(data.id[i]);

                simo.setX(New.num(data.positionX[i]));
                simo.setY(New.num(data.positionY[i]));
                simo.setZ(New.num(0));

                simo.setMass(New.num(data.mass[i]));

                simo.setVelocity(new TripleNumber(New.num(data.velocityX[i]),
                                                  New.num(data.velocityY[i]),
                                                  New.num(0)));

                simo.setAcceleration(new TripleNumber(New.num(data.accelerationX[i]),
                                                      New.num(data.accelerationY[i]),
                                                      New.num(0)));

                simo.setRadius(New.num(data.radius[i]));
                simo.setColor(data.color[i]);

                objects.add(simo);
            }
        }

        return objects;
    }

    public void init() throws SimulationException {
        initArrays(properties.getInitialObjects());
        
        if (duplicateIdExists(data.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(data.positionX, data.positionY, data.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        executingOnCpu = false;

        info(logger, "Done.\n");
        Utils.printConfiguration(this);
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
        for (int j = 0; j < data.deleted.length; j++) {
            if (!data.deleted[j]) {
                numberOfObjects++;
            }
        }
        return numberOfObjects;
    }

    public boolean collisionExists(float[] positionX, float[] positionY, float[] radius) {
        for (int i = 0; i < positionX.length; i++) {
            for (int j = 0; j < positionX.length; j++) {
                if (i == j) {
                    continue;
                }
                // distance between centres
                float distance = moveObjectsLogic.calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);

                if (distance < radius[i] + radius[j]) {
                    info(logger, String.format("Collision between object A(x:%f, y:%f, r:%f) and B(x:%f, y:%f, r:%f)",
                                               positionX[i], positionY[i], radius[i], positionX[j], positionY[j], radius[j]));
                    return true;
                }
            }
        }
        return false;
    }

    public SimulationAP getCpuSimulation() {
        return cpuSimulation;
    }
}
